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

package com.lishid.orebfuscator.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;

public class OrebfuscatorCommandExecutor {

	public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if ((sender instanceof Player) && !sender.hasPermission("Orebfuscator.admin")) {
			Orebfuscator.message(sender, "You do not have permissions.");
			return true;
		}

		if (args.length <= 0) {
			return false;
		}

		if (args[0].equalsIgnoreCase("engine") && args.length > 1) {
			int engine = OrebfuscatorConfig.EngineMode;
			try {
				engine = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				Orebfuscator.message(sender, args[1] + " is not a number!");
				return true;
			}
			if (engine != 1 && engine != 2) {
				Orebfuscator.message(sender, args[1] + " is not a valid EngineMode!");
				return true;
			}
			else {
				OrebfuscatorConfig.setEngineMode(engine);
				Orebfuscator.message(sender, "Engine set to: " + engine);
				return true;
			}
		}

		else if (args[0].equalsIgnoreCase("updateradius") && args.length > 1) {
			int radius = OrebfuscatorConfig.UpdateRadius;
			try {
				radius = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				Orebfuscator.message(sender, args[1] + " is not a number!");
				return true;
			}
			OrebfuscatorConfig.setUpdateRadius(radius);
			Orebfuscator.message(sender, "UpdateRadius set to: " + OrebfuscatorConfig.UpdateRadius);
			return true;
		}

		else if (args[0].equalsIgnoreCase("status")) {
			Orebfuscator.message(sender, "Orebfuscator " + Orebfuscator.instance.getDescription().getVersion());
			Orebfuscator.message(sender, "EngineMode: " + OrebfuscatorConfig.EngineMode);

			Orebfuscator.message(sender, "Update Radius: " + OrebfuscatorConfig.UpdateRadius);

			String disabledWorlds = OrebfuscatorConfig.getDisabledWorlds();
			Orebfuscator.message(sender, "Disabled worlds: " + (disabledWorlds.equals("") ? "None" : disabledWorlds));
		}

		return true;
	}
}