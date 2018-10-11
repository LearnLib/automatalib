/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.modelcheckers.ltsmin.monitor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.modelcheckers.ltsmin.AbstractLTSmin;
import net.automatalib.modelcheckers.ltsmin.LTSminDFA;
import net.automatalib.serialization.fsm.parser.FSM2DFAParser;
import net.automatalib.serialization.fsm.parser.FSMParseException;

/**
 * A monitor model checker using LTSmin for DFAs.
 *
 * @param <I>
 *         the input type
 *
 * @author Jeroen Meijer
 */
public class LTSminMonitorDFA<I> extends AbstractLTSminMonitor<I, DFA<?, I>, DFA<?, I>>
        implements LTSminDFA<I, DFA<?, I>> {

    @GenerateBuilder(defaults = BuilderDefaults.class)
    public LTSminMonitorDFA(boolean keepFiles, Function<String, I> string2Input) {
        super(keepFiles, string2Input);
    }

    /**
     * Converts the FSM file to a {@link DFA}.
     *
     * @see AbstractLTSmin#findCounterExample(Object, Collection, Object)
     */
    @Nullable
    @Override
    public DFA<?, I> findCounterExample(DFA<?, I> automaton, Collection<? extends I> inputs, String property)
            throws ModelCheckingException {
        final File fsm = findCounterExampleFSM(automaton, inputs, property);

        try {
            final CompactDFA<I> result;
            if (fsm != null) {
                result = FSM2DFAParser.parse(fsm, Optional.of(inputs), getString2Input(), LABEL_NAME, LABEL_VALUE);

                // check if we must keep the FSM
                if (!isKeepFiles() && !fsm.delete()) {
                    throw new ModelCheckingException("Could not delete file: " + fsm.getAbsolutePath());
                }

                for (Integer state : result.getStates()) {
                    final boolean deadlocks = result.getInputAlphabet().stream().noneMatch(
                            i -> result.getSuccessor(state, i) != null);
                    result.setAccepting(state, deadlocks);
                }
            } else {
                result = null;
            }

            return result;
        } catch (IOException | FSMParseException e) {
            throw new ModelCheckingException(e);
        }
    }
}
