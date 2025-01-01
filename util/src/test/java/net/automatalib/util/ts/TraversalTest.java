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
package net.automatalib.util.ts;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.Holder;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.util.ts.traversal.TSTraversalAction;
import net.automatalib.util.ts.traversal.TSTraversalVisitor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TraversalTest {

    private final CompactDFA<Character> tree;
    private final CompactNFA<Character> circular;

    public TraversalTest() {

        this.tree = new CompactDFA<>(Alphabets.characters('0', '9'));

        final Integer init = tree.addInitialState();
        final Integer last1 = addTreeTrace(tree, init, '1', '2', '3', '4', '5');
        final Integer last2 = addTreeTrace(tree, init, '6', '7', '8', '9', '0');

        tree.addTransition(last1, '0', init);
        tree.addTransition(last2, '0', init);

        this.circular = new CompactNFA<>(Alphabets.characters('a', 'c'));
        Integer s0 = circular.addInitialState();
        Integer s1 = circular.addInitialState();
        Integer s2 = circular.addInitialState();
        Integer s3 = circular.addInitialState();
        Integer s4 = circular.addInitialState();

        circular.addTransition(s0, (Character) 'a', s1);
        circular.addTransition(s1, (Character) 'a', s2);
        circular.addTransition(s1, (Character) 'b', s4);
        circular.addTransition(s2, (Character) 'a', s3);
        circular.addTransition(s3, (Character) 'a', s4);
        circular.addTransition(s4, (Character) 'a', s0);
        circular.addTransition(s0, (Character) 'c', s4);
        circular.addTransition(s4, (Character) 'c', s3);
        circular.addTransition(s4, (Character) 'b', s1);
        circular.addTransition(s3, (Character) 'c', s2);
        circular.addTransition(s2, (Character) 'c', s1);
        circular.addTransition(s1, (Character) 'c', s0);
    }

    @SafeVarargs
    private static <S, I> S addTreeTrace(MutableDFA<S, I> dfa, S init, I... symbols) {
        S iter = init;

        for (I i : symbols) {
            final S next = dfa.addState();
            dfa.addTransition(iter, i, next);
            iter = next;
        }

        return iter;
    }

    @Test
    public void testBreadthFirstTraversal() {
        final Iterable<Integer> iter = TSTraversal.breadthFirstOrder(tree, tree.getInputAlphabet());
        Assert.assertEquals(iter, Arrays.asList(0, 1, 6, 2, 7, 3, 8, 4, 9, 5, 10));
    }

    @Test
    public void testBreadthFirstDefault() {
        final DefaultVisitor<Integer, Character, Integer, Void> abcVisitor = new DefaultVisitor<>(0);
        TSTraversal.breadthFirst(circular, Alphabets.characters('a', 'c'), abcVisitor);

        Assert.assertEquals(abcVisitor.getStates(), Arrays.asList(0, 1, 4, 2, 3));

        final DefaultVisitor<Integer, Character, Integer, Void> cbaVisitor = new DefaultVisitor<>(0);
        TSTraversal.breadthFirst(circular, Alphabets.fromArray('c', 'b', 'a'), cbaVisitor);

        Assert.assertEquals(cbaVisitor.getStates(), Arrays.asList(0, 4, 1, 3, 2));
    }

    @Test
    public void testBreadthFirstInitialAbort() {
        final InitialAbortVisitor<Integer, Character, Integer, Void> abortVisitor = new InitialAbortVisitor<>();
        TSTraversal.breadthFirst(circular, circular.getInputAlphabet(), abortVisitor);

        Assert.assertEquals(abortVisitor.getStates(), Collections.emptyList());

        final DefaultVisitor<Integer, Character, Integer, Void> defaultVisitor = new DefaultVisitor<>(0);
        final boolean limit = TSTraversal.breadthFirst(circular, 0, circular.getInputAlphabet(), defaultVisitor);

        Assert.assertFalse(limit);
        Assert.assertEquals(defaultVisitor.getStates(), Collections.emptyList());
    }

    @Test
    public void testBreadthFirstAbortInput() {
        final AbortInputVisitor<Integer, Character, Integer, Void> abcVisitor =
                new AbortInputVisitor<>(circular.getState(0), 'a');
        TSTraversal.breadthFirst(circular, Alphabets.characters('a', 'c'), abcVisitor);

        Assert.assertEquals(abcVisitor.getStates(), Arrays.asList(0, 4, 1, 3, 2));

        final AbortInputVisitor<Integer, Character, Integer, Void> cbaVisitor =
                new AbortInputVisitor<>(circular.getState(0), 'a');
        TSTraversal.breadthFirst(circular, Alphabets.fromArray('c', 'b', 'a'), cbaVisitor);

        Assert.assertEquals(cbaVisitor.getStates(), Arrays.asList(0, 4, 3, 1, 2));
    }

    @Test
    public void testBreadthFirstAbortState() {
        final AbortStateVisitor<Integer, Character, Integer, Void> abcVisitor =
                new AbortStateVisitor<>(circular.getState(0), circular.getState(1));
        TSTraversal.breadthFirst(circular, Alphabets.characters('a', 'c'), abcVisitor);

        Assert.assertEquals(abcVisitor.getStates(), Arrays.asList(0, 1, 4, 3, 2));

        final AbortStateVisitor<Integer, Character, Integer, Void> cbaVisitor =
                new AbortStateVisitor<>(circular.getState(0), circular.getState(1));
        TSTraversal.breadthFirst(circular, Alphabets.fromArray('c', 'b', 'a'), cbaVisitor);

        Assert.assertEquals(cbaVisitor.getStates(), Arrays.asList(0, 4, 1, 3, 2));
    }

    @Test
    public void testDepthFirstTraversal() {
        final Iterable<Integer> iter = TSTraversal.depthFirstOrder(tree, tree.getInputAlphabet());
        Assert.assertEquals(iter, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void testDepthFirstDefault() {
        final DefaultVisitor<Integer, Character, Integer, Void> abcVisitor = new DefaultVisitor<>(0);
        TSTraversal.depthFirst(circular, Alphabets.characters('a', 'c'), abcVisitor);

        Assert.assertEquals(abcVisitor.getStates(), Arrays.asList(0, 1, 2, 3, 4));

        final DefaultVisitor<Integer, Character, Integer, Void> cbaVisitor = new DefaultVisitor<>(0);
        TSTraversal.depthFirst(circular, Alphabets.fromArray('c', 'b', 'a'), cbaVisitor);

        Assert.assertEquals(cbaVisitor.getStates(), Arrays.asList(0, 4, 3, 2, 1));
    }

    @Test
    public void testDepthFirstInitialAbort() {
        final InitialAbortVisitor<Integer, Character, Integer, Void> abortVisitor = new InitialAbortVisitor<>();
        TSTraversal.depthFirst(circular, circular.getInputAlphabet(), abortVisitor);

        Assert.assertEquals(abortVisitor.getStates(), Collections.emptyList());

        final DefaultVisitor<Integer, Character, Integer, Void> defaultVisitor = new DefaultVisitor<>(0);
        final boolean limit = TSTraversal.depthFirst(circular, 0, circular.getInputAlphabet(), defaultVisitor);

        Assert.assertFalse(limit);
        Assert.assertEquals(defaultVisitor.getStates(), Collections.emptyList());
    }

    @Test
    public void testDepthFirstAbortInput() {
        final AbortInputVisitor<Integer, Character, Integer, Void> abcVisitor =
                new AbortInputVisitor<>(circular.getState(0), 'a');
        TSTraversal.depthFirst(circular, Alphabets.characters('a', 'c'), abcVisitor);

        Assert.assertEquals(abcVisitor.getStates(), Arrays.asList(0, 4, 1, 3, 2));

        final AbortInputVisitor<Integer, Character, Integer, Void> cbaVisitor =
                new AbortInputVisitor<>(circular.getState(0), 'a');
        TSTraversal.depthFirst(circular, Alphabets.fromArray('c', 'b', 'a'), cbaVisitor);

        Assert.assertEquals(cbaVisitor.getStates(), Arrays.asList(0, 4, 3, 2, 1));
    }

    @Test
    public void testDepthFirstAbortState() {
        final AbortStateVisitor<Integer, Character, Integer, Void> abcVisitor =
                new AbortStateVisitor<>(circular.getState(0), circular.getState(1));
        TSTraversal.depthFirst(circular, Alphabets.characters('a', 'c'), abcVisitor);

        Assert.assertEquals(abcVisitor.getStates(), Arrays.asList(0, 1, 4, 3, 2));

        final AbortStateVisitor<Integer, Character, Integer, Void> cbaVisitor =
                new AbortStateVisitor<>(circular.getState(0), circular.getState(1));
        TSTraversal.depthFirst(circular, Alphabets.fromArray('c', 'b', 'a'), cbaVisitor);

        Assert.assertEquals(cbaVisitor.getStates(), Arrays.asList(0, 4, 3, 2, 1));
    }

    private abstract static class AbstractVisitor<S, I, T, D> implements TSTraversalVisitor<S, I, T, D> {

        private final Set<S> states;

        AbstractVisitor() {
            this.states = new LinkedHashSet<>();
        }

        @Override
        public boolean startExploration(S state, D data) {
            states.add(state);
            return true;
        }

        @Override
        public TSTraversalAction processTransition(S srcState,
                                                   D srcData,
                                                   I input,
                                                   T transition,
                                                   S tgtState,
                                                   Holder<D> tgtHolder) {
            return states.contains(tgtState) ? TSTraversalAction.IGNORE : TSTraversalAction.EXPLORE;
        }

        public Collection<S> getStates() {
            return states;
        }
    }

    private static final class InitialAbortVisitor<S, I, T, D> extends AbstractVisitor<S, I, T, D> {

        @Override
        public TSTraversalAction processInitial(S initialState, Holder<D> holder) {
            return TSTraversalAction.ABORT_TRAVERSAL;
        }
    }

    private static class DefaultVisitor<S, I, T, D> extends AbstractVisitor<S, I, T, D> {

        private final S initial;

        DefaultVisitor(S initial) {
            this.initial = initial;
        }

        @Override
        public TSTraversalAction processInitial(S initialState, Holder<D> holder) {
            return Objects.equals(initialState, initial) ? TSTraversalAction.EXPLORE : TSTraversalAction.IGNORE;
        }
    }

    private static class AbortInputVisitor<S, I, T, D> extends DefaultVisitor<S, I, T, D> {

        private final I ignored;

        AbortInputVisitor(S initial, I ignored) {
            super(initial);
            this.ignored = ignored;
        }

        @Override
        public TSTraversalAction processTransition(S srcState,
                                                   D srcData,
                                                   I input,
                                                   T transition,
                                                   S tgtState,
                                                   Holder<D> tgtHolder) {
            if (Objects.equals(input, ignored)) {
                return TSTraversalAction.ABORT_INPUT;
            }

            return super.processTransition(srcState, srcData, input, transition, tgtState, tgtHolder);
        }
    }

    private static class AbortStateVisitor<S, I, T, D> extends DefaultVisitor<S, I, T, D> {

        private final S ignored;

        AbortStateVisitor(S initial, S ignored) {
            super(initial);
            this.ignored = ignored;
        }

        @Override
        public TSTraversalAction processTransition(S srcState,
                                                   D srcData,
                                                   I input,
                                                   T transition,
                                                   S tgtState,
                                                   Holder<D> tgtHolder) {
            if (Objects.equals(srcState, ignored)) {
                return TSTraversalAction.ABORT_STATE;
            }

            return super.processTransition(srcState, srcData, input, transition, tgtState, tgtHolder);
        }
    }

}
