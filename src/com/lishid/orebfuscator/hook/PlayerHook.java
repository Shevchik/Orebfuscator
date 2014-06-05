package com.lishid.orebfuscator.hook;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.internal.PlayerInjector;

public class PlayerHook implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(final PlayerJoinEvent event) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Orebfuscator.instance, 
			new Runnable() {
				@Override
				public void run() {
					if (event.getPlayer().isOnline()) {
						PlayerInjector.hookPlayer(event.getPlayer());
					}
				}
			}
		);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		PlayerInjector.cleanupPlayer(event.getPlayer());
	}

}
