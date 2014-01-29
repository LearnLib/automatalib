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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Malte Isberner
 */
public class AutomataLibSettings {

	private static final Logger LOG = Logger.getLogger(AutomataLibSettings.class.getName());

	private static final AutomataLibSettings INSTANCE = new AutomataLibSettings();

	public static AutomataLibSettings getInstance() {
		return INSTANCE;
	}


	private final Properties properties = new Properties();


	public String getProperty(String propName) {
		return properties.getProperty("automatalib." + propName);
	}

	public String getProperty(String propName, String defaultValue) {
		return properties.getProperty("automatalib." + propName, defaultValue);
	}



	private AutomataLibSettings() {
		try {
			Enumeration<URL> resourceUrls = getClass().getClassLoader().getResources("automatalib.properties");
			while(resourceUrls.hasMoreElements()) {
				URL url = resourceUrls.nextElement();
				try(BufferedInputStream is = new BufferedInputStream(url.openStream())) {
					properties.load(is);
				}
				catch(IOException ex) {
					LOG.severe("Could not read property file " + url + ": " + ex.getMessage());
				}
			}
		}
		catch(IOException ex) {
			LOG.severe("Could not enumerate automatalib.properties files: " + ex.getMessage());
		}

		// System properties (specified via command line) override all other properties
		properties.putAll(System.getProperties());

		System.err.println("Effective properties:");
		properties.list(System.err);
	}
}
