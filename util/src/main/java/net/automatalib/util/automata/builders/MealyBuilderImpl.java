/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.util.automata.builders;

import net.automatalib.automata.transout.MutableMealyMachine;

import com.github.misberner.duzzt.annotations.DSLAction;
import com.github.misberner.duzzt.annotations.GenerateEmbeddedDSL;
import com.github.misberner.duzzt.annotations.SubExpr;

@GenerateEmbeddedDSL(name = "MealyBuilder",
	syntax = "<transition>* withInitial <transition>* create",
	where = {@SubExpr(name="transition", definedAs="from (on withOutput? (to|loop))+")})
class MealyBuilderImpl<S, I, T, O, A extends MutableMealyMachine<S, ? super I, T, ? super O>>
		extends AutomatonBuilderImpl<S, I, T, Void, O, A> {
	
	public MealyBuilderImpl(A automaton) {
		super(automaton);
	}
	
	@DSLAction
	public void withOutput(O output) {
		super.withProperty(output);
	}
}
