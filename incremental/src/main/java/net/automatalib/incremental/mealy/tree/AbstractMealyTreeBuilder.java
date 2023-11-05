/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.incremental.mealy.tree;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.incremental.mealy.MealyBuilder;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
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
        return doFindSeparatingWord(target, inputs, omitUndefined);
    }

    private <S, T> @Nullable Word<I> doFindSeparatingWord(MealyMachine<S, I, T, O> target,
                                                          Collection<? extends I> inputs,
                                                          boolean omitUndefined) {
        S automatonInit = target.getInitialState();

        if (automatonInit == null) {
            return omitUndefined ? null : Word.epsilon();
        }

        Deque<Record<@Nullable S, N, I>> dfsStack = new ArrayDeque<>();

        // reachedFrom can be null here, because we will always skip the bottom stack element below
        @SuppressWarnings("nullness")
        final Record<@Nullable S, N, I> init = new Record<>(automatonInit, root, null, inputs.iterator());

        dfsStack.push(init);

        while (!dfsStack.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull Record<@Nullable S, N, I> rec = dfsStack.peek();
            if (!rec.inputIt.hasNext()) {
                dfsStack.pop();
                continue;
            }
            I input = rec.inputIt.next();
            Edge<N, O> edge = getEdge(rec.treeNode, input);
            if (edge == null) {
                continue;
            }

            S state = rec.automatonState;
            T trans = state == null ? null : target.getTransition(state, input);
            if (omitUndefined && trans == null) {
                continue;
            }
            if (trans == null || !Objects.equals(target.getTransitionOutput(trans), edge.getOutput())) {

                WordBuilder<I> wb = new WordBuilder<>(dfsStack.size());
                wb.append(input);
                dfsStack.pop();

                while (!dfsStack.isEmpty()) {
                    wb.append(rec.incomingInput);
                    rec = dfsStack.pop();
                }
                return wb.reverse().toWord();
            }

            final Record<@Nullable S, N, I> nextRecord =
                    new Record<>(target.getSuccessor(trans), edge.getTarget(), input, inputs.iterator());
            dfsStack.push(nextRecord);
        }

        return null;
    }

    @Override
    public MealyTransitionSystem<?, I, ?, O> asTransitionSystem() {
        return new TransitionSystemView();
    }

    abstract @Nullable Edge<N, O> getEdge(N node, I symbol);

    abstract N createNode();

    abstract N insertNode(N parent, I symIdx, O output);

    private static final class Record<S, N, I> {

        private final S automatonState;
        private final N treeNode;
        private final I incomingInput;
        private final Iterator<? extends I> inputIt;

        Record(S automatonState, N treeNode, I incomingInput, Iterator<? extends I> inputIt) {
            this.automatonState = automatonState;
            this.treeNode = treeNode;
            this.inputIt = inputIt;
            this.incomingInput = incomingInput;
        }
    }

    class TransitionSystemView implements MealyTransitionSystem<N, I, Edge<N, O>, O> {

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
