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

import java.util.Set;

import net.automatalib.words.Alphabet;

/**
 * Interface for a builder object that takes care of the actual automaton
 * construction during parsing of a TAF file.
 * 
 * @author Malte Isberner
 */
interface TAFBuilder {
	public void init(Alphabet<String> alphabet);
	public void declareState(String identifer, Set<String> options);
	public Object finish();
}
