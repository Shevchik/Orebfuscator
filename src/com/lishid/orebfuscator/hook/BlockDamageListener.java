package com.lishid.orebfuscator.hook;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.obfuscation.BlockUpdate;

public class BlockDamageListener implements Listener {

	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		if (OrebfuscatorConfig.UpdateOnDamage) {
			BlockUpdate.update(event.getBlock(), event.getPlayer());
		}
	}

}
