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

package com.lishid.orebfuscator;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import com.lishid.orebfuscator.hook.ProtocolLibHook;
import com.lishid.orebfuscator.internal.InternalAccessor;
import com.lishid.orebfuscator.listeners.BlockChangeListener;
import com.lishid.orebfuscator.listeners.ProcessingThreads;
import com.lishid.orebfuscator.obfuscation.randompool.RandomIntPool;

/**
 * Orebfuscator Anti X-RAY
 *
 * @author lishid
 */
public class Orebfuscator extends JavaPlugin {

	public static final Logger logger = Logger.getLogger("Minecraft.OFC");
	public static Orebfuscator instance;

	@Override
	public void onEnable() {
		// Assign static instance
		instance = this;

		// Set NMS version
		InternalAccessor.Initialize(getServer());

		// Load configurations
		OrebfuscatorConfig.load();

		// Start block processing Threads
		ProcessingThreads.initialize();
		ProcessingThreads.instance.startThreads();

		// Start random generator pool
		RandomIntPool.initialize();
		RandomIntPool.getInstance().start();

		// Hook block change packet
		new BlockChangeListener().register(this);

		// Hook chunk data packets
		new ProtocolLibHook().register(this);
	}

	@Override
	public void onDisable() {
		if (ProcessingThreads.instance != null) {
			ProcessingThreads.instance.stopThreads();
		}
		if (RandomIntPool.getInstance() != null) {
			RandomIntPool.getInstance().stop();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return OrebfuscatorCommandExecutor.onCommand(sender, command, label, args);
	}

	/**
	 * Log an information
	 */
	public static void log(String text) {
		logger.info("[OFC] " + text);
	}

	/**
	 * Log an error
	 */
	public static void log(Throwable e) {
		logger.severe("[OFC] " + e.toString());
		e.printStackTrace();
	}

	/**
	 * Send a message to a player
	 */
	public static void message(CommandSender target, String message) {
		target.sendMessage(ChatColor.AQUA + "[OFC] " + message);
	}

}