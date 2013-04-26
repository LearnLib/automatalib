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
package net.automatalib.automata.transout.abstractimpl;

import net.automatalib.automata.transout.MooreMachine;
import net.automatalib.automata.transout.MutableMooreMachine;

public abstract class AbstractMooreMachine {
	private AbstractMooreMachine() {}
	
	public static <S,I,T,O> O getStateProperty(MooreMachine<S, I, T, O> $this, S state) {
		return $this.getStateOutput(state);
	}
	
	public static <S,I,T,O> Void getTransitionProperty(MooreMachine<S,I,T,O> $this, T transition) {
		return null;
	}
	
	public static <S,I,T,O> void setStateProperty(MutableMooreMachine<S, I, T, O> $this, S state, O property) {
		$this.setStateOutput(state, property);
	}
	
	public static <S,I,T,O> void setTransitionProperty(MutableMooreMachine<S, I, T, O> $this, T transition, Void property) {
	}
	
	public static <S,I,T,O> T copyTransition(MutableMooreMachine<S, I, T, O> $this, T transition, S succ) {
		T newTrans = $this.createTransition(succ, null);
		return newTrans;
	}
	
	public static <S,I,T,O> O getTransitionOutput(MooreMachine<S,I,T,O> $this, T transition) {
		return $this.getStateOutput($this.getSuccessor(transition));
	}
}
