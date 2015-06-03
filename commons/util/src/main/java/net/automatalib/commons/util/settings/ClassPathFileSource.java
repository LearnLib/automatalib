/* Copyright (C) 2015 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
