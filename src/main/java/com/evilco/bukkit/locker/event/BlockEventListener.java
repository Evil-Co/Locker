package com.evilco.bukkit.locker.event;

import com.evilco.bukkit.locker.LockerPlugin;
import com.evilco.bukkit.locker.ProtectionHandle;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class BlockEventListener implements Listener {

	/**
	 * Stores the parent plugin.
	 */
	protected final LockerPlugin plugin;

	/**
	 * Constructs a new BlockEventListener.
	 * @param plugin
	 */
	public BlockEventListener (LockerPlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Handles block breaks.
	 * @param event
	 */
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBlockBreak (BlockBreakEvent event) {
		// handle signs
		if (event.getBlock ().getType () == Material.SIGN_POST || event.getBlock ().getType () == Material.WALL_SIGN) {
			// get sign
			Sign sign = ((Sign) event.getBlock ().getState ());

			// check for protection
			if (!sign.getLine (0).equalsIgnoreCase (LockerPlugin.PROTECTION_PREFIX)) return;

			// check protection
			for (BlockFace face : LockerPlugin.FACES) {
				Block current = event.getBlock ().getRelative (face);

				// can be protected?
				if (!this.plugin.isProtectable (current)) continue;

				// verify protection contents
				String username = (sign.getLine (2) + sign.getLine (3));
				if ((!username.equalsIgnoreCase (event.getPlayer ().getName ()) || !this.plugin.getPermission ().has (event.getPlayer (), "locker.use")) && !this.plugin.getPermission ().has (event.getPlayer (), "locker.override")) {
					// notify user
					event.getPlayer ().sendMessage (ChatColor.RED + this.plugin.getTranslation ("protection.warning"));

					// cancel event
					event.setCancelled (true);

					// stop execution
					return;
				}

				// notify user
				event.getPlayer ().sendMessage (ChatColor.YELLOW + this.plugin.getTranslation ("protection.remove"));

				// stop execution
				return;
			}

			// stop execution
			return;
		}

		// get protection handle
		ProtectionHandle handle = this.plugin.getProtectionHandle (event.getBlock ());

		// verify
		if (handle == null) return;

		// check permissions
		if ((!handle.isOwner (event.getPlayer ()) || !this.plugin.getPermission ().has (event.getPlayer (), "locker.use")) && !this.plugin.getPermission ().has (event.getPlayer (), "locker.override")) {
			// notify user
			event.getPlayer ().sendMessage (ChatColor.RED + this.plugin.getTranslation ("protection.warning"));

			// cancel event
			event.setCancelled (true);

			// stop execution
			return;
		}

		// break descriptor
		event.getBlock ().getWorld ().getBlockAt (handle.getDescriptorLocation ()).breakNaturally ();

		// notify user
		event.getPlayer ().sendMessage (ChatColor.YELLOW + this.plugin.getTranslation ("protection.remove"));
	}

	/**
	 * Handles block placement.
	 * @param event
	 */
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBlockPlace (BlockPlaceEvent event) {
		// verify type
		if (event.getBlockPlaced ().getType () != Material.WALL_SIGN && event.getBlockPlaced ().getType () != Material.SIGN_POST) return;

		// verify blocks around
		for (BlockFace face : LockerPlugin.FACES) {
			// check block
			ProtectionHandle handle = this.plugin.getProtectionHandle (event.getBlockPlaced ().getRelative (face));

			// verify
			if (handle != null) {
				// cancel event
				event.setCancelled (true);

				// send warning
				event.getPlayer ().sendMessage (ChatColor.RED + this.plugin.getTranslation ("protection.placeLimitation"));

				// break loop
				break;
			}
		}
	}

	/**
	 * Handles item movements.
	 * @param event
	 */
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInventoryMoveItem (InventoryMoveItemEvent event) {
		// skip all holders without a BlockState
		if (!(event.getSource ().getHolder () instanceof BlockState) && !(event.getSource ().getHolder () instanceof DoubleChest)) return;
		if (event.getDestination ().getHolder () instanceof Player) return;

		// get block from state
		Block block = (event.getSource ().getHolder () instanceof DoubleChest ? ((BlockState) ((DoubleChest) event.getSource ().getHolder()).getLeftSide ()) : ((BlockState) event.getSource ().getHolder ())).getBlock ();

		// check protection
		ProtectionHandle handle = this.plugin.getProtectionHandle (block);

		// verify handle
		if (handle == null) return;

		// check destination
		if (event.getDestination ().getHolder () instanceof BlockState) {
			// get destination handle
			ProtectionHandle destinationHandle = this.plugin.getProtectionHandle (((BlockState) event.getDestination ().getHolder ()).getBlock ());

			// cancel unauthorized movements
			if (destinationHandle == null || !destinationHandle.getOwnerName ().equalsIgnoreCase (handle.getOwnerName ())) event.setCancelled (true);
		} else
			event.setCancelled (true);
	}

	/**
	 * Handles sign changes.
	 * @param event
	 */
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onSignChange (SignChangeEvent event) {
		// check contents
		if (!event.getLine (0).equalsIgnoreCase (LockerPlugin.PROTECTION_PREFIX)) return;

		// verify permissions
		if (!this.plugin.getPermission ().has (event.getPlayer (), "locker.use")) {
			// set new text
			event.setLine (0, "");
			event.setLine (1, ChatColor.RED + "[Locker]");
			event.setLine (2, ChatColor.RED + this.plugin.getTranslation ("protection.invalid"));
			event.setLine (3, "");

			// notify user
			event.getPlayer ().sendMessage (ChatColor.RED + this.plugin.getTranslation ("protection.permissions"));

			// stop execution
			return;
		}

		// fix if empty
		if (event.getLine (1).isEmpty () && event.getLine (2).isEmpty ()) {
			// set new text
			event.setLine (0, "");
			event.setLine (1, ChatColor.RED + "[Locker]");
			event.setLine (2, ChatColor.RED + this.plugin.getTranslation ("protection.invalid"));
			event.setLine (3, "");

			// stop execution
			return;
		}

		// shift lines
		if (!event.getLine (1).isEmpty () && event.getLine (2).isEmpty ()) {
			// set new text
			event.setLine (3, event.getLine (2));
			event.setLine (2, event.getLine (1));
			event.setLine (1, "");
		}

		// build username
		String username = (event.getLine (2) + event.getLine (3));

		// verify username
		if (!username.equalsIgnoreCase (event.getPlayer ().getName ()) && !this.plugin.getPermission ().has (event.getPlayer (), "locker.override")) {
			// set new text
			event.setLine (0, "");
			event.setLine (1, ChatColor.RED + "[Locker]");
			event.setLine (2, ChatColor.RED + this.plugin.getTranslation ("protection.invalid"));
			event.setLine (3, "");

			// stop execution
			return;
		}
	}
}