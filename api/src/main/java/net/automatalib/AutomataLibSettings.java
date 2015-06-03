/* Copyright (c) 2014 TU Dortmund
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
