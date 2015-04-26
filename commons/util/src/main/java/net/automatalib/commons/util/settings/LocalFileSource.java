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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class LocalFileSource implements SettingsSource {

	private final File file;
	
	protected LocalFileSource(File file) {
		this.file = file;
	}
	
	protected LocalFileSource(String fileName) {
		this(new File(fileName));
	}
	
	@Override
	public int getPriority() {
		// This is directly under user control, so it should have the second-highest possible
		// priority (to be overridden only by system properties)
		return Integer.MAX_VALUE - 1;
	}

	@Override
	public void loadSettings(Properties properties) {
		if (!file.exists()) {
			return;
		}
		
		Logger log = Logger.getLogger(getClass().getName());
		
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			properties.load(r);
		}
		catch(IOException ex) {
			log.warning("Could not read properties file " + file.getAbsolutePath() + ": " + ex.getMessage());
		}
	}
	
	
}
