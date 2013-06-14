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
package net.automatalib.commons.util.ref;

import java.lang.ref.WeakReference;


/**
 * A weak reference wrapper, complying to the {@link Ref} interface.
 * 
 * @author Malte Isberner <malte.isberner@cs.uni-dortmund.de>
 *
 * @param <T> referent class.
 */
public final class WeakRef<T> implements Ref<T> {
	
	private final WeakReference<T> reference;
	
	/**
	 * Constructor.
	 * @param referent the referent.
	 */
	public WeakRef(T referent) {
		this.reference = new WeakReference<T>(referent);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.misc.util.ref.Ref#get()
	 */
	@Override
	public T get() {
		return reference.get();
	}

}
