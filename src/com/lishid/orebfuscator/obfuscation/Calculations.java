/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.v1_6_R3.Packet51;
import com.lishid.orebfuscator.internal.v1_6_R3.Packet56;

public class Calculations {

	public static void Obfuscate(PacketContainer container, Player player) {
		if (container.getType().equals(PacketType.Play.Server.MAP_CHUNK)) {
			Packet51 packet = new Packet51();
			packet.setPacket(container.getHandle());
			Calculations.Obfuscate(packet, player);
		} else if (container.getType().equals(PacketType.Play.Server.MAP_CHUNK_BULK)) {
			Packet56 packet = new Packet56();
			packet.setPacket(container.getHandle());
			Calculations.Obfuscate(packet, player);
		}
	}

	private static void Obfuscate(Packet56 packet, Player player) {
		ChunkInfo[] infos = getInfo(packet, player);

		ExecutorService localservice = Executors.newFixedThreadPool(5);
		for (int chunkNum = 0; chunkNum < infos.length; chunkNum++) {
			final ChunkInfo info = infos[chunkNum];
			localservice.execute(
				new Runnable() {
					@Override
					public void run() {
						ComputeChunkInfoAndObfuscate(info);
					}
				}
			);
		}
		localservice.shutdown();
		try {
			localservice.awaitTermination(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}

		packet.compress();
	}

	private static void Obfuscate(Packet51 packet, Player player) {
		ChunkInfo info = getInfo(packet, player);

		if (info.chunkMask == 0 && info.extraMask == 0) {
			return;
		}

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
		for (int chunkNum = 0; chunkNum < packet.getPacketChunkNumber(); chunkNum++) {
			ChunkInfo info = new ChunkInfo();
			infos[chunkNum] = info;
			info.world = player.getWorld();
			info.chunkX = x[chunkNum];
			info.chunkZ = z[chunkNum];
			info.chunkMask = chunkMask[chunkNum];
			info.extraMask = extraMask[chunkNum];
			info.data = packet.getInflatedBuffers()[chunkNum];
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

		if (info.chunkSectionNumber * 4096 > info.data.length) {
			return;
		}

		// Obfuscate
		if (!OrebfuscatorConfig.isWorldDisabled(info.world.getName())) {
			Obfuscate(info);
		}
	}

	private static void Obfuscate(ChunkInfo info) {
		boolean isNether = info.world.getEnvironment() == Environment.NETHER;

		int engineMode = OrebfuscatorConfig.EngineMode;

		int currentTypeIndex = 0;
		int addExtendedIndex = 10240 * info.chunkSectionNumber;
		int currentExtendedIndex = 0;
		int startX = info.chunkX << 4;
		int startZ = info.chunkZ << 4;

		// Loop over 16x16x16 chunks in the 16x256x16 column
		for (int i = 0; i < 16; i++) {
			if ((info.chunkMask & (1 << i)) != 0) {
				boolean usesExtra = ((info.extraMask & (1 << i)) != 0);

				for (int y = 0; y < 16; y++) {
					int blockY = (i << 4) + y;
					for (int z = 0; z < 16; z++) {
						for (int x = 0; x < 16; x++) {

							int typeID = info.data[currentTypeIndex] & 0xFF;
							if (usesExtra) {
								if (currentTypeIndex % 2 == 0) {
									typeID += info.data[addExtendedIndex + currentExtendedIndex] & 0x0F << 8;
								} else {
									typeID += info.data[addExtendedIndex + currentExtendedIndex] >> 4 & 0x0F << 8;
								}
							}

							// Obfuscate block if needed or copy old
							int newBlockID = 0;
							if (OrebfuscatorConfig.isObfuscated(typeID, isNether) && !areAjacentBlocksTransparent(info, startX + x, blockY, startZ + z)) {
								if (engineMode == 1) {
									// Engine mode 1, use stone
									newBlockID = (isNether ? 87 : 1);
								} else if (engineMode == 2) {
									// Ending mode 2, get random block
									newBlockID = OrebfuscatorConfig.getRandomBlockID(isNether);
								}
								info.data[currentTypeIndex] = (byte) newBlockID;
								if (usesExtra) {
									byte extra = (byte) (newBlockID >> 8);
									if (currentTypeIndex % 2 == 0) {
										info.data[addExtendedIndex + currentExtendedIndex] = extra;
									} else {
										info.data[addExtendedIndex + currentExtendedIndex] += (byte) (extra << 4);
									}
								}
							}

							if (usesExtra) {
								if (currentTypeIndex % 2 == 1) {
									currentExtendedIndex++;
								}
							}
							currentTypeIndex++;
						}
					}
				}
			}
		}
	}

	private static boolean areAjacentBlocksTransparent(ChunkInfo info, int x, int y, int z) {

		if (isTransparent(info, x + 1, y, z)) {
			return true;
		}
		if (isTransparent(info, x - 1, y, z)) {
			return true;
		}
		if (isTransparent(info, x, y, z + 1)) {
			return true;
		}
		if (isTransparent(info, x, y, z - 1)) {
			return true;
		}
		if (isTransparent(info, x, y + 1, z)) {
			return true;
		}
		if (isTransparent(info, x, y - 1, z)) {
			return true;
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	private static boolean isTransparent(ChunkInfo info, int x, int y, int z) {
		if (y < 0 || y > info.world.getMaxHeight()) {
			return true;
		}

		int ysection = y >> 4;
		if ((info.chunkMask & (1 << ysection)) != 0 && x >> 4 == info.chunkX && z >> 4 == info.chunkZ) {
			int section = info.chunkSectionToIndexMap[y >> 4];

			int blockindex = (y % 16 << 8) + (((z % 16) & 0x0F) << 4) + ((x % 16) & 0x0F);

			int typeID = info.data[section * 4096 + blockindex] & 0xFF;
			if ((info.extraMask & (1 << ysection)) != 0) {
				int extrasecton = info.extraSectionToIndexMap[ysection];
				if (blockindex % 2 == 0) {
					typeID += info.data[info.chunkSectionNumber * 10240 + extrasecton * 2048 + blockindex >> 1] & 0x0F << 8;
				} else {
					typeID += info.data[info.chunkSectionNumber * 10240 + extrasecton * 2048 + blockindex >> 1] >> 4 & 0x0F << 8;
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