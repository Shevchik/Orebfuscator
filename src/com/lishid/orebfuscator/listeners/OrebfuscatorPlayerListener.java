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

package com.lishid.orebfuscator.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.obfuscation.BlockUpdate;

public class OrebfuscatorPlayerListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (OrebfuscatorConfig.LoginNotification) {
			if (OrebfuscatorConfig.playerBypassOp(player)) {
				Orebfuscator.message(player, "Orebfuscator bypassed because you are OP.");
			}
			else if (OrebfuscatorConfig.playerBypassPerms(player)) {
				Orebfuscator.message(player, "Orebfuscator bypassed because you have permission.");
			}
		}
	}

}
