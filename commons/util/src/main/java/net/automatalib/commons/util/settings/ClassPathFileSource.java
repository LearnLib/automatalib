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
package net.automatalib.commons.util.settings;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class ClassPathFileSource implements SettingsSource {
	
	private final String fileName;
	
	protected ClassPathFileSource(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public void loadSettings(Properties properties) {
		Logger log = Logger.getLogger(getClass().getName());
		
		try {
			Enumeration<URL> resourceUrls = getClass().getClassLoader().getResources(fileName);
			while(resourceUrls.hasMoreElements()) {
				URL url = resourceUrls.nextElement();
				try(BufferedInputStream is = new BufferedInputStream(url.openStream())) {
					properties.load(is);
				}
				catch(IOException ex) {
					log.severe("Could not read property file " + url + ": " + ex.getMessage());
				}
			}
		}
		catch(IOException ex) {
			log.severe("Could not enumerate " + fileName + " files: " + ex.getMessage());
		}
	}
	
	

}
