package com.evilco.bukkit.locker.event;

import com.evilco.bukkit.locker.LockerPlugin;
import com.evilco.bukkit.locker.ProtectionHandle;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class EntityEventListener implements Listener {

	/**
	 * Stores the parent plugin instance.
	 */
	protected final LockerPlugin plugin;

	/**
	 * Constructs a new EntityEventListener.
	 * @param plugin
	 */
	public EntityEventListener (LockerPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Handles explosions.
	 * @param event
	 */
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onEntityExplode (EntityExplodeEvent event) {
		for (Block block : event.blockList ()) {
			// search for protection
			ProtectionHandle handle = this.plugin.getProtectionHandle (block);

			// remove block & descriptor
			if (handle != null) {
				event.blockList ().remove (block);
				event.blockList ().remove (handle.getDescriptorLocation ().getBlock ());
			}
		}
	}
}