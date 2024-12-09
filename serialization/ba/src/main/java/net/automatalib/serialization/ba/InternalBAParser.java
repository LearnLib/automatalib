/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.serialization.ba;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.fsa.MutableFSA;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.IOUtil;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;

class InternalBAParser<S, I, A extends MutableFSA<S, I>> implements InputModelDeserializer<I, A> {

    private static final Pattern ILLEGAL_PATTERN = Pattern.compile("[,\\->]");
    private static final Pattern TRANS_PATTERN = Pattern.compile("(.*),(.*)->(.*)");

    private final AutomatonCreator<? extends A, I> creator;
    private final Function<String, I> labelParser;

    InternalBAParser(AutomatonCreator<? extends A, I> creator, Function<String, I> labelParser) {
        this.creator = creator;
        this.labelParser = labelParser;
    }

    @Override
    public InputModelData<I, A> readModel(InputStream is) throws IOException, FormatException {

        String initialState = null;
        final Set<String> states = new HashSet<>();
        final Set<String> finalStates = new HashSet<>();
        final Set<String> symbols = new HashSet<>();
        final Map<String, Map<String, Set<String>>> transitions = new HashMap<>();

        try (BufferedReader br = new BufferedReader(IOUtil.asNonClosingUTF8Reader(is))) {

            String line = br.readLine();

            // initial state (optional)
            if (line != null && !TRANS_PATTERN.matcher(line).matches()) {
                if (ILLEGAL_PATTERN.matcher(line).find()) {
                    throw new FormatException("Invalid identifier: " + line);
                }
                states.add(line);
                initialState = line;
                line = br.readLine();
            }

            // transitions
            while (line != null) {

                final Matcher matcher = TRANS_PATTERN.matcher(line);

                if (!matcher.matches()) {
                    break;
                }

                final String label = matcher.group(1);
                if (ILLEGAL_PATTERN.matcher(label).find()) {
                    throw new FormatException("Invalid identifier: " + label);
                }

                final String src = matcher.group(2);
                if (ILLEGAL_PATTERN.matcher(src).find()) {
                    throw new FormatException("Invalid identifier: " + src);
                }

                final String tgt = matcher.group(3);
                if (ILLEGAL_PATTERN.matcher(tgt).find()) {
                    throw new FormatException("Invalid identifier: " + tgt);
                }

                if (initialState == null) {
                    initialState = src;
                }

                states.add(src);
                states.add(tgt);
                symbols.add(label);
                transitions.computeIfAbsent(src, k -> new HashMap<>())
                           .computeIfAbsent(label, k -> new HashSet<>())
                           .add(tgt);

                line = br.readLine();
            }

            // accepting states (optional)
            while (line != null) {
                if (ILLEGAL_PATTERN.matcher(line).find()) {
                    throw new FormatException("Invalid identifier: " + line);
                }

                states.add(line);
                finalStates.add(line);
                line = br.readLine();
            }

            return constructAutomaton(symbols, transitions, states, finalStates, initialState);
        }
    }

    private InputModelData<I, A> constructAutomaton(Set<String> symbols,
                                                    Map<String, Map<String, Set<String>>> transitions,
                                                    Set<String> states,
                                                    Set<String> finalStates,
                                                    String initialState) {

        // alphabet
        final Map<String, I> inputMapping = new HashMap<>(HashUtil.capacity(symbols.size()));
        for (String symbol : symbols) {
            inputMapping.put(symbol, this.labelParser.apply(symbol));
        }

        final Alphabet<I> alphabet = Alphabets.fromCollection(inputMapping.values());
        final A automaton = this.creator.createAutomaton(alphabet);

        // states
        final Map<String, S> stateMapping = new HashMap<>(HashUtil.capacity(states.size()));
        final boolean allAccepting = finalStates.isEmpty();

        for (String state : states) {
            final S s = automaton.addState(allAccepting || finalStates.contains(state));
            if (state.equals(initialState)) {
                automaton.setInitial(s, true);
            }
            stateMapping.put(state, s);
        }

        // transitions
        for (Entry<String, Map<String, Set<String>>> e1 : transitions.entrySet()) {
            final String src = e1.getKey();
            for (Entry<String, Set<String>> e2 : e1.getValue().entrySet()) {
                final String label = e2.getKey();
                for (String tgt : e2.getValue()) {
                    automaton.addTransition(stateMapping.get(src), inputMapping.get(label), stateMapping.get(tgt));
                }
            }
        }

        return new InputModelData<>(automaton, alphabet);
    }
}
