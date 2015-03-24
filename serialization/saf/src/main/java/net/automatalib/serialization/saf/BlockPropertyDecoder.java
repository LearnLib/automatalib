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
package net.automatalib.serialization.saf;

import java.io.DataInput;
import java.io.IOException;

@FunctionalInterface
public interface BlockPropertyDecoder<P> {
	public static <P> BlockPropertyDecoder<P> nullDecoder() {
		return in -> null;
	}
	default public void start(DataInput in) throws IOException {}
	public P readProperty(DataInput in) throws IOException;
	default public void finish(DataInput in) throws IOException {}
}
