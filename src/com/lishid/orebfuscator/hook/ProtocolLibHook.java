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

package com.lishid.orebfuscator.hook;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.obfuscation.ProcessingThreads;

public class ProtocolLibHook {

	private ProtocolManager manager;

	public void register(Plugin plugin) {
		manager = ProtocolLibrary.getProtocolManager();

		manager.addPacketListener(
			new PacketAdapter(plugin, PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.MAP_CHUNK_BULK) {
				@Override
				public void onPacketSending(PacketEvent event) {
					event.setCancelled(true);
					final PacketContainer packet = event.getPacket();
					final Player player = event.getPlayer();
					Runnable processChunk = new Runnable() {
						@Override
						public void run() {
							Calculations.Obfuscate(packet, player);
							try {
								manager.sendServerPacket(player, packet, false);
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						}			
					};
					ProcessingThreads.instance.submitChunkObfuscate(processChunk);
				}
			}
		);
	}

}
