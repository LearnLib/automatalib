/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
package net.automatalib.serialization.taf.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.common.util.string.StringUtil;

@SuppressWarnings("nullness")
abstract class AbstractTAFBuilder<S, T, SP, TP, M extends MutableDeterministic<S, String, T, SP, TP>>
        implements TAFBuilder {

    private static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    private final InternalTAFParser parser;
    private final Map<String, S> stateMap = new HashMap<>();
    private final Set<String> declaredStates = new HashSet<>();
    protected M automaton;
    private Alphabet<String> alphabet;

    AbstractTAFBuilder(InternalTAFParser parser) {
        this.parser = parser;
    }

    @Override
    public void init(Alphabet<String> alphabet) {
        if (automaton != null) {
            throw new IllegalStateException();
        }
        automaton = createAutomaton(alphabet);
        this.alphabet = alphabet;
    }

    @Override
    public void declareState(String identifier, Set<String> options) {
        if (!declaredStates.add(identifier)) {
            error("State {0} declared twice, ignoring properties ...", identifier);
            return;
        }

        boolean init = options.remove("initial") | options.remove("init");
        if (init && automaton.getInitialState() != null) {
            error("Duplicate initial state {0}, ignoring property ...", identifier);
            init = false;
        }

        S state = stateMap.get(identifier);
        SP property = getStateProperty(options);
        if (state == null) {
            state = init ? automaton.addInitialState(property) : automaton.addState(property);
            stateMap.put(identifier, state);
        } else {
            automaton.setStateProperty(state, property);
            if (init) {
                automaton.setInitialState(state);
            }
        }

        if (!options.isEmpty()) {
            warning("Unrecognized options for state {0}: {1}", identifier, options);
        }
    }

    @Override
    public M finish() {
        if (automaton == null) {
            throw new IllegalStateException("Must call one of the parse methods first");
        }

        stateMap.clear();
        declaredStates.clear();
        M result = automaton;
        automaton = null;
        alphabet = null;
        return result;
    }

    protected void error(String msgFmt, Object... args) {
        parser.error(msgFmt, args);
    }

    protected abstract M createAutomaton(Alphabet<String> stringAlphabet);

    protected abstract SP getStateProperty(Set<String> options);

    protected void warning(String msgFmt, Object... args) {
        parser.warning(msgFmt, args);
    }

    protected void doAddTransitions(String source, Collection<String> symbols, String target, TP transProperty) {
        S src = lookupState(source);
        S tgt = lookupState(target);
        List<String> invalidSymbols = new ArrayList<>();
        for (String input : symbols) {
            if (!alphabet.containsSymbol(input)) {
                invalidSymbols.add(StringUtil.enquoteIfNecessary(input, ID_PATTERN));
                continue;
            }
            T exTrans = automaton.getTransition(src, input);
            if (exTrans != null) {
                if (!Objects.equals(tgt, automaton.getSuccessor(exTrans))) {
                    error("Duplicate transition from {0} on input {1} to differing target {2} would introduce non-determinism",
                          source,
                          StringUtil.enquoteIfNecessary(input, ID_PATTERN),
                          tgt);
                } else if (!Objects.equals(transProperty, automaton.getTransitionProperty(exTrans))) {
                    error("Duplicate transition from {0} on input {1} to {2} with differing property '{3}' would introduce non-determinism",
                          source,
                          StringUtil.enquoteIfNecessary(input, ID_PATTERN),
                          tgt,
                          transProperty);
                }
            } else {
                automaton.addTransition(src, input, tgt, transProperty);
            }
        }
        if (!invalidSymbols.isEmpty()) {
            error("Invalid symbols for transition from {0} to {1}: {2}", source, target, invalidSymbols);
        }
    }

    protected S lookupState(String identifier) {
        return stateMap.computeIfAbsent(identifier, k -> automaton.addState());
    }

    protected void doAddWildcardTransitions(String source, String target, TP transProperty) {
        S src = lookupState(source);
        S tgt = lookupState(target);
        for (String input : alphabet) {
            T exTrans = automaton.getTransition(src, input);
            if (exTrans == null) {
                automaton.addTransition(src, input, tgt, transProperty);
            }
        }
    }

    protected Alphabet<String> getAlphabet() {
        return alphabet;
    }
}
