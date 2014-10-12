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
package net.automatalib.automata.transout;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.StateOutput;


public interface MooreMachine<S, I, T, O> extends UniversalDeterministicAutomaton<S,I,T,O,Void>,
		StateOutput<S,O>, TransitionOutputAutomaton<S, I, T, O> {
	
	@Override
	default public O getStateProperty(S state) {
		return getStateOutput(state);
	}
	
	@Override
	default public Void getTransitionProperty(T transition) {
		return null;
	}
	
	@Override
	default public O getTransitionOutput(T transition) {
		return getStateOutput(getSuccessor(transition));
	}
}
