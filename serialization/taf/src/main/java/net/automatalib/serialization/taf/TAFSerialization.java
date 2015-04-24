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
package net.automatalib.serialization.taf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.serialization.SerializationProvider;
import net.automatalib.serialization.taf.parser.PrintStreamDiagnosticListener;
import net.automatalib.serialization.taf.parser.TAFParser;
import net.automatalib.serialization.taf.writer.TAFWriter;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class TAFSerialization implements SerializationProvider {
	
	private static TAFSerialization INSTANCE = new TAFSerialization();
	
	public static TAFSerialization getInstance() {
		return INSTANCE;
	}

	private TAFSerialization() {
	}

	@Override
	public CompactDFA<Integer> readGenericDFA(InputStream is)
			throws IOException {
		CompactDFA<String> nativeDfa = readNativeDFA(is);
		return normalize(nativeDfa, nativeDfa.getInputAlphabet());
	}
	
	@Override
	public CompactDFA<String> readNativeDFA(InputStream is) {
		return TAFParser.parseDFA(is, PrintStreamDiagnosticListener.getStderrDiagnosticListener());
	}

	@Override
	public <I> void writeDFA(DFA<?, I> dfa, Alphabet<I> alphabet,
			OutputStream os) throws IOException {
		TAFWriter.writeDFA(dfa, alphabet, new OutputStreamWriter(os));
	}

	@Override
	public CompactNFA<Integer> readGenericNFA(InputStream is)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <I> void writeNFA(NFA<?, I> nfa, Alphabet<I> alphabet,
			OutputStream os) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	private static <I> CompactDFA<Integer> normalize(DFA<?,I> dfa, Alphabet<I> alphabet) {
		Alphabet<Integer> normalizedAlphabet = Alphabets.integers(0, alphabet.size() - 1);
		CompactDFA<Integer> result = new CompactDFA<>(normalizedAlphabet, dfa.size());
		AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, dfa, alphabet, result,
				i -> alphabet.getSymbolIndex(i));
		return result;
	}

}
