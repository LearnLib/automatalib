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
package net.automatalib.serialization.taf.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.string.StringUtil;
import net.automatalib.serialization.InputModelSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;

class TAFConcreteWriter<S, I, T, TP, A extends UniversalDeterministicAutomaton<S, I, T, ?, TP>>
        implements InputModelSerializer<I, A> {

    private static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    private final String type;
    private final BiFunction<A, S, ? extends Collection<? extends String>> spExtractor;

    private int indent;

    TAFConcreteWriter(String type, BiFunction<A, S, Collection<String>> spExtractor) {
        this.type = type;
        this.spExtractor = spExtractor;
    }

    @Override
    public void writeModel(OutputStream os, A automaton, Alphabet<I> inputs) throws IOException {

        try (Writer out = IOUtil.asNonClosingUTF8Writer(os)) {
            begin(out, type, inputs);

            S init = automaton.getInitialState();
            StateIDs<S> ids = automaton.stateIDs();
            for (S state : automaton) {
                Set<String> options = new HashSet<>(spExtractor.apply(automaton, state));
                if (Objects.equals(init, state)) {
                    options.add("initial");
                }
                int id = ids.getStateId(state);
                String name = "s" + id;

                beginState(out, name, options);

                final Map<Pair<S, TP>, List<I>> groupedTransitions = new HashMap<>(HashUtil.capacity(inputs.size()));
                for (I i : inputs) {
                    final T t = automaton.getTransition(state, i);

                    if (t != null) {
                        final S succ = automaton.getSuccessor(t);
                        final TP tp = automaton.getTransitionProperty(t);
                        final Pair<S, TP> key = Pair.of(succ, tp);

                        groupedTransitions.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
                    }
                }

                for (Map.Entry<Pair<S, TP>, List<I>> group : groupedTransitions.entrySet()) {
                    S tgt = group.getKey().getFirst();
                    int tgtId = ids.getStateId(tgt);
                    String tgtName = "s" + tgtId;
                    TP transProp = group.getKey().getSecond();
                    writeTransition(out, group.getValue(), tgtName, transProp);
                }

                endState(out);
            }

            end(out);
        }
    }

    private void begin(Writer out, String type, Collection<?> inputs) throws IOException {
        writeIndent(out);
        out.append(type).append(' ');
        writeStringCollection(out, inputs);
        out.append(" {").append(System.lineSeparator());
        indent++;
    }

    private void beginState(Writer out, String name, Set<String> options) throws IOException {
        writeIndent(out);
        out.append(name).append(' ');
        if (!options.isEmpty()) {
            out.append(options.toString()).append(' ');
        }
        out.append('{').append(System.lineSeparator());
        indent++;
    }

    private void writeTransition(Writer out, Collection<?> symbols, String target, @Nullable Object output)
            throws IOException {
        writeIndent(out);
        writeStringCollection(out, symbols);
        if (output != null) {
            out.append(" / ").append(StringUtil.enquoteIfNecessary(output.toString()));
        }
        out.append(" -> ").append(target).append(System.lineSeparator());
    }

    private void endState(Writer out) throws IOException {
        --indent;
        writeIndent(out);
        out.append('}').append(System.lineSeparator());
    }

    private void end(Writer out) throws IOException {
        --indent;
        writeIndent(out);
        out.append('}').append(System.lineSeparator());
    }

    private void writeIndent(Writer out) throws IOException {
        for (int i = 0; i < indent; i++) {
            out.append('\t');
        }
    }

    private void writeStringCollection(Writer out, Collection<?> symbols) throws IOException {
        if (symbols.isEmpty()) {
            out.append("{}");
        } else if (symbols.size() == 1) {
            Object sym = symbols.iterator().next();
            StringUtil.enquoteIfNecessary(String.valueOf(sym), out, ID_PATTERN);
        } else {
            out.append('{');
            boolean first = true;
            for (Object sym : symbols) {
                if (first) {
                    first = false;
                } else {
                    out.append(',');
                }
                StringUtil.enquoteIfNecessary(String.valueOf(sym), out, ID_PATTERN);
            }
            out.append('}');
        }
    }
}
