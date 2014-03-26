package com.lishid.orebfuscator.listeners;

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
				public void onPacketSending(PacketEvent event) {
					List<Integer> ints = event.getPacket().getIntegers().getValues();
					if (OrebfuscatorConfig.isBlockTransparent(ints.get(3))) {
						Player player = event.getPlayer();
						World world = player.getWorld();
						Block block = world.getBlockAt(ints.get(0), ints.get(1), ints.get(2));
						BlockUpdate.Update(player, block);
					}
				}
			}
		);
	}

}
