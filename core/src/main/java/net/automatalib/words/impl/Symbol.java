/* Copyright (C) 2013-2014 TU Dortmund
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
package net.automatalib.words.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.abstractimpl.AbstractSymbol;

@ParametersAreNonnullByDefault
public class Symbol extends AbstractSymbol<Symbol> {
	@Nullable
	private final Object userObject;

	public Symbol(@Nullable Object userObject) {
		this.userObject = userObject;
	}
	
	@Override
	public int compareTo(@Nonnull Symbol o) {
		return getId() - o.getId();
	}
	
	@Nullable
	public Object getUserObject() {
		return userObject;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	@Nonnull
	public String toString() {
		return String.valueOf(userObject);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((userObject == null) ? 0 : userObject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Symbol other = (Symbol) obj;
		if (userObject == null) {
			if (other.userObject != null)
				return false;
		} else if (!userObject.equals(other.userObject))
			return false;
		return true;
	}
	
	
}
