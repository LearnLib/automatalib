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
package net.automatalib.commons.util.lib;

/**
 * Specifies in which order a library to be loaded is searched for.
 * 
 * @author Malte Isberner
 *
 */
public enum LoadPolicy {
	/**
	 * First try to load a compatible version of the requested library shipped
	 * with the loading class. If that fails, try loading the system-provided
	 * version of that library.
	 */
	PREFER_SHIPPED,
	/**
	 * First try to load the system version of the requested library. If that fails,
	 * try loading a compatible shipped version.
	 */
	PREFER_SYSTEM,
	/**
	 * Only try to load a compatible version of the requested library shipped with
	 * the loading class.
	 */
	SHIPPED_ONLY,
	/**
	 * Only try to laod the system version of the requested library.
	 */
	SYSTEM_ONLY
}
