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
		AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, dfa, alphabet, result, alphabet::getSymbolIndex);
		return result;
	}

}
