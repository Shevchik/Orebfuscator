package com.lishid.orebfuscator.listeners;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.obfuscation.BlockUpdateOLD;

public class BlockChangeListener {

	public void register(Plugin plugin) {
		/*ProtocolLibrary.getProtocolManager().addPacketListener(
			new PacketAdapter(
				PacketAdapter
				.params(plugin, PacketType.Play.Server.MULTI_BLOCK_CHANGE)
				.listenerPriority(ListenerPriority.LOWEST)
			) {
				@Override
				public void onPacketSending(final PacketEvent event) {
					final int chunkX = event.getPacket().getIntegers().read(0);
					final int chunkZ = event.getPacket().getIntegers().read(1);
					final int recordcount = event.getPacket().getIntegers().read(2);
					final byte[] data = event.getPacket().getByteArrays().read(0);
					Runnable updateBlocks = new Runnable() {
						@SuppressWarnings("deprecation")
						@Override
						public void run() {
							Player player = event.getPlayer();
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
							BlockUpdateOLD.Update(player, blocks);
						}
					};
					ProcessingThreads.instance.submitBlockUpdate(updateBlocks);
				}
			}
		);*/
	}

}
