package com.evilco.bukkit.locker;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class ProtectionHandle {

	/**
	 * Stores the descriptor location.
	 */
	protected final Location descriptorLocation;

	/**
	 * Stores the name of the protection owner.
	 */
	protected final String ownerName;

	/**
	 * Constructs a new ProtectionHandle.
	 * @param ownerName
	 */
	public ProtectionHandle (String ownerName, Location descriptorLocation) {
		this.ownerName = ownerName;
		this.descriptorLocation = descriptorLocation;
	}

	/**
	 * Returns the descriptor location.
	 * @return
	 */
	public Location getDescriptorLocation () {
		return this.descriptorLocation;
	}

	/**
	 * Returns the owner name.
	 * @return
	 */
	public String getOwnerName () {
		return this.ownerName;
	}

	/**
	 * Checks whether the specified user has access permissions.
	 * @param playerName
	 * @return
	 */
	public boolean hasAccessPermissions (String playerName) {
		// TODO: Add a friendlist feature
		return (this.ownerName.equalsIgnoreCase (playerName));
	}

	/**
	 * Checks whether the specified user has access permissions.
	 * @param player
	 * @return
	 */
	public boolean hasAccessPermissions (Player player) {
		return this.hasAccessPermissions (player.getName ());
	}

	/**
	 * Checks whether the specified user is the owner.
	 * @param playerName
	 * @return
	 */
	public boolean isOwner (String playerName) {
		return this.ownerName.equalsIgnoreCase (playerName);
	}

	/**
	 * Checks whether the specified user is the owner.
	 * @param player
	 * @return
	 */
	public boolean isOwner (Player player) {
		return this.isOwner (player.getName ());
	}
}