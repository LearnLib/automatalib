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
package net.automatalib.serialization.taf.parser;

import java.util.Collection;
import java.util.Set;

import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;

final class DefaultTAFBuilderMealy extends AbstractTAFBuilder<Integer,String,CompactMealyTransition<String>,Void,String,CompactMealy<String,String>>
		implements TAFBuilderMealy {

	public DefaultTAFBuilderMealy(InternalTAFParser parser) {
		super(parser);
	}

	@Override
	public void addTransitions(String source, Collection<String> symbols, String output, String targetId) {
		doAddTransitions(source, symbols, targetId, output);
	}

	@Override
	protected CompactMealy<String,String> createAutomaton(
			Alphabet<String> stringAlphabet) {
		return new CompactMealy<>(stringAlphabet);
	}

	@Override
	protected Void getStateProperty(Set<String> options) {
		return null;
	}

	@Override
	protected String translateInput(String s) {
		return s;
	}

}
