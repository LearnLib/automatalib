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
package net.automatalib.automata.transout.impl;

import net.automatalib.automata.base.fast.AbstractFastMutableNondet;
import net.automatalib.automata.transout.probabilistic.MutableProbabilisticMealy;
import net.automatalib.automata.transout.probabilistic.ProbabilisticOutput;
import net.automatalib.words.Alphabet;

public class FastProbMealy<I, O>
        extends AbstractFastMutableNondet<FastProbMealyState<O>, I, ProbMealyTransition<FastProbMealyState<O>, O>, Void, ProbabilisticOutput<O>>
        implements MutableProbabilisticMealy<FastProbMealyState<O>, I, ProbMealyTransition<FastProbMealyState<O>, O>, O> {

    public FastProbMealy(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    public FastProbMealyState<O> getSuccessor(ProbMealyTransition<FastProbMealyState<O>, O> transition) {
        return transition.getSuccessor();
    }

    @Override
    public O getTransitionOutput(ProbMealyTransition<FastProbMealyState<O>, O> transition) {
        return transition.getOutput();
    }

    @Override
    public Void getStateProperty(FastProbMealyState<O> state) {
        return null;
    }

    @Override
    public ProbabilisticOutput<O> getTransitionProperty(ProbMealyTransition<FastProbMealyState<O>, O> transition) {
        return new ProbabilisticOutput<>(transition.getProbability(), transition.getOutput());
    }

    @Override
    public void setTransitionOutput(ProbMealyTransition<FastProbMealyState<O>, O> transition, O output) {
        transition.setOutput(output);
    }

    @Override
    public void setTransitionProbability(ProbMealyTransition<FastProbMealyState<O>, O> transition, float probability) {
        transition.setProbability(probability);
    }

    @Override
    public float getTransitionProbability(ProbMealyTransition<FastProbMealyState<O>, O> transition) {
        return transition.getProbability();
    }

    @Override
    public void setStateProperty(FastProbMealyState<O> state, Void property) {
    }

    @Override
    public void setTransitionProperty(ProbMealyTransition<FastProbMealyState<O>, O> transition,
                                      ProbabilisticOutput<O> property) {
        float prob;
        O output;
        if (property == null) {
            prob = 0.0f;
            output = null;
        } else {
            prob = property.getProbability();
            output = property.getOutput();
        }
        transition.setProbability(prob);
        transition.setOutput(output);
    }

    @Override
    public ProbMealyTransition<FastProbMealyState<O>, O> createTransition(FastProbMealyState<O> successor,
                                                                          ProbabilisticOutput<O> properties) {
        float prob;
        O output;
        if (properties == null) {
            prob = 0.0f;
            output = null;
        } else {
            prob = properties.getProbability();
            output = properties.getOutput();
        }
        return new ProbMealyTransition<>(successor, output, prob);
    }

    @Override
    protected FastProbMealyState<O> createState(Void property) {
        return new FastProbMealyState<>(inputAlphabet.size());
    }

    public void addTransition(FastProbMealyState<O> src,
                              I input,
                              FastProbMealyState<O> successor,
                              O output,
                              float prob) {
        addTransition(src, input, successor, new ProbabilisticOutput<>(prob, output));
    }

}
