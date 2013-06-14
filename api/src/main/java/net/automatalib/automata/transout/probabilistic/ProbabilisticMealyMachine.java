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
package net.automatalib.automata.transout.probabilistic;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.concepts.Probabilistic;
import net.automatalib.automata.concepts.TransitionOutput;
import net.automatalib.ts.UniversalTransitionSystem;

@ParametersAreNonnullByDefault
public interface ProbabilisticMealyMachine<S, I, T, O> extends Automaton<S, I, T>,
	TransitionOutput<T, O>, UniversalTransitionSystem<S,I,T,Void,ProbabilisticOutput<O>>, Probabilistic<T> {

	@Override
	@Nonnull
	public ProbabilisticOutput<O> getTransitionProperty(T transition);
}
