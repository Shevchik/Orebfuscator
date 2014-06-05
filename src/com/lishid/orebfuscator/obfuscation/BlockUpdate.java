package com.lishid.orebfuscator.obfuscation;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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


	private static ExecutorService blockUpdateExecutor;
	public static void initExecutor() {
		blockUpdateExecutor = Executors.newFixedThreadPool(OrebfuscatorConfig.blockUpdateThreads); 
	}

	public static void update(final Packet53 packet, final Player player) {
		blockUpdateExecutor.execute(
			new Runnable() {
				@Override
				public void run() {
					Block block = player.getWorld().getBlockAt(packet.getBlockX(), packet.getBlockY(), packet.getBlockZ());
					if (!OrebfuscatorConfig.isBlockTransparent(packet.getNewMaterial())) {
						return;
					}
					HashSet<Block> updateBlocks = GetAjacentBlocks(player.getWorld(), new HashSet<Block>(20), block, OrebfuscatorConfig.UpdateRadius);
					scheduleSyncUpdate(updateBlocks);
				}
			}
		);
	}

	public static void update(final Packet52 packet, final Player player) {
		blockUpdateExecutor.execute(
			new Runnable() {
				@Override
				public void run() {
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
					scheduleSyncUpdate(updateBlocks);
				}
			}
		);
	}

	private static void scheduleSyncUpdate(final Collection<Block> blocks) {
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
