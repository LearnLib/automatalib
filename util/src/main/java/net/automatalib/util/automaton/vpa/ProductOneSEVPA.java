/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.util.automaton.vpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.automaton.vpa.impl.AbstractSEVPA;
import net.automatalib.common.util.Pair;
import net.automatalib.util.ts.acceptor.AcceptanceCombiner;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Production automaton that allows to join two given {@link OneSEVPA}s. Acceptance semantics of the product automaton
 * depends on the given {@link AcceptanceCombiner}.
 *
 * @param <L1>
 *         location type of first 1-SEVPA
 * @param <L2>
 *         location type of second 1-SEVPA
 * @param <I>
 *         input alphabet type
 */
public class ProductOneSEVPA<L1, L2, I> extends AbstractSEVPA<Pair<L1, L2>, I> implements OneSEVPA<Pair<L1, L2>, I> {

    private final OneSEVPA<L1, I> sevpa1;
    private final OneSEVPA<L2, I> sevpa2;

    private final AcceptanceCombiner accCombiner;

    private final StateIDs<L1> stateIDs1;
    private final StateIDs<L2> stateIDs2;

    public ProductOneSEVPA(VPAlphabet<I> alphabet,
                           OneSEVPA<L1, I> sevpa1,
                           OneSEVPA<L2, I> sevpa2,
                           AcceptanceCombiner combiner) {
        super(alphabet);
        this.sevpa1 = sevpa1;
        this.sevpa2 = sevpa2;
        this.accCombiner = combiner;
        this.stateIDs1 = sevpa1.stateIDs();
        this.stateIDs2 = sevpa2.stateIDs();
    }

    @Override
    public @Nullable Pair<L1, L2> getInternalSuccessor(Pair<L1, L2> loc, I intSym) {
        final L1 succ1 = sevpa1.getInternalSuccessor(loc.getFirst(), intSym);
        if (succ1 == null) {
            return null;
        }
        final L2 succ2 = sevpa2.getInternalSuccessor(loc.getSecond(), intSym);
        if (succ2 == null) {
            return null;
        }
        return Pair.of(succ1, succ2);
    }

    @Override
    public Collection<Pair<L1, L2>> getTransitions(Pair<L1, L2> state, I input) {
        final Collection<L1> t1 = sevpa1.getTransitions(state.getFirst(), input);
        final Collection<L2> t2 = sevpa2.getTransitions(state.getSecond(), input);

        final List<Pair<L1, L2>> result = new ArrayList<>(t1.size() * t2.size());

        for (L1 l1 : t1) {
            for (L2 l2 : t2) {
                result.add(Pair.of(l1, l2));
            }
        }

        return result;
    }

    @Override
    public Pair<L1, L2> getSuccessor(Pair<L1, L2> transition) {
        return transition;
    }

    @Override
    public List<Pair<L1, L2>> getStates() {
        final List<Pair<L1, L2>> locations = new ArrayList<>(sevpa1.size() * sevpa2.size());

        for (L1 l1 : sevpa1.getStates()) {
            for (L2 l2 : sevpa2.getStates()) {
                locations.add(Pair.of(l1, l2));
            }
        }

        return locations;
    }

    @Override
    public @Nullable Pair<L1, L2> getReturnSuccessor(Pair<L1, L2> loc, I retSym, int stackSym) {
        final int stackSym1 = stackSym / sevpa2.getNumStackSymbols();
        final L1 succ1 = sevpa1.getReturnSuccessor(loc.getFirst(), retSym, stackSym1);
        if (succ1 == null) {
            return null;
        }
        final int stackSym2 = stackSym % sevpa2.getNumStackSymbols();
        final L2 succ2 = sevpa2.getReturnSuccessor(loc.getSecond(), retSym, stackSym2);
        if (succ2 == null) {
            return null;
        }
        return Pair.of(succ1, succ2);
    }

    @Override
    public Boolean getStateProperty(Pair<L1, L2> loc) {
        return accCombiner.combine(sevpa1.getStateProperty(loc.getFirst()), sevpa2.getStateProperty(loc.getSecond()));
    }

    @Override
    public Void getTransitionProperty(Pair<L1, L2> transition) {
        return null;
    }

    @Override
    public Pair<L1, L2> getInitialState() {
        return Pair.of(sevpa1.getInitialState(), sevpa2.getInitialState());
    }

    @Override
    public int encodeStackSym(Pair<L1, L2> srcLoc, I callSym) {
        final int stackSym1 = sevpa1.encodeStackSym(srcLoc.getFirst(), callSym);
        final int stackSym2 = sevpa2.encodeStackSym(srcLoc.getSecond(), callSym);
        return stackSym1 * sevpa2.getNumStackSymbols() + stackSym2;
    }

    @Override
    public int getNumStackSymbols() {
        return sevpa1.getNumStackSymbols() * sevpa2.getNumStackSymbols();
    }

    @Override
    public int size() {
        return sevpa1.size() * sevpa2.size();
    }

    @Override
    public int getStateId(Pair<L1, L2> state) {
        return stateIDs1.getStateId(state.getFirst()) * sevpa2.size() + stateIDs2.getStateId(state.getSecond());
    }

    @Override
    public Pair<L1, L2> getState(int id) {
        final int l1Id = id / sevpa2.size();
        final int l2Id = id % sevpa2.size();
        return Pair.of(stateIDs1.getState(l1Id), stateIDs2.getState(l2Id));
    }
}
