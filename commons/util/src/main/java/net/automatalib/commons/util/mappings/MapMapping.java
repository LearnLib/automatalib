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
package net.automatalib.commons.util.mappings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class that wraps a {@link Mapping} around a {@link java.util.Map}.
 * 
 * @author Malte Isberner
 *
 * @param <D> domain type.
 * @param <R> range type.
 */
public class MapMapping<D, R> implements MutableMapping<D, R> {
	
	public static <D,R> MapMapping<D,R> create(Map<D,R> map) {
		return new MapMapping<D,R>(map);
	}
	
	
	private final Map<? super D, R> map;
	
	public MapMapping(Map<D,R> map, boolean copy) {
		if(!copy)
			this.map = map;
		else
			this.map = new HashMap<D,R>();
	}
	
	/**
	 * Constructor.
	 * @param map the underlying {@link java.util.Map} object.
	 */
	public MapMapping(Map<? super D,R> map) {
		this.map = map;
	}
	
	public MapMapping() {
		this(new HashMap<D,R>());
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.udo.ls5.util.Mapping#get(java.lang.Object)
	 */
	@Override
	public R get(D elem) {
		return map.get(elem);
	}
	
	/**
	 * Delegates to the underlying {@link java.util.Map}.
	 * @see java.util.Map#put(Object, Object)
	 */
	@Override
	public R put(D key, R value) {
		return map.put(key, value);
	}
	
	/**
	 * Delegates to the underlying {@link java.util.Map}.
	 * @see java.util.Map#entrySet()
	 */
	public Set<? extends Map.Entry<? super D,R>> entrySet() {
		return map.entrySet();
	}
}
