package com.lishid.orebfuscator.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.v1_6_R3.Packet52;
import com.lishid.orebfuscator.obfuscation.BlockUpdate;

public class BlockChangeListener {

	private ProtocolManager manager;

	public void register(Plugin plugin) {
		manager = ProtocolLibrary.getProtocolManager();

		manager.addPacketListener(
			new PacketAdapter(
				PacketAdapter
				.params(plugin, PacketType.Play.Server.BLOCK_CHANGE)
				.listenerPriority(ListenerPriority.LOWEST)
			) {
				@Override
				public void onPacketSending(final PacketEvent event) {
					final List<Integer> ints = event.getPacket().getIntegers().getValues();
					if (OrebfuscatorConfig.isBlockTransparent(ints.get(3))) {
						Runnable updateBlock = new Runnable() {
							@Override
							public void run() {
								Player player = event.getPlayer();
								World world = player.getWorld();
								Block block = world.getBlockAt(ints.get(0), ints.get(1), ints.get(2));
								BlockUpdate.Update(player, block);
							}
						};
						ProcessingThreads.instance.submitBlockUpdate(updateBlock);
					}
				}
			}
		);

		manager.addPacketListener(
			new PacketAdapter(
				PacketAdapter
				.params(plugin, PacketType.Play.Server.MULTI_BLOCK_CHANGE)
				.listenerPriority(ListenerPriority.LOWEST)
			) {
				@Override
				public void onPacketSending(final PacketEvent event) {
					Packet52 packet = new Packet52();
					packet.setPacket(event.getPacket().getHandle());
					final int chunkX = packet.getChunkX();
					final int chunkZ = packet.getChunkZ();
					final int recordcount = packet.getRecordsCount();
					final byte[] data = packet.getBuffer();
					Runnable updateBlocks = new Runnable() {
						@SuppressWarnings("deprecation")
						@Override
						public void run() {
							Player player = event.getPlayer();
							World world = player.getWorld();
							List<Block> blocks = new ArrayList<Block>(30);
							for (int i = 0; i < recordcount; i++) {
								byte[] locationdata = new byte[2];
								System.arraycopy(data, i * 4, locationdata, 0, 2);
								byte zx = locationdata[0];
								int x = (chunkX << 4) + (zx >> 4) & 0x0F;
								int z = (chunkZ << 4) + zx & 0x0F;
								int y = locationdata[1] & 0xFF;
								if (world.isChunkLoaded(chunkX, chunkZ)) {
									Block block = world.getBlockAt(x, y, z);
									if (OrebfuscatorConfig.isBlockTransparent(block.getTypeId())) {
										blocks.add(world.getBlockAt(x, y, z));
									}
								}
							}
							BlockUpdate.Update(player, blocks);
						}
					};
					ProcessingThreads.instance.submitBlockUpdate(updateBlocks);
				}
			}
		);
	}

}
