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
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.obfuscation.BlockUpdate;

public class OrebfuscatorBlockListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		BlockUpdate.Update(event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDamage(BlockDamageEvent event) {
		if (!OrebfuscatorConfig.UpdateOnDamage) {
			return;
		}

		if (!BlockUpdate.needsUpdate(event.getBlock())) {
			return;
		}

		BlockUpdate.Update(event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.getBlock().getType() != Material.SAND && event.getBlock().getType() != Material.GRAVEL) {
			return;
		}

		if (!applyphysics(event.getBlock())) {
			return;
		}

		BlockUpdate.Update(event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		for (Block b : event.getBlocks()) {
			BlockUpdate.Update(b);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if (!event.isSticky()) {
			return;
		}

		BlockUpdate.Update(event.getBlock());
	}

	@SuppressWarnings("deprecation")
	private boolean applyphysics(Block block) {
		int blockID = block.getRelative(0, -1, 0).getTypeId();

		int air = Material.AIR.getId();
		int fire = Material.FIRE.getId();
		int water = Material.WATER.getId();
		int water2 = Material.STATIONARY_WATER.getId();
		int lava = Material.LAVA.getId();
		int lava2 = Material.STATIONARY_LAVA.getId();

		return (blockID == air || blockID == fire || blockID == water || blockID == water2 || blockID == lava || blockID == lava2);
	}
}