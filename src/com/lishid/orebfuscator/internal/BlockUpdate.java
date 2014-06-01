package com.lishid.orebfuscator.internal;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.obfuscation.CalculationsUtil;

public class BlockUpdate {

	public static void update(Packet53 packet, Player player) {
		Block block = player.getWorld().getBlockAt(packet.getBlockX(), packet.getBlockY(), packet.getBlockZ());
		if (!OrebfuscatorConfig.isBlockTransparent(packet.getNewMaterial())) {
			return;
		}
		HashSet<Block> updateBlocks = GetAjacentBlocks(player.getWorld(), new HashSet<Block>(20), block, OrebfuscatorConfig.UpdateRadius);
		scheduleUpdate(updateBlocks);
	}

	private static void scheduleUpdate(final HashSet<Block> blocks) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(
			Orebfuscator.instance, 
			new Runnable() {
				@Override
				public void run() {
					for (Block ublock : blocks) {
						((CraftWorld) ublock.getWorld()).getHandle().notify(ublock.getX(), ublock.getY(), ublock.getZ());
					}
				}
			}
		);
	}

	public static HashSet<Block> GetAjacentBlocks(World world, HashSet<Block> allBlocks, Block block, int countdown) {
		if (block == null) {
			return allBlocks;
		}

		AddBlockCheck(allBlocks, block);

		if (countdown == 0) {
			return allBlocks;
		}

		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX() + 1, block.getY(), block.getZ()), countdown - 1);
		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX() - 1, block.getY(), block.getZ()), countdown - 1);
		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() + 1, block.getZ()), countdown - 1);
		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() - 1, block.getZ()), countdown - 1);
		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() + 1), countdown - 1);
		GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() - 1), countdown - 1);

		return allBlocks;
	}

	@SuppressWarnings("deprecation")
	public static void AddBlockCheck(HashSet<Block> allBlocks, Block block) {
		if (OrebfuscatorConfig.isObfuscated(block.getTypeId())) {
			allBlocks.add(block);
		}
	}

}
