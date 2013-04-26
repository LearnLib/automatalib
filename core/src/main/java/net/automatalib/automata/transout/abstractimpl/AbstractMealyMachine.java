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

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.MutableMealyMachine;

public abstract class AbstractMealyMachine {
	public static <S,I,T,O> Void getStateProperty(MealyMachine<S, I, T, O> $this, S state) {
		return null;
	}
	
	public static <S,I,T,O> O getTransitionProperty(MealyMachine<S,I,T,O> $this, T transition) {
		return $this.getTransitionOutput(transition);
	}
	
	public static <S,I,T,O> void setStateProperty(MutableMealyMachine<S, I, T, O> $this, S state, Void property) {
		
	}
	
	public static <S,I,T,O> void setTransitionProperty(MutableMealyMachine<S, I, T, O> $this, T transition, O property) {
		$this.setTransitionOutput(transition, property);
	}
	
	public static <S,I,T,O> T copyTransition(MutableMealyMachine<S, I, T, O> $this, T transition, S succ) {
		O output = $this.getTransitionOutput(transition);
		T newTrans = $this.createTransition(succ, output);
		return newTrans;
	}
	
}
