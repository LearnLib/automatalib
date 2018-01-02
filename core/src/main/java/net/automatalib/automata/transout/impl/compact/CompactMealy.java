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
package net.automatalib.automata.transout.impl.compact;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.base.compact.AbstractCompactDeterministic;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.words.Alphabet;

public class CompactMealy<I, O> extends AbstractCompactDeterministic<I, CompactMealyTransition<O>, Void, O>
        implements MutableMealyMachine<Integer, I, CompactMealyTransition<O>, O> {

    public CompactMealy(Alphabet<I> alphabet, float resizeFactor) {
        super(alphabet, resizeFactor);
    }

    public CompactMealy(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
    }

    public CompactMealy(Alphabet<I> alphabet, int stateCapacity) {
        super(alphabet, stateCapacity);
    }

    public CompactMealy(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public O getTransitionOutput(CompactMealyTransition<O> transition) {
        return transition.getOutput();
    }

    @Override
    public O getTransitionProperty(CompactMealyTransition<O> transition) {
        return transition.getOutput();
    }

    @Override
    public void setTransitionProperty(CompactMealyTransition<O> transition, O property) {
        transition.setOutput(property);
    }

    @Override
    public void setTransitionOutput(CompactMealyTransition<O> transition, O output) {
        transition.setOutput(output);
    }

    @Override
    public int getIntSuccessor(CompactMealyTransition<O> transition) {
        return transition.getSuccId();
    }

    @Override
    public CompactMealyTransition<O> createTransition(int succId, O property) {
        return new CompactMealyTransition<>(succId, property);
    }

    @Override
    public void setStateProperty(int state, Void property) {
    }

    @Override
    public Void getStateProperty(int stateId) {
        return null;
    }

    @Override
    public CompactMealyTransition<O> copyTransition(CompactMealyTransition<O> trans, int succId) {
        return new CompactMealyTransition<>(succId, trans.getOutput());
    }

    public static final class Creator<I, O> implements AutomatonCreator<CompactMealy<I, O>, I> {

        @Override
        public CompactMealy<I, O> createAutomaton(Alphabet<I> alphabet, int sizeHint) {
            return new CompactMealy<>(alphabet, sizeHint);
        }

        @Override
        public CompactMealy<I, O> createAutomaton(Alphabet<I> alphabet) {
            return new CompactMealy<>(alphabet);
        }
    }

}
