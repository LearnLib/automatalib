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
package net.automatalib.automata.concepts;

public interface SuffixOutput<I, O> extends Output<I,O> {
    // FIXME: here I see a potential clash between I/O of the automaton and O of the suffix. 
    // Why do we need this interface and method anyway? I think its the responsibility of 
    // whoever is using this Automaton to truncate output as needed

	public O computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix);
}
