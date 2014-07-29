package com.evilco.bukkit.locker.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.evilco.bukkit.locker.LockerPlugin;
import org.bukkit.ChatColor;

import java.util.Arrays;

/**
 * @author Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.com>
 */
public class SignSanityPacketListener extends PacketAdapter {


	/**
	 * Constructs a new SignSanityPacketAdapter instance.
	 * @param plugin The parent plugin instance.
	 */
	public SignSanityPacketListener (LockerPlugin plugin) {
		super (plugin, PacketType.Play.Server.UPDATE_SIGN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPacketSending (PacketEvent event) {
		// get packet container
		PacketContainer container = event.getPacket ();

		// get sign contents
		String[] signContent = container.getStringArrays ().read (0);

		// fix array size
		if (signContent.length < 4) {
			// store old length
			int oldLength = signContent.length;

			// fix array size
			signContent = Arrays.copyOf (signContent, 4);

			// fill missing elements
			for (int i = oldLength; i < 4; i++) signContent[i] = "";
		}

		// check for prefix
		if (!signContent[0].equalsIgnoreCase (LockerPlugin.PROTECTION_PREFIX)) return;

		// mark non-migrated protections
		if (signContent[2].length () != 9 || !signContent[2].startsWith (":")) {
			// update colors
			signContent[2] = ChatColor.RED + (signContent[2].length () > 13 ? signContent[2].substring (0, 12) : signContent[2]);
			signContent[3] = ChatColor.RED + (signContent[3].length () > 13 ? signContent[2].substring (0, 12) : signContent[3]);
		} else {
			signContent[2] = ChatColor.DARK_BLUE + (signContent[3].length () > 13 ? signContent[3].substring (0, 12) : signContent[3]);
			signContent[3] = "";
		}

		// replace contents
		container.getStringArrays ().write (0, signContent);
	}
}