/* Copyright (C) 2015 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
