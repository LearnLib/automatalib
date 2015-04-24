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
package net.automatalib.tools.taf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.automatalib.automata.FiniteAlphabetAutomaton;
import net.automatalib.serialization.taf.parser.PrintStreamDiagnosticListener;
import net.automatalib.serialization.taf.parser.TAFParser;
import net.automatalib.visualization.Visualization;

/**
 * {@code tafviz} - a tool for visualizing TAF files
 * 
 * @author Malte Isberner
 */
public class TAFViz {
	
	private void loadAndVisualize(InputStream is) {
		FiniteAlphabetAutomaton<?, String, ?> automaton
			= TAFParser.parseAny(is, PrintStreamDiagnosticListener.getStderrDiagnosticListener());
		Visualization.visualizeAutomaton(automaton, automaton.getInputAlphabet(), false);
	}

	public int run(String[] args) {
		if (args.length == 0) {
			loadAndVisualize(System.in);
		}
		for (String arg : args) {
			try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(new File(arg)))) {
				loadAndVisualize(is);
			}
			catch (FileNotFoundException ex) {
				System.err.println("Error opening file " + arg + ": " + ex.getMessage());
			}
			catch (IOException ex) {
				System.err.println("Error reading file " + arg + ": " + ex.getMessage());
			}
		}
		return 0;
	}
	
	public static void main(String[] args) {
		TAFViz app = new TAFViz();
		app.run(args);
	}
		

}
