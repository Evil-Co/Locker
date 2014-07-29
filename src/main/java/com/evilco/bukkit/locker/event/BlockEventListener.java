package com.evilco.bukkit.locker.event;

import com.evilco.bukkit.locker.LockerPlugin;
import com.evilco.bukkit.locker.ProtectionHandle;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

import java.util.List;

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
			// get protection handle
			ProtectionHandle handle = this.plugin.getProtectionHandle (event.getBlock ().getState ());

			// verify
			if (handle != null && !handle.isOwner (event.getPlayer ())) {
				// notify user
				event.getPlayer ().sendMessage (ChatColor.RED + this.plugin.getTranslation ("protection.warning"));

				// cancel event
				event.setCancelled (true);

				// stop further execution
				return;
			}
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
			if (destinationHandle == null || destinationHandle.getOwnerID () == null || handle.getOwnerID () == null || !destinationHandle.getOwnerID ().equals (handle.getOwnerID ())) event.setCancelled (true);
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

		// create new handle
		ProtectionHandle handle = new ProtectionHandle (event.getPlayer (), event.getBlock ().getLocation ());

		// update text
		List<String> signText = handle.buildSignText ();

		for (int i = 0; i < signText.size (); i++) {
			event.setLine (i, signText.get (i));
		}
	}
}