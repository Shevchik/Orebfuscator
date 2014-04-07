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

import java.util.zip.Deflater;

import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.IPacket51;
import com.lishid.orebfuscator.internal.IPacket56;
import com.lishid.orebfuscator.internal.InternalAccessor;

public class Calculations {

	public static final ThreadLocal<Deflater> localDeflater = new ThreadLocal<Deflater>() {
		@Override
		protected Deflater initialValue() {
			return new Deflater(OrebfuscatorConfig.CompressionLevel);
		}
	};

	public static void Obfuscate(PacketContainer container, Player player) {
		if (container.getType().equals(PacketType.Play.Server.MAP_CHUNK)) {
			IPacket51 packet = InternalAccessor.Instance.newPacket51();
			packet.setPacket(container.getHandle());
			Calculations.Obfuscate(packet, player);
		} else if (container.getType().equals(PacketType.Play.Server.MAP_CHUNK_BULK)) {
			IPacket56 packet = InternalAccessor.Instance.newPacket56();
			packet.setPacket(container.getHandle());
			Calculations.Obfuscate(packet, player);
		}
	}

	private static void Obfuscate(IPacket56 packet, Player player) {
		ChunkInfo[] infos = getInfo(packet, player);

		for (int chunkNum = 0; chunkNum < infos.length; chunkNum++) {
			ChunkInfo info = infos[chunkNum];
			ComputeChunkInfoAndObfuscate(info);
		}

		Deflater deflater = localDeflater.get();
		packet.compress(deflater);
	}

	private static void Obfuscate(IPacket51 packet, Player player) {
		ChunkInfo info = getInfo(packet, player);

		if (info.chunkMask == 0 && info.extraMask == 0) {
			return;
		}

		ComputeChunkInfoAndObfuscate(info);

		Deflater deflater = localDeflater.get();
		packet.compress(deflater);
	}

	private static ChunkInfo[] getInfo(IPacket56 packet, Player player) {
		ChunkInfo[] infos = new ChunkInfo[packet.getPacketChunkNumber()];

		int[] x = packet.getX();
		int[] z = packet.getZ();

		byte[][] inflatedBuffers = packet.getInflatedBuffers();

		int[] chunkMask = packet.getChunkMask();
		int[] extraMask = packet.getExtraMask();

		// Create an info objects
		int writeindex = 0;
		for (int chunkNum = 0; chunkNum < inflatedBuffers.length; chunkNum++) {
			ChunkInfo info = new ChunkInfo();
			infos[chunkNum] = info;
			info.world = player.getWorld();
			info.chunkX = x[chunkNum];
			info.chunkZ = z[chunkNum];
			info.chunkMask = chunkMask[chunkNum];
			info.extraMask = extraMask[chunkNum];
			info.data = inflatedBuffers[chunkNum];
			info.finaldata = packet.getOutputBuffer();
			info.finaldataWriteIndex = writeindex;
			writeindex += inflatedBuffers[chunkNum].length;
		}

		return infos;
	}

	private static ChunkInfo getInfo(IPacket51 packet, Player player) {
		// Create an info objects
		ChunkInfo info = new ChunkInfo();
		info.world = player.getWorld();
		info.chunkX = packet.getX();
		info.chunkZ = packet.getZ();
		info.chunkMask = packet.getChunkMask();
		info.extraMask = packet.getExtraMask();
		info.data = packet.getBuffer();
		info.finaldata = packet.getBuffer();
		info.finaldataWriteIndex = 0;
		return info;
	}

	private static void ComputeChunkInfoAndObfuscate(ChunkInfo info) {
		// Compute chunk number
        for (int i = 0; i < 16; i++) {
            if ((info.chunkMask & 1 << i) > 0) {
                info.chunkSectionToIndexMap[i] = info.chunkSectionNumber;
                info.chunkSectionNumber++;
            }
            if ((info.extraMask & 1 << i) > 0) {
                info.extraSectionToIndexMap[i] = info.extraSectionNumber;
                info.extraSectionNumber++;
            }
        }

		if (4096 * info.chunkSectionNumber > info.data.length) {
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
			if ((info.chunkMask & 1 << i) != 0) {
				boolean usesExtra = ((info.extraMask & 1 << i) != 0);

				for (int y = 0; y < 16; y++) {
					int blockY = (i << 4) + y;
					for (int z = 0; z < 16; z++) {
						for (int x = 0; x < 16; x++) {

							int typeID = info.data[currentTypeIndex];
							if (typeID < 0) {
								typeID += 256;
							}
							if (usesExtra) {
								byte extra = 0;
								if (currentTypeIndex % 2 == 0) {
									extra = (byte) (info.data[addExtendedIndex + currentExtendedIndex] & 0x0F);
								} else {
									extra = (byte) (info.data[addExtendedIndex + currentExtendedIndex] >> 4);
								}
								if (extra < 0) {
									extra += 16;
								}
								typeID += extra << 8;
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
								info.finaldata[info.finaldataWriteIndex + currentTypeIndex] = (byte) newBlockID;
								if (usesExtra) {
									byte extra = (byte) (newBlockID >> 8);
									if (currentTypeIndex % 2 == 0) {
										info.finaldata[info.finaldataWriteIndex + addExtendedIndex + currentExtendedIndex] = extra;
									} else {
										info.finaldata[info.finaldataWriteIndex + addExtendedIndex + currentExtendedIndex] += (byte) (extra << 4);
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

		int id = 1;

		boolean foundID = false;
        if ((info.chunkMask & (1 << (y >> 4))) > 0 && x >> 4 == info.chunkX && z >> 4 == info.chunkZ) {
            int section = info.chunkSectionToIndexMap[y >> 4];
            int cX = ((x % 16) < 0) ? (x % 16 + 16) : (x % 16);
            int cZ = ((z % 16) < 0) ? (z % 16 + 16) : (z % 16);

            int blockindex = (y % 16 << 8) + (cZ << 4) + cX;

            id = info.data[section * 4096 + blockindex];
            if (id < 0) {
            	id +=256;
            }
            if ((info.extraMask & (1 << (y >> 4))) > 0) {
            	int extrasecton = info.extraSectionToIndexMap[y >> 4];
				byte extra = 0;
				if (blockindex % 2 == 0) {
					extra = (byte) (info.data[info.chunkSectionNumber * 10240 + extrasecton * 2048 + blockindex / 2] & 0x0F);
				} else {
					extra = (byte) (info.data[info.chunkSectionNumber * 10240 + extrasecton * 2048 + blockindex / 2] >> 4);
				}
				if (extra < 0) {
					extra += 16;
				}
				id += extra << 8;
            }
            foundID = true;
        }

		if (!foundID && CalculationsUtil.isChunkLoaded(info.world, x >> 4, z >> 4)) {
			id = info.world.getBlockTypeIdAt(x, y, z);
		}

		if (OrebfuscatorConfig.isBlockTransparent(id)) {
			return true;
		}

		return false;
	}

}