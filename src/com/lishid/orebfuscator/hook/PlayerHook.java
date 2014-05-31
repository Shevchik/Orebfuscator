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

import com.lishid.orebfuscator.utils.ReflectionHelper;

public class PlayerHook implements Listener {

	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		CraftPlayer cplayer = (CraftPlayer) player;
		NetworkManager nm = (NetworkManager) cplayer.getHandle().playerConnection.networkManager;
		List<?> high = new AsyncAddArrayList(player, (List<Packet>) ReflectionHelper.getPrivateField(nm, getHighPriorityQueueFieldName()));
		ReflectionHelper.setPrivateField(nm, getHighPriorityQueueFieldName(), high);
		List<?> low = new AsyncAddArrayList(player, (List<Packet>) ReflectionHelper.getPrivateField(nm, getLowPriorityQueueFieldName()));
		ReflectionHelper.setPrivateField(nm, getLowPriorityQueueFieldName(), low);
	}

	@EventHandler(priority = EventPriority.MONITOR)	
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		CraftPlayer cplayer = (CraftPlayer) player;
		NetworkManager nm = (NetworkManager) cplayer.getHandle().playerConnection.networkManager;
		((AsyncAddArrayList) ReflectionHelper.getPrivateField(nm, getHighPriorityQueueFieldName())).stop();
		((AsyncAddArrayList) ReflectionHelper.getPrivateField(nm, getLowPriorityQueueFieldName())).stop();
	}

	private static String getHighPriorityQueueFieldName() {
		return "field_74487_p";
	}

	private static String getLowPriorityQueueFieldName() {
		return "field_74486_q";
	}

}
