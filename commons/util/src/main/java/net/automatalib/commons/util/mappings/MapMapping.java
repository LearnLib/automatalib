/* Copyright (C) 2013 TU Dortmund
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
