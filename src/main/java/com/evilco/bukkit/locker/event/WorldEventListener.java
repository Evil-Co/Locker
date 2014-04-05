package com.evilco.bukkit.locker.event;

import com.evilco.bukkit.locker.LockerPlugin;
import com.evilco.bukkit.locker.ProtectionHandle;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class WorldEventListener implements Listener {

	/**
	 * Stores the parent plugin instance.
	 */
	protected final LockerPlugin plugin;

	/**
	 * Constructs a new WorldEventListener.
	 * @param plugin
	 */
	public WorldEventListener (LockerPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Handles growing structures.
	 * @param event
	 */
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onStructureGrow (StructureGrowEvent event) {
		for (BlockState block : event.getBlocks ()) {
			// check block
			ProtectionHandle handle = this.plugin.getProtectionHandle (block.getBlock ());

			// remove blocks
			if (handle != null) event.setCancelled (true);
		}
	}
}