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
package net.automatalib.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.words.Alphabet;

public interface SerializationProvider {
	
	// DFA serialization
	default public CompactDFA<?> readNativeDFA(URL url) throws IOException {
		try (InputStream is = url.openStream()) {
			return readNativeDFA(is);
		}
	}
	default public CompactDFA<?> readNativeDFA(File f) throws FileNotFoundException, IOException {
		try (InputStream is = new FileInputStream(f)) {
			return readNativeDFA(is);
		}
	}
	default public CompactDFA<?> readNativeDFA(byte[] buf) throws IOException {
		try (ByteArrayInputStream is = new ByteArrayInputStream(buf)) {
			return readNativeDFA(is);
		}
	}
	default public CompactDFA<?> readNativeDFA(InputStream is) throws IOException {
		return readGenericDFA(is);
	}
	
	default public CompactDFA<Integer> readGenericDFA(URL url) throws IOException {
		try (InputStream is = url.openStream()) {
			return readGenericDFA(is);
		}
	}
	default public CompactDFA<Integer> readGenericDFA(File f) throws FileNotFoundException, IOException {
		try (FileInputStream is = new FileInputStream(f)) {
			return readGenericDFA(is);
		}
	}
	default public CompactDFA<Integer> readGenericDFA(byte[] buf) throws IOException {
		try (ByteArrayInputStream is = new ByteArrayInputStream(buf)) {
			return readGenericDFA(is);
		}
	}
	public CompactDFA<Integer> readGenericDFA(InputStream is) throws IOException;
	
	default public <I> CompactDFA<I> readCustomDFA(URL url, Alphabet<I> alphabet) throws IOException {
		try (InputStream is = url.openStream()) {
			return readCustomDFA(is, alphabet);
		}
	}
	default public <I> CompactDFA<I> readCustomDFA(File f, Alphabet<I> alphabet) throws IOException {
		try (InputStream is = new FileInputStream(f)) {
			return readCustomDFA(is, alphabet);
		}
	}
	default public <I> CompactDFA<I> readCustomDFA(byte[] buf, Alphabet<I> alphabet) throws IOException {
		try (InputStream is = new ByteArrayInputStream(buf)) {
			return readCustomDFA(is, alphabet);
		}
	}
	default public <I> CompactDFA<I> readCustomDFA(InputStream is, Alphabet<I> alphabet) throws IOException {
		CompactDFA<?> dfa = readNativeDFA(is);
		
		return dfa.translate(alphabet);
	}
	
	public <I> void writeDFA(DFA<?,I> dfa, Alphabet<I> alphabet, OutputStream os) throws IOException;
	default public <I> void writeDFA(DFA<?,I> dfa, Alphabet<I> alphabet, File f) throws FileNotFoundException, IOException {
		try (FileOutputStream os = new FileOutputStream(f)) {
			writeDFA(dfa, alphabet, os);
		}
	}
	default public <I> byte[] encodeDFA(DFA<?,I> dfa, Alphabet<I> alphabet) throws IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			writeDFA(dfa, alphabet, os);
			return os.toByteArray();
		}
	}
	
	// NFA serialization
	default public CompactNFA<?> readNativeNFA(URL url) throws IOException {
		try (InputStream is = url.openStream()) {
			return readNativeNFA(is);
		}
	}
	default public CompactNFA<?> readNativeNFA(File f) throws FileNotFoundException, IOException {
		try (InputStream is = new FileInputStream(f)) {
			return readNativeNFA(is);
		}
	}
	default public CompactNFA<?> readNativeNFA(byte[] buf) throws IOException {
		try (ByteArrayInputStream is = new ByteArrayInputStream(buf)) {
			return readNativeNFA(is);
		}
	}
	default public CompactNFA<?> readNativeNFA(InputStream is) throws IOException {
		return readGenericNFA(is);
	}
	
	default public CompactNFA<Integer> readGenericNFA(URL url) throws IOException {
		try (InputStream is = url.openStream()) {
			return readGenericNFA(is);
		}
	}
	default public CompactNFA<Integer> readGenericNFA(File f) throws FileNotFoundException, IOException {
		try (FileInputStream is = new FileInputStream(f)) {
			return readGenericNFA(is);
		}
	}
	default public CompactNFA<Integer> readGenericNFA(byte[] buf) throws IOException {
		try (ByteArrayInputStream is = new ByteArrayInputStream(buf)) {
			return readGenericNFA(is);
		}
	}
	public CompactNFA<Integer> readGenericNFA(InputStream is) throws IOException;
	
	default public <I> CompactNFA<I> readCustomNFA(URL url, Alphabet<I> alphabet) throws IOException {
		try (InputStream is = url.openStream()) {
			return readCustomNFA(is, alphabet);
		}
	}
	default public <I> CompactNFA<I> readCustomNFA(File f, Alphabet<I> alphabet) throws IOException {
		try (InputStream is = new FileInputStream(f)) {
			return readCustomNFA(is, alphabet);
		}
	}
	default public <I> CompactNFA<I> readCustomNFA(byte[] buf, Alphabet<I> alphabet) throws IOException {
		try (InputStream is = new ByteArrayInputStream(buf)) {
			return readCustomNFA(is, alphabet);
		}
	}
	default public <I> CompactNFA<I> readCustomNFA(InputStream is, Alphabet<I> alphabet) throws IOException {
		CompactNFA<?> nfa = readNativeNFA(is);
		
		return nfa.translate(alphabet);
	}
	
	public <I> void writeNFA(NFA<?,I> nfa, Alphabet<I> alphabet, OutputStream os) throws IOException;
	default public <I> void writeNFA(NFA<?,I> nfa, Alphabet<I> alphabet, File f) throws FileNotFoundException, IOException {
		try (FileOutputStream os = new FileOutputStream(f)) {
			writeNFA(nfa, alphabet, os);
		}
	}
	default public <I> byte[] encodeNFA(NFA<?,I> nfa, Alphabet<I> alphabet) throws IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			writeNFA(nfa, alphabet, os);
			return os.toByteArray();
		}
	}
}
