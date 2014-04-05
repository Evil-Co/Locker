package com.evilco.bukkit.locker;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.evilco.bukkit.locker.event.BlockEventListener;
import com.evilco.bukkit.locker.event.EntityEventListener;
import com.evilco.bukkit.locker.event.PlayerEventListener;
import com.evilco.bukkit.locker.event.WorldEventListener;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class LockerPlugin extends JavaPlugin {

	/**
	 * Defines the protection prefix.
	 */
	public static final String PROTECTION_PREFIX = "[Locker]";

	/**
	 * Defines a list of faces searched for signs.
	 */
	public static final ImmutableList<BlockFace> FACES = new ImmutableList.Builder<BlockFace> ()
			.add (BlockFace.UP)
			.add (BlockFace.DOWN)
			.add (BlockFace.NORTH)
			.add (BlockFace.EAST)
			.add (BlockFace.SOUTH)
			.add (BlockFace.WEST)
		.build ();

	/**
	 * Caches the permission implementation.
	 */
	protected Permission permission = null;

	/**
	 * Caches the protocol manager instance.
	 */
	protected ProtocolManager protocolManager = null;

	/**
	 * Stores the currently selected plugin translation.
	 */
	protected ResourceBundle translation = null;

	/**
	 * Returns the permission implementaiton.
	 * @return
	 */
	public Permission getPermission () {
		return this.permission;
	}

	/**
	 * Searches for a protection handle.
	 * @param block
	 * @return
	 */
	public ProtectionHandle getProtectionHandle (Block block) {
		// verify
		Preconditions.checkNotNull (block, "block");

		// get block state
		BlockState state = block.getState ();

		// verify
		if (state == null || !(state instanceof InventoryHolder)) return null;

		// search sign
		Block current = null;

		for (BlockFace face : FACES) {
			// get block
			current = block.getRelative (face);

			// check
			if (current.getType () == Material.WALL_SIGN || current.getType () == Material.SIGN_POST) {
				// get sign
				Sign sign = ((Sign) current.getState ());

				// verify sign
				if (sign.getLine (0).equalsIgnoreCase (PROTECTION_PREFIX)) return (new ProtectionHandle ((sign.getLine (2) + sign.getLine (3)), current.getLocation ()));
			}
		}

		// no protection found
		return null;
	}

	/**
	 * Returns the current protocol manager instance.
	 * @return
	 */
	public ProtocolManager getProtocolManager () {
		return this.protocolManager;
	}

	/**
	 * Checks whether a specific block can be protected.
	 * @param block
	 * @return
	 */
	public boolean isProtectable (Block block) {
		// verify
		Preconditions.checkNotNull (block, "block");

		// get block state
		BlockState state = block.getState ();

		// verify
		return (state != null && state instanceof InventoryHolder);
	}

	/**
	 * Returns a translation string.
	 * @param name
	 * @return
	 */
	public String getTranslation (String name) {
		if (!this.translation.containsKey (name)) return name;
		return this.translation.getString (name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnable () {
		super.onEnable ();

		// load ProtocolLib
		protocolManager = ProtocolLibrary.getProtocolManager ();

		// load permission plugin
		RegisteredServiceProvider<Permission> permissionServiceProvider = getServer().getServicesManager().getRegistration(Permission.class);
		this.permission = permissionServiceProvider.getProvider ();

		// register event handlers
		this.getServer ().getPluginManager ().registerEvents ((new BlockEventListener (this)), this);
		this.getServer ().getPluginManager ().registerEvents ((new EntityEventListener (this)), this);
		this.getServer ().getPluginManager ().registerEvents ((new PlayerEventListener (this)), this);
		this.getServer ().getPluginManager ().registerEvents ((new WorldEventListener (this)), this);

		// load translation
		this.translation = ResourceBundle.getBundle ("messages", Locale.ENGLISH);

		// load metrics
		try {
			MetricsLite metrics = new MetricsLite (this);
			metrics.start ();
		} catch (IOException ex) {
			this.getLogger ().warning ("Could not start metrics.");
		}
	}
}