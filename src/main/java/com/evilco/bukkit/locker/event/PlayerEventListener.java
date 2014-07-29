package com.evilco.bukkit.locker.event;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.evilco.bukkit.locker.LockerPlugin;
import com.evilco.bukkit.locker.ProtectionHandle;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class PlayerEventListener implements Listener {

	/**
	 * Stores the parent plugin instance.
	 */
	protected final LockerPlugin plugin;

	/**
	 * Constructs a new PlayerEventListener.
	 * @param plugin
	 */
	public PlayerEventListener (LockerPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Handles player interactions.
	 * @param event
	 * @throws Exception
	 */
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerInteract (PlayerInteractEvent event) throws Exception {
		// verify
		if (!event.hasBlock () || event.getAction () != Action.RIGHT_CLICK_BLOCK) return;

		// check type
		if (!(event.getClickedBlock ().getState () instanceof InventoryHolder)) return;

		// get locker handle
		ProtectionHandle handle = this.plugin.getProtectionHandle (event.getClickedBlock ());

		// verify sign block
		if (handle == null) return;

		// verify access
		if (!handle.hasAccessPermissions (event.getPlayer ())) {
			// create update packet
			PacketContainer packetContainer = this.plugin.getProtocolManager ().createPacket (PacketType.Play.Server.UPDATE_SIGN);

			// update contents
			packetContainer.getIntegers ()
				.write (0, handle.getDescriptorLocation ().getBlockX ())
				.write (1, handle.getDescriptorLocation ().getBlockY ())
				.write (2, handle.getDescriptorLocation ().getBlockZ ());

			packetContainer.getStringArrays ()
				.write (0, new String[] {
					LockerPlugin.PROTECTION_PREFIX,
					ChatColor.RED + this.plugin.getTranslation ("protection.accessDenied"),
					handle.getOwnerName (),
				});

			// send packet
			this.plugin.getProtocolManager ().sendServerPacket (event.getPlayer (), packetContainer);

			// cancel event
			event.setUseInteractedBlock (Event.Result.DENY);
			event.setUseItemInHand (Event.Result.DENY);
		} else
			handle.migrate (this.plugin, event.getPlayer ());
	}
}