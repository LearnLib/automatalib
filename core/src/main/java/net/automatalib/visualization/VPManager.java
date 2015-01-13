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
package net.automatalib.visualization;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class VPManager {
	
	private final Map<String,VisualizationProvider> providers = new HashMap<>();
	private VisualizationProvider bestProvider = new DummyVP();
	
	public void load() {
		providers.clear();
		ServiceLoader<VisualizationProvider> loader = ServiceLoader.load(VisualizationProvider.class);
		
		bestProvider = new DummyVP();
		for (VisualizationProvider vp : loader) {
			registerProvider(vp);
		}
	}
	
	public void registerProvider(VisualizationProvider vp) {
		if (!vp.checkUsable()) {
			return;
		}
		providers.put(vp.getId(), vp);
		
		if (vp.getPriority() > bestProvider.getPriority()) {
			bestProvider = vp;
		}
	}
	
	public VisualizationProvider getBestProvider() {
		return bestProvider;
	}
	
	public VisualizationProvider getProviderByName(String name) {
		return providers.get(name);
	}
	
}
