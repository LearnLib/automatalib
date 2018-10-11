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
package net.automatalib.modelcheckers.ltsmin.ltl;

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
import net.automatalib.modelcheckers.ltsmin.LTSminDFA;
import net.automatalib.modelchecking.Lasso.DFALasso;
import net.automatalib.modelchecking.ModelCheckerLasso.DFAModelCheckerLasso;
import net.automatalib.modelchecking.lasso.DFALassoImpl;
import net.automatalib.serialization.fsm.parser.FSM2DFAParser;
import net.automatalib.serialization.fsm.parser.FSMParseException;

/**
 * An LTL model checker using LTSmin for DFAs.
 *
 * @param <I>
 *         the input type
 *
 * @author Jeroen Meijer
 */
public class LTSminLTLDFA<I> extends AbstractLTSminLTL<I, DFA<?, I>, DFALasso<I>>
        implements DFAModelCheckerLasso<I, String>, LTSminDFA<I, DFALasso<I>> {

    /**
     * The index in the FSM state vector for accept/reject.
     */
    public static final String LABEL_NAME = "label";

    /**
     * The value in the state vector for acceptance.
     */
    public static final String LABEL_VALUE = "accept";

    @GenerateBuilder(defaults = AbstractLTSminLTL.BuilderDefaults.class)
    public LTSminLTLDFA(boolean keepFiles, Function<String, I> string2Input, int minimumUnfolds, double multiplier) {
        super(keepFiles, string2Input, minimumUnfolds, multiplier);
    }

    /**
     * Converts the FSM file to a {@link DFALasso}.
     *
     * @param automaton
     *         the DFA used to compute the number of loop unrolls.
     *
     * @see AbstractLTSminLTL#findCounterExample(Object, Collection, Object)
     */
    @Nullable
    @Override
    public DFALasso<I> findCounterExample(DFA<?, I> automaton, Collection<? extends I> inputs, String property)
            throws ModelCheckingException {
        final File fsm = findCounterExampleFSM(automaton, inputs, property);

        final DFALasso<I> result;

        if (fsm != null) {
            final CompactDFA<I> dfa;

            try {
                dfa = FSM2DFAParser.parse(fsm, Optional.of(inputs), getString2Input(), LABEL_NAME, LABEL_VALUE);

                // check if we must keep the FSM
                if (!isKeepFiles() && !fsm.delete()) {
                    throw new ModelCheckingException("Could not delete file: " + fsm.getAbsolutePath());
                }
            } catch (IOException | FSMParseException e) {
                throw new ModelCheckingException(e);
            }

            result = new DFALassoImpl<>(dfa, dfa.getInputAlphabet(), computeUnfolds(automaton.size()));
        } else {
            result = null;
        }

        return result;
    }
}
