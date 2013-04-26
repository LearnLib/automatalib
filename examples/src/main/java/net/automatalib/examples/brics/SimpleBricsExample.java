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
package net.automatalib.examples.brics;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import net.automatalib.brics.AbstractBricsAutomaton;
import net.automatalib.brics.BricsNFA;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Word;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class SimpleBricsExample {


	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		// Create a BRICS automaton from a regular expression ...
		RegExp r = new RegExp("ab+(c|d)*e?");
		Automaton a = r.toAutomaton();
		// ... and wrap it into the AutomataLib interfaces
		AbstractBricsAutomaton ba = new BricsNFA(a);
		
		// Then, display a DOT representation of this automaton
		Writer w = DOT.createDotWriter(true);
		GraphDOT.write(ba, w);
		w.close();
		
		List<Word<Character>> testWords = Arrays.asList(
				Word.fromString("abd"),
				Word.fromString("abbc"),
				Word.fromString("abbbbbde"),
				Word.fromString("ade"));
		
		for(Word<Character> tw : testWords)
			System.out.println("Output for " + tw + " is " + ba.computeOutput(tw));
	}

}
