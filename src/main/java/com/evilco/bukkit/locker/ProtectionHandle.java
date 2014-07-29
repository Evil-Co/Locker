package com.evilco.bukkit.locker;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
	 * Stores the owner's UUID.
	 */
	protected UUID ownerID = null;

	/**
	 * Stores the name of the protection owner.
	 */
	protected String ownerName;

	/**
	 * Constructs a new ProtectionHandle.
	 * @param sign The sign.
	 */
	public ProtectionHandle (Sign sign, Location descriptorLocation) {
		// get line 2 and check for migration
		String owner = sign.getLine (2);

		// migration code
		if (owner.length () != 9 || !owner.startsWith (":"))
			// store owner name
			this.ownerName = (sign.getLine (2) + sign.getLine (3));
		else {
			// decode UUID
			this.ownerID = decodeUUID (owner);

			// store owner name
			this.ownerName = sign.getLine (3);
		}

		// store location
		this.descriptorLocation = descriptorLocation;
	}

	/**
	 * Constructs a new ProtectionHandle.
	 * @param player The player.
	 * @param descriptorLocation The descriptor location.
	 */
	public ProtectionHandle (Player player, Location descriptorLocation) {
		this.ownerID = player.getUniqueId ();
		this.ownerName = player.getName ();
		this.descriptorLocation = descriptorLocation;

		// update sign
		this.updateSign ();
	}

	/**
	 * Builds the sign text for this handle.
	 * @return The sign text.
	 */
	public List<String> buildSignText () {
		// create list
		ArrayList<String> list = new ArrayList<String> ();

		// add elements
		list.set (0, LockerPlugin.PROTECTION_PREFIX);
		list.set (1, "");
		list.set (2, encodeUUID (this.ownerID));
		list.set (3, (this.ownerName.length () > 15 ? this.ownerName.substring (0, 14) : this.ownerName));

		// return finished list
		return list;
	}

	/**
	 * Decodes a UUID.
	 * @param encoded The encoded UUID.
	 * @return The UUID.
	 */
	public static UUID decodeUUID (String encoded) {
		long most = 0;
		long least = 0;

		// remove first character
		encoded = encoded.substring (1);

		// iterate over elements
		for (int i = 0; i < 4; i++) {
			most |= ((long) (encoded.charAt (i) << (48 - (16 * i))));
			least |= ((long) (encoded.charAt ((i + 4)) << (48 - (16 * i))));
		}

		// construct UUID
		return (new UUID (most, least));
	}

	/**
	 * Encodes a UUID for storage on a sign.
	 * @param uuid The UUID.
	 * @return The encoded UUID.
	 */
	public static String encodeUUID (UUID uuid) {
		// create string builder
		StringBuilder builder = new StringBuilder ();

		// get bits
		long most = uuid.getMostSignificantBits ();
		long least = uuid.getLeastSignificantBits ();

		// add prefix
		builder.append (':');

		// encode most significant bits
		builder.append (((char) ((most >> 48) & 0xFFFF)));
		builder.append (((char) ((most >> 32) & 0xFFFF)));
		builder.append (((char) ((most >> 16) & 0xFFFF)));
		builder.append (((char) (most & 0xFFFF)));

		// encode least significant bits
		builder.append (((char) ((least >> 48) & 0xFFFF)));
		builder.append (((char) ((least >> 32) & 0xFFFF)));
		builder.append (((char) ((least >> 16) & 0xFFFF)));
		builder.append (((char) (least & 0xFFFF)));

		// return finished string
		return builder.toString ();
	}

	/**
	 * Returns the descriptor location.
	 * @return
	 */
	public Location getDescriptorLocation () {
		return this.descriptorLocation;
	}

	/**
	 * Returns the owner UUID.
	 * @return The UUID.
	 */
	public UUID getOwnerID () {
		return this.ownerID;
	}

	/**
	 * Returns the owner name.
	 * @return The owner name.
	 */
	public String getOwnerName () {
		return this.ownerName;
	}

	/**
	 * Checks whether the specified user has access permissions.
	 * @param player The player who tries to access the container.
	 * @return True if the player has permissions.
	 */
	public boolean hasAccessPermissions (Player player) {
		// TODO: Add a friendlist feature

		// check UUID
		if (this.ownerID != null) return (player.getUniqueId ().equals (this.ownerID));

		// check permissions
		// TODO: This may cause problems for people who weren't able to migrate early on for whatever reasons
		return (player.getName ().equals (this.ownerName));
	}

	/**
	 * Checks whether the specified user is the owner.
	 * @param player The player.
	 * @return True if the player is the container owner.
	 */
	public boolean isOwner (Player player) {
		return (player.getUniqueId ().equals (this.ownerID));
	}

	/**
	 * Migrates an old sign.
	 */
	public void migrate (LockerPlugin plugin, Player player) {
		// disallow migration
		if (this.ownerID != null) return;

		// verify owner
		if (!this.ownerName.equalsIgnoreCase (player.getName ())) return;

		// migrate
		this.ownerID = player.getUniqueId ();

		// force sign update
		this.updateSign ();

		// notify player
		player.sendMessage (ChatColor.BLUE + plugin.getTranslation ("plugin.migrated"));
	}

	/**
	 * Updates a sign according to the current handle.
	 */
	public void updateSign () {
		// deny update if migration is pending
		if (this.ownerID == null) return;

		// get sign
		Sign sign = ((Sign) this.getDescriptorLocation ().getBlock ().getState ());

		// update text
		sign.setLine (2, encodeUUID (this.ownerID));
		sign.setLine (3, (this.ownerName.length () > 15 ? this.ownerName.substring (0, 14) : this.ownerName));

		// force sign update
		sign.update (true);
	}
}