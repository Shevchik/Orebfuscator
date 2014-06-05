package com.lishid.orebfuscator.internal;

import java.util.List;

import net.minecraft.server.v1_6_R3.NetworkManager;
import net.minecraft.server.v1_6_R3.Packet;

import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.utils.ReflectionHelper;

public class PlayerInjector {

	@SuppressWarnings("unchecked")
	public static void hookPlayer(Player player) {
		CraftPlayer cplayer = (CraftPlayer) player;
		NetworkManager nm = (NetworkManager) cplayer.getHandle().playerConnection.networkManager;
		List<?> high = new AsyncPacketQueue(player, nm, (List<Packet>) ReflectionHelper.getPrivateField(nm, Fields.NetworkManagerFields.getHighPriorityQueueFieldName()));
		ReflectionHelper.setPrivateField(nm, Fields.NetworkManagerFields.getHighPriorityQueueFieldName(), high);
	}

	public static void cleanupPlayer(Player player) {
		CraftPlayer cplayer = (CraftPlayer) player;
		NetworkManager nm = (NetworkManager) cplayer.getHandle().playerConnection.networkManager;
		((AsyncPacketQueue) ReflectionHelper.getPrivateField(nm, Fields.NetworkManagerFields.getHighPriorityQueueFieldName())).cleanup();
	}

}
