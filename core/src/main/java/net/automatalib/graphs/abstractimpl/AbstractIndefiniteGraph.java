/* Copyright (C) 2013 TU Dortmund
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
package net.automatalib.graphs.abstractimpl;

import java.util.HashMap;

import net.automatalib.commons.util.mappings.MapMapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;

public abstract class AbstractIndefiniteGraph<N, E> implements IndefiniteGraph<N, E> {

	/**
	 * Provides a realization for {@link IndefiniteGraph#createStaticNodeMapping()}
	 * by defaulting to a {@link HashMap} backed mapping.
	 * @see IndefiniteGraph#createStaticNodeMapping()
	 */
	public static <N,E,V> MutableMapping<N,V> createStaticNodeMapping(IndefiniteGraph<N,E> $this) {
		return new MapMapping<>(new HashMap<N,V>());
	}
	
	/**
	 * Provides a realization for {@link IndefiniteGraph#createDynamicNodeMapping()}
	 * by defaulting to a {@link HashMap} backed mapping.
	 * @see IndefiniteGraph#createDynamicNodeMapping()
	 */
	public static <N,E,V> MutableMapping<N,V> createDynamicNodeMapping(IndefiniteGraph<N,E> $this) {
		return new MapMapping<>(new HashMap<N,V>());
	}
	

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#createStaticNodeMapping()
	 */
	@Override
	public <V> MutableMapping<N, V> createStaticNodeMapping() {
		return createStaticNodeMapping(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#createDynamicNodeMapping()
	 */
	@Override
	public <V> MutableMapping<N, V> createDynamicNodeMapping() {
		return createDynamicNodeMapping(this);
	}

}
