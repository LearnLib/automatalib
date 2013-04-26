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
package net.automatalib.graphs;

/**
 * A finite Kripke Transition System combines the properties of both a labeled transition system
 * and a Kripke structure.
 *  
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <N> node class
 * @param <E> edge class
 * @param <AP> atomic proposition class
 * @param <L> edge label class
 * 
 * @see FiniteLTS
 * @see FiniteKripkeStructure
 */
public interface FiniteKTS<N, E, AP, L> extends FiniteKripkeStructure<N,E,AP>, FiniteLTS<N,E,L> {

}
