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
package net.automatalib.commons.util.ref;

import java.lang.ref.WeakReference;


/**
 * An abstraction for (weak or strong) references.
 * 
 * This class allows for treating normal ("strong") references the same way
 * as {@link WeakReference}s.
 * 
 * @author Malte Isberner <malte.isberner@cs.uni-dortmund.de>
 *
 * @param <T> referent class.
 */
public interface Ref<T> {
	/**
	 * Retrieves the referent. In case of {@link WeakRef}s, the return value
	 * may become <code>null</code>.
	 * @return the referent.
	 */
	public T get();
}
