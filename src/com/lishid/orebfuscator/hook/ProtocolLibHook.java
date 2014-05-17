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
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.lishid.orebfuscator.obfuscation.Calculations;

public class ProtocolLibHook {

	private ExecutorService executors = Executors.newFixedThreadPool(4);

	public void register(Plugin plugin) {
		ProtocolLibrary.getProtocolManager().addPacketListener(
			new PacketAdapter(
				PacketAdapter.params(plugin, PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.MAP_CHUNK_BULK)
			) {
				@Override
				public void onPacketSending(final PacketEvent event) {
					try {
						final Thread thread = getPlayerConnectionWriteThread(event.getPlayer());
						suspendThread(thread);
						executors.execute(
							new Runnable() {
								@Override
								public void run() {
									Calculations.Obfuscate(event.getPacket(), event.getPlayer());
									resumeThread(thread);
								}
							}
						);
					} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
					}
				}
			}
		);
	}

	private Thread getPlayerConnectionWriteThread(Player player) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		CraftPlayer cplayer = (CraftPlayer) player;
		NetworkManager nm = (NetworkManager) cplayer.getHandle().playerConnection.networkManager;
		Field f = nm.getClass().getDeclaredField("field_74483_t");
		f.setAccessible(true);
		return (Thread) f.get(nm);
	}

	private HashMap<Thread, AtomicInteger> suspendcount = new HashMap<Thread, AtomicInteger>();

	@SuppressWarnings("deprecation")
	private synchronized void suspendThread(Thread thread) {
		if (!suspendcount.containsKey(thread)) {
			suspendcount.put(thread, new AtomicInteger());
		}
		suspendcount.get(thread).incrementAndGet();
		thread.suspend();
	}

	@SuppressWarnings("deprecation")
	private synchronized void resumeThread(Thread thread) {
		if (suspendcount.get(thread).decrementAndGet() == 0) {
			thread.resume();
			suspendcount.remove(thread);
		}
	}

}