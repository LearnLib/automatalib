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
package net.automatalib.modelchecker.ltsmin.monitor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.exception.FormatException;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.modelchecker.ltsmin.AbstractLTSmin;
import net.automatalib.modelchecker.ltsmin.LTSminDFA;
import net.automatalib.modelchecker.ltsmin.LTSminLTLParser;
import net.automatalib.serialization.fsm.parser.FSM2DFAParser;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A monitor model checker using LTSmin for DFAs.
 *
 * @param <I>
 *         the input type
 */
public class LTSminMonitorDFA<I> extends AbstractLTSminMonitor<I, DFA<?, I>, DFA<?, I>>
        implements LTSminDFA<I, DFA<?, I>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LTSminMonitorDFA.class);

    @GenerateBuilder(defaults = BuilderDefaults.class)
    public LTSminMonitorDFA(boolean keepFiles, Function<String, I> string2Input) {
        super(keepFiles, string2Input);
    }

    @Override
    protected void verifyFormula(String formula) throws FormatException {
        LTSminLTLParser.requireValidLetterFormula(formula);
    }

    /**
     * Converts the FSM file to a {@link DFA}.
     *
     * @see AbstractLTSmin#findCounterExample(Object, Collection, Object)
     */
    @Override
    public @Nullable DFA<?, I> findCounterExample(DFA<?, I> automaton, Collection<? extends I> inputs, String property) {
        final File fsm = findCounterExampleFSM(automaton, inputs, property);

        if (fsm == null) {
            return null;
        }

        try {
            final CompactDFA<I> result =
                    FSM2DFAParser.getParser(inputs, getString2Input(), LABEL_NAME, LABEL_VALUE).readModel(fsm);

            for (Integer state : result) {
                final boolean deadlocks = inputs.stream().noneMatch(i -> result.getSuccessor(state, i) != null);
                result.setAccepting(state, deadlocks);
            }

            return result;
        } catch (IOException | FormatException e) {
            throw new ModelCheckingException(e);
        } finally {
            // check if we must keep the FSM
            if (!isKeepFiles() && !fsm.delete()) {
                LOGGER.warn("Could not delete file: '{}'", fsm.getAbsolutePath());
            }
        }
    }
}
