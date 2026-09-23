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
package net.automatalib.incremental.mealy.tree;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.incremental.mealy.MealyBuilder;
import net.automatalib.incremental.util.DFSepWordFinder;
import net.automatalib.incremental.util.DFSepWordFinder.Visitor;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractMealyTreeBuilder<N, I, O> implements MealyBuilder<I, O> {

    final N root;

    AbstractMealyTreeBuilder(N root) {
        this.root = root;
    }

    @Override
    public boolean lookup(Word<? extends I> word, List<? super O> output) {
        N curr = root;

        for (I sym : word) {
            Edge<N, O> edge = getEdge(curr, sym);
            if (edge == null) {
                return false;
            }
            output.add(edge.getOutput());
            curr = edge.getTarget();
        }

        return true;
    }

    @Override
    public @Nullable Word<I> findSeparatingWord(MealyMachine<?, I, ?, O> target,
                                                Collection<? extends I> inputs,
                                                boolean omitUndefined) {
        return doFindSeparatingWord(asTransitionSystem(), target, inputs, omitUndefined);
    }

    private <S1, S2, T1, T2> @Nullable Word<I> doFindSeparatingWord(MealyMachine<S1, I, T1, O> tree,
                                                                    MealyMachine<S2, I, T2, O> target,
                                                                    Collection<? extends I> inputs,
                                                                    boolean omitUndefined) {

        final Visitor<S1, S2, T1, T2> visitor = new Visitor<>() {

            @Override
            public boolean succConflict(T1 incTrans, @Nullable T2 targetTrans) {
                return targetTrans == null ||
                       !Objects.equals(tree.getTransitionOutput(incTrans), target.getTransitionOutput(targetTrans));
            }
        };

        return DFSepWordFinder.findSeparatingWord(tree, target, inputs, omitUndefined, visitor);
    }

    abstract @Nullable Edge<N, O> getEdge(N node, I symbol);

    abstract N createNode();

    abstract N insertNode(N parent, I symIdx, O output);

    abstract class AbstractAutomatonView implements MealyMachine<N, I, Edge<N, O>, O> {

        @Override
        public @Nullable Edge<N, O> getTransition(N state, I input) {
            return getEdge(state, input);
        }

        @Override
        public N getSuccessor(Edge<N, O> transition) {
            return transition.getTarget();
        }

        @Override
        public N getInitialState() {
            return root;
        }

        @Override
        public O getTransitionOutput(Edge<N, O> transition) {
            return transition.getOutput();
        }
    }
}
