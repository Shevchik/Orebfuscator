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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.server.v1_6_R3.NetworkManager;

import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.lishid.orebfuscator.obfuscation.Calculations;

public class ProtocolLibHook {

	private ProtocolManager manager;

	private ExecutorService executors = Executors.newFixedThreadPool(4);

	private HashMap<String, AtomicInteger> suspendcount = new HashMap<String, AtomicInteger>();

	public void register(Plugin plugin) {
		manager = ProtocolLibrary.getProtocolManager();

		manager.addPacketListener(
			new PacketAdapter(
				PacketAdapter.params(plugin, PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.MAP_CHUNK_BULK)
			) {
				@SuppressWarnings("deprecation")
				@Override
				public void onPacketSending(final PacketEvent event) {
					String playername = event.getPlayer().getName();
					if (!suspendcount.containsKey(playername)) {
						suspendcount.put(playername, new AtomicInteger());
					}
					try {
						NetworkManager nm = (NetworkManager) CraftPlayer.class.cast(event.getPlayer()).getHandle().playerConnection.networkManager;
						Field f = nm.getClass().getDeclaredField("field_74483_t");
						f.setAccessible(true);
						final Thread thread = (Thread) f.get(nm);
						final AtomicInteger atomic = suspendcount.get(playername);
						thread.suspend();
						atomic.getAndIncrement();
						executors.execute(
							new Runnable() {
								@Override
								public void run() {
									Calculations.Obfuscate(event.getPacket(), event.getPlayer());
									atomic.decrementAndGet();
									if (atomic.get() == 0) {
										thread.resume();
									}
								}
							}
						);
					} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
					}
				}
			}
		);
	}

}
