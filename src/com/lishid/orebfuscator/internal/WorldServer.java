package com.lishid.orebfuscator.internal;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;

public class WorldServer {

	public static void notify(Block ublock) {
		((CraftWorld) ublock.getWorld()).getHandle().notify(ublock.getX(), ublock.getY(), ublock.getZ());
	}

	public static boolean hasLight(World world) {
		return !((CraftWorld) world).getHandle().worldProvider.g;
	}

}
