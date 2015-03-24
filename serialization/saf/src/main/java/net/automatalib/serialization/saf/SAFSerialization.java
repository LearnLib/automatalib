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
package net.automatalib.serialization.saf;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.serialization.SerializationProvider;
import net.automatalib.words.Alphabet;


public class SAFSerialization implements SerializationProvider {
	
	enum AutomatonType {
		DFA(true),
		NFA(false),
		MEALY(true);
		
		private final boolean deterministic;
		
		public boolean isDeterministic() {
			return deterministic;
		}
		private AutomatonType(boolean deterministic) {
			this.deterministic = deterministic;
		}
	}
	
	private static final SAFSerialization INSTANCE = new SAFSerialization();
	
	public static SAFSerialization getInstance() {
		return INSTANCE;
	}

	private SAFSerialization() {
		
	}

	@Override
	public CompactDFA<Integer> readGenericDFA(InputStream is)
			throws IOException {
		is = IOUtil.asUncompressedInputStream(is);
		SAFInput in = new SAFInput(is);
		return in.readNativeDFA();
	}

	@Override
	public <I> void writeDFA(DFA<?, I> dfa, Alphabet<I> alphabet,
			OutputStream os) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompactNFA<Integer> readGenericNFA(InputStream is)
			throws IOException {
		is = IOUtil.asUncompressedInputStream(is);
		SAFInput in = new SAFInput(is);
		return in.readNativeNFA();
	}

	@Override
	public <I> void writeNFA(NFA<?, I> nfa, Alphabet<I> alphabet,
			OutputStream os) throws IOException {
		throw new UnsupportedOperationException();
	}

}
