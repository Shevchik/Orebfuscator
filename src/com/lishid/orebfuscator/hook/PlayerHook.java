package com.lishid.orebfuscator.hook;

import java.util.List;

import net.minecraft.server.v1_6_R3.NetworkManager;
import net.minecraft.server.v1_6_R3.Packet;

import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.lishid.orebfuscator.internal.Fields;
import com.lishid.orebfuscator.utils.ReflectionHelper;

public class PlayerHook implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent event) {
		hookPlayer(event.getPlayer());
	}

	@SuppressWarnings("unchecked")
	public void hookPlayer(Player player) {
		CraftPlayer cplayer = (CraftPlayer) player;
		NetworkManager nm = (NetworkManager) cplayer.getHandle().playerConnection.networkManager;
		List<?> high = new AsyncAddArrayList(player, nm, (List<Packet>) ReflectionHelper.getPrivateField(nm, Fields.NetworkManagerFields.getHighPriorityQueueFieldName()));
		ReflectionHelper.setPrivateField(nm, Fields.NetworkManagerFields.getHighPriorityQueueFieldName(), high);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		cleanupPlayer(event.getPlayer());
	}

	public void cleanupPlayer(Player player) {
		CraftPlayer cplayer = (CraftPlayer) player;
		NetworkManager nm = (NetworkManager) cplayer.getHandle().playerConnection.networkManager;
		((AsyncAddArrayList) ReflectionHelper.getPrivateField(nm, Fields.NetworkManagerFields.getHighPriorityQueueFieldName())).cleanup();
	}

}
