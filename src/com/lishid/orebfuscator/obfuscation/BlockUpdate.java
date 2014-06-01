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
import com.lishid.orebfuscator.internal.Notify;
import com.lishid.orebfuscator.internal.Packet52;
import com.lishid.orebfuscator.internal.Packet53;

public class BlockUpdate {

	public static void update(Packet53 packet, Player player) {
		Block block = player.getWorld().getBlockAt(packet.getBlockX(), packet.getBlockY(), packet.getBlockZ());
		if (!OrebfuscatorConfig.isBlockTransparent(packet.getNewMaterial())) {
			return;
		}
		HashSet<Block> updateBlocks = GetAjacentBlocks(player.getWorld(), new HashSet<Block>(20), block, OrebfuscatorConfig.UpdateRadius);
		scheduleUpdate(updateBlocks);
	}

	@SuppressWarnings("deprecation")
	public static void update(Packet52 packet, Player player) {
		int chunkX = packet.getChunkX();
		int chunkZ = packet.getChunkZ();
		int recordcount = packet.getRecordsCount();
		byte[] data = packet.getRecords();
		World world = player.getWorld();
		List<Block> blocks = new LinkedList<Block>();
		for (int i = 0; i < recordcount; i++) {
			byte xz = data[i * 4];
			int x = (chunkX << 4) + ((xz >> 4) & 0xF);
			int z = (chunkZ << 4) + (xz & 0xF);
			int y = data[(i * 4) + 1] & 0xFF;
			if (world.isChunkLoaded(chunkX, chunkZ)) {
				Block block = world.getBlockAt(x, y, z);
				if (OrebfuscatorConfig.isBlockTransparent(block.getTypeId())) {
					blocks.add(block);
				}
			}
		}
		HashSet<Block> updateBlocks = new HashSet<Block>(40);
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
						Notify.notify(ublock);
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
