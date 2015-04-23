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

/**
 * This package contains the basic interfaces for automata.
 * <p>
 * An automaton (in the AutomataLib context) is a finite-state
 * {@link net.automatalib.ts.TransitionSystem transition system}. Like transition systems,
 * automata are in general nondeterministic, but can be specialized to be
 * {@link net.automatalib.automata.DeterministicAutomaton deterministic}.
 * <p>
 * The {@link net.automatalib.automata.Automaton Automaton} interface has no inherent semantics.
 * Special types of automata, such as {@link net.automatalib.automata.fsa.DFA DFAs} or
 * {@link net.automatalib.automata.transout.MealyMachine Mealy machines} can be found in
 * the respective subpackages.
 * 
 * @author Malte Isberner
 * 
 * @see net.automatalib.ts
 */
package net.automatalib.automata;