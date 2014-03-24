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

package com.lishid.orebfuscator.internal.v1_6_R3;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.lishid.orebfuscator.internal.IChangeBlockPacket;

public class ChangeBlockPacket implements IChangeBlockPacket {

	@SuppressWarnings("deprecation")
	@Override
	public void notify(Block block) {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		PacketContainer updatePacket = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
		StructureModifier<Integer> integers = updatePacket.getIntegers();
		integers.write(0, block.getX());
		integers.write(1, block.getY());
		integers.write(2, block.getZ());
		integers.write(3, block.getTypeId());
		integers.write(4, (int) block.getData());
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getWorld().getName().equals(block.getWorld().getName())) {
				Chunk playerChunk = player.getLocation().getChunk();
				Chunk blockChunk = block.getChunk();
				int vd = Bukkit.getViewDistance();
				if (
					Math.abs(playerChunk.getX() - blockChunk.getX()) <= vd &&
					Math.abs(playerChunk.getZ() - blockChunk.getZ()) <= vd 
				) {
					try {
						manager.sendServerPacket(player, updatePacket, true);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}