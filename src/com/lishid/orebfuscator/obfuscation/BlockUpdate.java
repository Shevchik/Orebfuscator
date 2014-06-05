package com.lishid.orebfuscator.obfuscation;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.Packet52;
import com.lishid.orebfuscator.internal.Packet53;
import com.lishid.orebfuscator.internal.WorldServer;
import com.lishid.orebfuscator.utils.BlockChangeArray;
import com.lishid.orebfuscator.utils.BlockChangeArray.BlockChange;

public class BlockUpdate {

	public static void update(Block block, Player player) {
		HashSet<Block> updateBlocks = GetAjacentBlocks(player.getWorld(), new HashSet<Block>(20), block, OrebfuscatorConfig.UpdateRadius);
		for (Block ublock : updateBlocks) {
			WorldServer.notify(ublock);
		}
	}

	public static void update(Packet53 packet, Player player) {
		Block block = player.getWorld().getBlockAt(packet.getBlockX(), packet.getBlockY(), packet.getBlockZ());
		if (!OrebfuscatorConfig.isBlockTransparent(packet.getNewMaterial())) {
			return;
		}
		HashSet<Block> updateBlocks = GetAjacentBlocks(player.getWorld(), new HashSet<Block>(20), block, OrebfuscatorConfig.UpdateRadius);
		scheduleUpdate(updateBlocks);
	}

	public static void update(Packet52 packet, Player player) {
		List<Block> blocks = new LinkedList<Block>();
		int chunkX = packet.getChunkX();
		int chunkZ = packet.getChunkZ();
		World world = player.getWorld();
		BlockChangeArray baarray = new BlockChangeArray(packet.getRecords());
		for (int i = 0; i < baarray.getSize(); i++) {
			BlockChange bc = baarray.getBlockChange(i);
			int x = (chunkX << 4) + bc.getRelativeX();
			int z = (chunkZ << 4) + bc.getRelativeZ();
			Block block = CalculationsUtil.getBlockAt(world, x, bc.getAbsoluteY(), z);
			if (block != null && OrebfuscatorConfig.isBlockTransparent(bc.getBlockID())) {
				blocks.add(block);
			}
		}
		HashSet<Block> updateBlocks = new HashSet<Block>(blocks.size() * 3);
		for (Block block : blocks) {
			updateBlocks.addAll(GetAjacentBlocks(block.getWorld(), new HashSet<Block>(), block, OrebfuscatorConfig.UpdateRadius));
		}
		scheduleUpdate(updateBlocks);
	}

	private static void scheduleUpdate(final Collection<Block> blocks) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(
			Orebfuscator.instance,
			new Runnable() {
				@Override
				public void run() {
					for (Block ublock : blocks) {
						WorldServer.notify(ublock);
					}
				}
			}
		);
	}

	@SuppressWarnings("deprecation")
	public static HashSet<Block> GetAjacentBlocks(World world, HashSet<Block> allBlocks, Block block, int countdown) {
		if (block == null) {
			return allBlocks;
		}

		if (OrebfuscatorConfig.isObfuscated(block.getTypeId())) {
			allBlocks.add(block);
		}

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

}
