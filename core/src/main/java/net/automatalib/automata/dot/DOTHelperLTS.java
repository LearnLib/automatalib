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
package net.automatalib.automata.dot;

import net.automatalib.automata.lts.abstractimpl.AbstractMutableLTS;
import net.automatalib.words.InputOutputLabel;


public class DOTHelperLTS<I, O> extends DefaultDOTHelperAutomaton<Integer, InputOutputLabel, Integer, AbstractMutableLTS<InputOutputLabel>> {

	public DOTHelperLTS(AbstractMutableLTS<InputOutputLabel> automaton) {
		super(automaton);
	}
	
	
}
