package com.evilco.bukkit.locker.translation;

import java.util.ListResourceBundle;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class Messages extends ListResourceBundle {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object[][] getContents () {
		return new Object[][] {
			{"plugin.migrated",				"Your protection has been migrated."},
			{"protection.accessDenied",			"No Access"},
			{"protection.invalid",				"Invalid"},
			{"protection.permissions",			"You do not have permissions to protect inventories."},
			{"protection.placeLimitation",			"You cannot place signs around protected inventories."},
			{"protection.remove",				"You removed the protection."},
			{"protection.warning",				"The block you're trying to break is protected."}
		};
	}
}