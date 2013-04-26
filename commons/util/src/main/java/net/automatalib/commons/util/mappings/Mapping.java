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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.commons.util.mappings;

import java.util.Map;

/**
 * An interface for mapping objects of a certain domain type
 * to objects of a certain range type.
 * <p>
 * A mapping is very much like a {@link Map}, but the perspective is a
 * different one: Whereas a map is a (particularly finite) key/value collection, a
 * mapping is more like a function: it does not support retrieval of all keys or values,
 * because it does not requires them to be stored at all. Instead, they can be calculated
 * on the fly upon an invocation of {@link #get(Object)}.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <D> domain type.
 * @param <R> range type.
 */
public interface Mapping<D,R> {
	/**
	 * Get the range object <code>elem</code> maps to.
	 * 
	 * @param elem object from the domain.
	 * @return the object from the range corresponding to
	 * <code>elem</code>.
	 */
	public R get(D elem);
}
