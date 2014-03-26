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

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.lishid.orebfuscator.OrebfuscatorConfig;

public class BlockUpdate {
	
	private static ProtocolManager manager;
	private static ProtocolManager getProtocolManager() {
		if (manager == null) {
			manager = ProtocolLibrary.getProtocolManager();
		}
		return manager;
	}

	public static void Update(Player player, Block block) {
		HashSet<Block> updateBlocks = GetAjacentBlocks(block.getWorld(), new HashSet<Block>(20), block, OrebfuscatorConfig.UpdateRadius);
		
		sendBlockUpdates(player, updateBlocks);
	}

	public static HashSet<Block> GetAjacentBlocks(World world, HashSet<Block> allBlocks, Block block, int countdown) {
		if (block == null) {
			return allBlocks;
		}

		AddBlockCheck(allBlocks, block);

		if (countdown == 0) {
			return allBlocks;
		}

		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX() + 1, block.getY(), block.getZ()), countdown - 1);
		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX() - 1, block.getY(), block.getZ()), countdown - 1);
		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() + 1, block.getZ()), countdown - 1);
		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() - 1, block.getZ()), countdown - 1);
		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() + 1), countdown - 1);
		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() - 1), countdown - 1);

		return allBlocks;
	}

	@SuppressWarnings("deprecation")
	public static void AddBlockCheck(HashSet<Block> allBlocks, Block block) {
		if ((OrebfuscatorConfig.isObfuscated(block.getTypeId(), block.getWorld().getEnvironment() == Environment.NETHER))) {
			allBlocks.add(block);
		}
	}

	@SuppressWarnings("deprecation")
	private static void sendBlockUpdates(Player player, HashSet<Block> blocks) {
		for (Block block : blocks) {
			PacketContainer packet = getProtocolManager().createPacket(PacketType.Play.Server.BLOCK_CHANGE);
			StructureModifier<Integer> ints = packet.getIntegers();
			ints.write(0, block.getX());
			ints.write(1, block.getY());
			ints.write(2, block.getZ());
			ints.write(3, block.getTypeId());
			ints.write(4, (int) block.getData());
			try {
				getProtocolManager().sendServerPacket(player, packet, false);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

}
