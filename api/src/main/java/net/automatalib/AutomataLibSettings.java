/* Copyright (c) 2014 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 *
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib;

import java.util.Properties;

import net.automatalib.commons.util.settings.SettingsSource;

/**
 * @author Malte Isberner
 */
public class AutomataLibSettings {

	private static final AutomataLibSettings INSTANCE = new AutomataLibSettings();

	public static AutomataLibSettings getInstance() {
		return INSTANCE;
	}


	private final Properties properties;


	public String getProperty(String propName) {
		return properties.getProperty("automatalib." + propName);
	}

	public String getProperty(String propName, String defaultValue) {
		return properties.getProperty("automatalib." + propName, defaultValue);
	}



	private AutomataLibSettings() {
		properties = SettingsSource.readSettings(AutomataLibSettingsSource.class);
	}
}
