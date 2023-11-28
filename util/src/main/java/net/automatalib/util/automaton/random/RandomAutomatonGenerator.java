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
package net.automatalib.util.automaton.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.automatalib.api.automaton.MutableAutomaton;
import net.automatalib.common.util.collection.CollectionsUtil;
import net.automatalib.common.util.random.RandomUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RandomAutomatonGenerator<S, I, T, SP, TP, A extends MutableAutomaton<S, I, T, SP, TP>> {

    protected final Random random;
    protected final List<? extends I> inputs;
    protected final List<? extends SP> spList;
    protected final List<? extends TP> tpList;
    @SuppressWarnings("PMD.LooseCoupling") // ArrayList#ensureCapacity is required
    protected final ArrayList<S> states;
    protected final A automaton;

    public RandomAutomatonGenerator(Random random,
                                    Collection<? extends I> inputs,
                                    Collection<? extends SP> stateProps,
                                    Collection<? extends TP> transProps,
                                    A automaton) {
        this.random = random;

        this.spList = CollectionsUtil.randomAccessList(stateProps);
        this.tpList = CollectionsUtil.randomAccessList(transProps);

        this.inputs = CollectionsUtil.randomAccessList(inputs);
        this.states = new ArrayList<>();
        this.automaton = automaton;
    }

    public A getAutomaton() {
        return automaton;
    }

    protected @Nullable TP randomTransProperty() {
        return RandomUtil.choose(random, tpList);
    }

    protected @Nullable S randomState() {
        return RandomUtil.choose(random, states);
    }

    protected @Nullable S randomDistinctState(int stateIdx) {
        if (states.size() == 1) {
            return null;
        }

        int idx = random.nextInt(states.size() - 1);

        if (idx >= stateIdx) {
            idx++;
        }

        return states.get(idx);
    }

    protected @Nullable I randomInput() {
        return RandomUtil.choose(random, inputs);
    }

    public void addStates(int numStates) {
        states.ensureCapacity(states.size() + numStates);

        for (int i = 0; i < numStates; i++) {
            S state = automaton.addState(randomStateProperty());
            states.add(state);
        }
    }

    protected @Nullable SP randomStateProperty() {
        return RandomUtil.choose(random, spList);
    }

    public void chooseInitial() {
        S init = RandomUtil.choose(random, states);
        if (init != null) {
            automaton.setInitial(init, true);
        }
    }

    public void chooseInitials(int num) {
        List<S> inits = RandomUtil.sampleUnique(random, states, num);

        for (S init : inits) {
            automaton.setInitial(init, true);
        }
    }

}
