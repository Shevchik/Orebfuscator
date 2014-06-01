package com.lishid.orebfuscator.internal;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;

public class Notify {

	public static void notify(Block ublock) {
		((CraftWorld) ublock.getWorld()).getHandle().notify(ublock.getX(), ublock.getY(), ublock.getZ());
	}

}
