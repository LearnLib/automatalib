/* Copyright (C) 2015 TU Dortmund
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
package net.automatalib.commons.util.lib;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class PlatformProperties {
	
	private static final Logger LOG = Logger.getLogger(PlatformProperties.class.getName());
	
	public static String OS_NAME;
	public static String OS_ARCH;
	public static String OS_VERSION;
	
	static {
		Properties aliases = new Properties();
		try(InputStream is = PlatformProperties.class.getResourceAsStream("/platform-aliases.properties")) {
			aliases.load(is);
		}
		catch (Exception ex) {
			LOG.warning("Could not load platform aliases file: " + ex.getMessage());
			LOG.warning("You may experience issues with the resolution of native libraries.");
		}
		
		String osName = System.getProperty("os.name").toLowerCase().replace(' ', '_').replace('/', '_');
		OS_NAME = aliases.getProperty("os." + osName, osName);
		
		String osArch = System.getProperty("os.arch").toLowerCase().replace(' ', '_').replace('/', '_');
		OS_ARCH = aliases.getProperty("arch." + osArch, osArch);
		
		OS_VERSION = System.getProperty("os.version");
	}
	
	private PlatformProperties() {
		throw new AssertionError();
	}
}
