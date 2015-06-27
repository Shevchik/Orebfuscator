/*
` * Copyright (C) 2011-2014 lishid.  All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.orebfuscator.obfuscation;

import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.Packet51;
import com.lishid.orebfuscator.internal.Packet56;
import com.lishid.orebfuscator.internal.WorldServer;

public class Calculations {

	public static void Obfuscate(Packet56 packet, Player player) {

		if (OrebfuscatorConfig.isWorldDisabled(player.getWorld().getName())) {
			return;
		}

		ChunkInfo[] infos = getInfo(packet, player);

		for (int chunkNum = 0; chunkNum < infos.length; chunkNum++) {
			ComputeChunkInfoAndObfuscate(infos[chunkNum]);
		}

		packet.compress();
	}

	public static void Obfuscate(Packet51 packet, Player player) {

		if (OrebfuscatorConfig.isWorldDisabled(player.getWorld().getName())) {
			return;
		}

		ChunkInfo info = getInfo(packet, player);

		ComputeChunkInfoAndObfuscate(info);

		packet.compress();
	}

	private static ChunkInfo[] getInfo(Packet56 packet, Player player) {
		ChunkInfo[] infos = new ChunkInfo[packet.getPacketChunkNumber()];

		int[] x = packet.getX();
		int[] z = packet.getZ();

		int[] chunkMask = packet.getChunkMask();
		int[] extraMask = packet.getExtraMask();

		// Create an info objects
		int writeIndex = 0;
		for (int chunkNum = 0; chunkNum < packet.getPacketChunkNumber(); chunkNum++) {
			ChunkInfo info = new ChunkInfo();
			infos[chunkNum] = info;
			info.world = player.getWorld();
			info.chunkX = x[chunkNum];
			info.chunkZ = z[chunkNum];
			info.chunkMask = chunkMask[chunkNum];
			info.extraMask = extraMask[chunkNum];
			info.data = packet.getInflatedBuffers()[chunkNum];
			info.buildBuffer = packet.getBuildBuffer();
			info.writeIndex = writeIndex;
			writeIndex += packet.getInflatedBuffers()[chunkNum].length;
		}

		return infos;
	}

	private static ChunkInfo getInfo(Packet51 packet, Player player) {
		// Create an info objects
		ChunkInfo info = new ChunkInfo();
		info.world = player.getWorld();
		info.chunkX = packet.getX();
		info.chunkZ = packet.getZ();
		info.chunkMask = packet.getChunkMask();
		info.extraMask = packet.getExtraMask();
		info.data = packet.getInflatedBuffer();
		info.buildBuffer = packet.getBuildBuffer();
		return info;
	}

	private static void ComputeChunkInfoAndObfuscate(ChunkInfo info) {
		// Compute chunk number
		for (int i = 0; i < 16; i++) {
			if ((info.chunkMask & (1 << i)) != 0) {
				info.chunkSectionToIndexMap[i] = info.chunkSectionNumber;
				info.chunkSectionNumber++;
			}
			if ((info.extraMask & (1 << i)) != 0) {
				info.extraSectionToIndexMap[i] = info.extraSectionNumber;
				info.extraSectionNumber++;
			}
		}

		Obfuscate(info);
	}

	private static void Obfuscate(ChunkInfo info) {
		int e1r = info.world.getEnvironment() == Environment.NETHER ? 87 :1;

		int engineMode = OrebfuscatorConfig.EngineMode;

		int currentTypeIndex = 0;
		//add array can start at index 10240 or at index 8192, it is based on if we have light data or not
		int addExtendedIndex = info.chunkSectionNumber * (WorldServer.hasLight(info.world) ? 10240 : 8192);
		int currentExtendedIndex = 0;
		int startX = info.chunkX << 4;
		int startZ = info.chunkZ << 4;

		int randomBlock = 0;

		// Loop over 16x16x16 chunks in the 16x256x16 column
		for (int i = 0; i < 16; i++) {
			if ((info.chunkMask & (1 << i)) != 0) {
				boolean usesExtra = ((info.extraMask & (1 << i)) != 0);

				for (int y = 0; y < 16; y++) {
					int blockY = (i << 4) + y;
					if (blockY > OrebfuscatorConfig.MaxObfuscateHeight) {
						return;
					}
					for (int z = 0; z < 16; z++) {
						for (int x = 0; x < 16; x++) {

							int typeID = info.data[currentTypeIndex] & 0xFF;
							if (usesExtra) {
								if ((currentTypeIndex & 1) == 0) {
									typeID |= ((info.data[addExtendedIndex + currentExtendedIndex] << 8) & 0xF00);
								} else {
									typeID |= ((info.data[addExtendedIndex + currentExtendedIndex] << 4) & 0xF00);
								}
							}

							// Obfuscate block if needed or copy old
							if (OrebfuscatorConfig.isObfuscated(typeID) && !areAjacentBlocksTransparent(info, addExtendedIndex, startX + x, blockY, startZ + z)) {
								int newBlockID = 0;
								if (engineMode == 1) {
									// Engine mode 1, use stone
									newBlockID = e1r;
								} else if (engineMode == 2) {
									// Ending mode 2, get random block
									if (randomBlock >= OrebfuscatorConfig.RandomBlocks.length) {
										randomBlock = 0;
									}
									newBlockID = OrebfuscatorConfig.RandomBlocks[randomBlock++];
								}
								info.buildBuffer[info.writeIndex + currentTypeIndex] = (byte) newBlockID;
								if (usesExtra) {
									if ((currentTypeIndex & 1) == 0) {
										int otherHalf = (info.buildBuffer[info.writeIndex + addExtendedIndex + currentExtendedIndex] & 0xF0);
										info.buildBuffer[info.writeIndex + addExtendedIndex + currentExtendedIndex] = (byte) (otherHalf | (newBlockID >> 8));
									} else {
										int otherHalf = (info.buildBuffer[info.writeIndex + addExtendedIndex + currentExtendedIndex] & 0x0F);
										info.buildBuffer[info.writeIndex + addExtendedIndex + currentExtendedIndex] = (byte) (otherHalf | ((newBlockID >> 8) << 4));
									}
								}
							}

							if (usesExtra && (currentTypeIndex & 1) == 1) {
								currentExtendedIndex++;
							}
							currentTypeIndex++;
						}
					}
				}
			}
		}
	}

	private static boolean areAjacentBlocksTransparent(ChunkInfo info, int addExtendedIndex, int x, int y, int z) {
		if (y == 0 || y == info.world.getMaxHeight()) {
			return true;
		}

		if (isTransparent(info, addExtendedIndex, x + 1, y, z)) {
			return true;
		}
		if (isTransparent(info, addExtendedIndex, x - 1, y, z)) {
			return true;
		}
		if (isTransparent(info, addExtendedIndex, x, y, z + 1)) {
			return true;
		}
		if (isTransparent(info, addExtendedIndex, x, y, z - 1)) {
			return true;
		}
		if (isTransparent(info, addExtendedIndex, x, y + 1, z)) {
			return true;
		}
		if (isTransparent(info, addExtendedIndex, x, y - 1, z)) {
			return true;
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	private static boolean isTransparent(ChunkInfo info, int addExtendedIndex, int x, int y, int z) {
		int ysection = y >> 4;
		if ((info.chunkMask & (1 << ysection)) != 0 && x >> 4 == info.chunkX && z >> 4 == info.chunkZ) {
			int blockindex = ((y & 0xF) << 8) + ((z & 0xF) << 4) + (x & 0xF);
			int typeID = info.data[info.chunkSectionToIndexMap[y >> 4] * 4096 + blockindex] & 0xFF;
			if ((info.extraMask & (1 << ysection)) != 0) {
				if ((blockindex & 1) == 0) {
					typeID |= ((info.data[addExtendedIndex + info.extraSectionToIndexMap[ysection] * 2048 + blockindex >> 1] << 8) & 0xF00);
				} else {
					typeID |= ((info.data[addExtendedIndex + info.extraSectionToIndexMap[ysection] * 2048 + blockindex >> 1] << 4) & 0xF00);
				}
			}
			return OrebfuscatorConfig.isBlockTransparent(typeID);
		}

		if (CalculationsUtil.isChunkLoaded(info.world, x >> 4, z >> 4)) {
			return OrebfuscatorConfig.isBlockTransparent(info.world.getBlockTypeIdAt(x, y, z));
		}

		return true;
	}

}
