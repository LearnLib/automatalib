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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import com.google.common.collect.Iterators;

public interface SettingsSource {
	public void loadSettings(Properties props);
	
	default public int getPriority() {
		return 0;
	}
	
	
	public static <S extends SettingsSource> Properties readSettings(Class<S> clazz) {
		Properties p = new Properties();
		readSettings(clazz, p);
		return p;
	}
	
	public static <S extends SettingsSource> void readSettings(Class<S> clazz, Properties p) {
		ServiceLoader<S> loader = ServiceLoader.load(clazz);
		List<S> sources = new ArrayList<>();
		Iterators.addAll(sources, loader.iterator());
		sources.sort(new Comparator<SettingsSource>() {
			@Override
			public int compare(SettingsSource a, SettingsSource b) {
				return a.getPriority() - b.getPriority();
			}
		});
		
		for (S source : sources) {
			source.loadSettings(p);
		}
	}
}
