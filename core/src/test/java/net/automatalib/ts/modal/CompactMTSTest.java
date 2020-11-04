/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.ts.modal;

import java.util.Arrays;
import java.util.Collections;

import net.automatalib.graphs.BidirectionalGraph;
import net.automatalib.ts.modal.transitions.ModalEdgeProperty;
import net.automatalib.ts.modal.transitions.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transitions.ModalEdgePropertyImpl;
import net.automatalib.ts.modal.transitions.MutableModalEdgeProperty;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author msc
 */
public class CompactMTSTest {

    @DataProvider(name = "default")
    private static Object[][] generateTSWithTP() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'd');
        final CompactMTS<Character> mts = new CompactMTS<>(alphabet);

        final Integer s0 = mts.addInitialState();
        final Integer s1 = mts.addState();
        final Integer s2 = mts.addState();
        final ModalEdgeProperty tprop1 = new ModalEdgePropertyImpl(null);
        final ModalEdgeProperty tprop2 = new ModalEdgePropertyImpl(ModalType.MAY);

        return new Object[][] {{mts, s0, s1, 'a', tprop1}, {mts, s0, s2, 'd', tprop2}, {mts, s2, s0, 'a', tprop2}};
    }

    @Test(dataProvider = "default", description = "Add Transition with non-null Property")
    <A extends MutableModalTransitionSystem<S, I, T, TP> & BidirectionalGraph<S, T>, S, I, T, TP extends MutableModalEdgeProperty> void addTransitionWithTP(
            A mts,
            S src,
            S dest,
            I label,
            TP tprop) {
        Assert.assertTrue(mts.getStates().containsAll(Arrays.asList(src, dest)));
        Assert.assertTrue(mts.getInputAlphabet().contains(label));
        Assert.assertTrue(mts.getTransitions(src, label).isEmpty());
        Assert.assertNotNull(tprop);

        int tsize = 0;
        for (S s : mts.getStates()) {
            tsize += mts.getOutgoingEdges(s).size();
        }

        T t = mts.addTransition(src, label, dest, tprop);
        Assert.assertNotNull(t);
        Assert.assertEquals(mts.getSource(t), src);
        Assert.assertEquals(mts.getSuccessor(t), dest);
        Assert.assertEquals(mts.getEdgeLabel(t), label);
        Assert.assertEquals(mts.getTransitionProperty(t), tprop);
        Assert.assertEquals(mts.getTransitions(src, label), Collections.singleton(t));
        Assert.assertTrue(mts.getIncomingEdges(dest).contains(t));

        int tsizeL = 0;
        for (S s : mts.getStates()) {
            tsizeL += mts.getOutgoingEdges(s).size();
        }
        Assert.assertEquals(tsizeL, tsize + 1);
    }

    @Test(dataProvider = "default", description = "Add Transition with null as Property")
    <A extends MutableModalTransitionSystem<S, I, T, TP> & BidirectionalGraph<S, T>, S, I, T, TP extends MutableModalEdgeProperty> void addTransitionWithoutTP(
            A mts,
            S src,
            S dest,
            I label,
            TP tprop) {

        Assert.assertTrue(mts.getStates().containsAll(Arrays.asList(src, dest)));
        Assert.assertTrue(mts.getInputAlphabet().contains(label));
        Assert.assertTrue(mts.getTransitions(src, label).isEmpty());

        T t = mts.addTransition(src, label, dest, null);
        Assert.assertNotNull(t);
        Assert.assertEquals(mts.getSource(t), src);
        Assert.assertEquals(mts.getSuccessor(t), dest);
        Assert.assertEquals(mts.getEdgeLabel(t), label);
        Assert.assertNotNull(mts.getTransitionProperty(t));
        Assert.assertEquals(mts.getTransitions(src, label), Collections.singleton(t));
        Assert.assertTrue(mts.getIncomingEdges(dest).contains(t));
    }

    @Test(dataProvider = "default", description = "Create Transition with null as Property and then add it")
    <A extends MutableModalTransitionSystem<S, I, T, TP> & BidirectionalGraph<S, T>, S, I, T, TP extends MutableModalEdgeProperty> void addTransitionWithExternT(
            A mts,
            S src,
            S dest,
            I label,
            TP tprop) {
        Assert.assertTrue(mts.getStates().containsAll(Arrays.asList(src, dest)));
        Assert.assertTrue(mts.getInputAlphabet().contains(label));
        Assert.assertTrue(mts.getTransitions(src, label).isEmpty());

        T t = mts.createTransition(dest, null);
        mts.addTransition(src, label, t);
        Assert.assertNotNull(t);
        Assert.assertEquals(mts.getSource(t), src);
        Assert.assertEquals(mts.getSuccessor(t), dest);
        Assert.assertEquals(mts.getEdgeLabel(t), label);
        Assert.assertNotNull(mts.getTransitionProperty(t));
        Assert.assertEquals(mts.getTransitions(src, label), Collections.singleton(t));
        Assert.assertTrue(mts.getIncomingEdges(dest).contains(t));
    }

    @Test(dataProvider = "default", description = "Create Transition with non-null Property and then add it")
    <A extends MutableModalTransitionSystem<S, I, T, TP> & BidirectionalGraph<S, T>, S, I, T, TP extends MutableModalEdgeProperty> void addTransitionWithExternT2(
            A mts,
            S src,
            S dest,
            I label,
            TP tprop) {
        Assert.assertTrue(mts.getStates().containsAll(Arrays.asList(src, dest)));
        Assert.assertTrue(mts.getInputAlphabet().contains(label));
        Assert.assertTrue(mts.getTransitions(src, label).isEmpty());

        T t = mts.createTransition(dest, tprop);
        mts.addTransition(src, label, t);
        Assert.assertNotNull(t);
        Assert.assertEquals(mts.getSource(t), src);
        Assert.assertEquals(mts.getSuccessor(t), dest);
        Assert.assertEquals(mts.getEdgeLabel(t), label);
        Assert.assertNotNull(mts.getTransitionProperty(t));
        Assert.assertEquals(mts.getTransitions(src, label), Collections.singleton(t));
        Assert.assertTrue(mts.getIncomingEdges(dest).contains(t));
    }


    @Test(description = "Example graph from Japser")
    void testJasper() {
        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'b');
        final CompactMTS<String> s = new CompactMTS<>(alphabet);

        final Integer as0 = s.addInitialState();
        final Integer as1 = s.addState();
        final Integer as2 = s.addState();

        final MTSTransition<String, MutableModalEdgeProperty> t1 = s.addTransition(as0, "a", as0, new ModalEdgePropertyImpl(ModalType.MUST));
        final MTSTransition<String, MutableModalEdgeProperty> t2 = s.addTransition(as0, "b", as1, new ModalEdgePropertyImpl(ModalType.MUST));
        final MTSTransition<String, MutableModalEdgeProperty> t3 = s.addTransition(as1, "a", as1, new ModalEdgePropertyImpl(ModalType.MUST));
        final MTSTransition<String, MutableModalEdgeProperty> t4 = s.addTransition(as1, "b", as2, new ModalEdgePropertyImpl(ModalType.MAY));
        final MTSTransition<String, MutableModalEdgeProperty> t5 = s.addTransition(as2, "a", as2, new ModalEdgePropertyImpl(ModalType.MAY));
        final MTSTransition<String, MutableModalEdgeProperty> t6 = s.addTransition(as2, "b", as2, new ModalEdgePropertyImpl(ModalType.MAY));

        assertThat(s.getTransitions(as0, "a"))
                .hasSize(1)
                .extracting("target")
                .containsExactly(as0);

        assertThat(s.getSource(t1))
                .as("Source of 0 -a-> 0 is 0")
                .isEqualTo(as0);
        assertThat(s.getSource(t2))
                .as("Source of 0 -b-> 1 is 0")
                .isEqualTo(as0);
        assertThat(s.getSource(t3))
                .as("Source of 1 -a-> 1 is 1")
                .isEqualTo(as1);
        assertThat(s.getSource(t4))
                .as("Source of 1 -b-> 2 is 1")
                .isEqualTo(as1);
        assertThat(s.getSource(t5))
                .as("Source of 2 -a-> 2 is 2")
                .isEqualTo(as2);
        assertThat(s.getSource(t6))
                .as("Source of 2 -b-> 2 is 2")
                .isEqualTo(as2);

        assertThat(s.getTransitions(as0, "b"))
                .hasSize(1)
                .allMatch(t -> t.getTarget() == as1)
                .allMatch(t -> t.getProperty().getType() == ModalType.MUST)
                .allMatch(t -> "b".equals(t.getLabel()))
                .allMatch(t -> t.getSource() == as0);

        assertThat(s.getTransitions(as1, "a"))
                .hasSize(1)
                .allMatch(t -> t.getTarget() == as1)
                .allMatch(t -> t.getProperty().getType() == ModalType.MUST)
                .allMatch(t -> "a".equals(t.getLabel()))
                .allMatch(t -> t.getSource() == as1);

        assertThat(s.getTransitions(as1, "b"))
                .hasSize(1)
                .allMatch(t -> t.getTarget() == as2)
                .allMatch(t -> t.getProperty().getType() == ModalType.MAY)
                .allMatch(t -> "b".equals(t.getLabel()))
                .allMatch(t -> t.getSource() == as1);

        assertThat(s.getTransitions(as2, "a"))
                .hasSize(1)
                .allMatch(t -> t.getTarget() == as2)
                .allMatch(t -> t.getProperty().getType() == ModalType.MAY)
                .allMatch(t -> "a".equals(t.getLabel()))
                .allMatch(t -> t.getSource() == as2);

        assertThat(s.getTransitions(as2, "b"))
                .hasSize(1)
                .allMatch(t -> t.getTarget() == as2)
                .allMatch(t -> t.getProperty().getType() == ModalType.MAY)
                .allMatch(t -> "b".equals(t.getLabel()))
                .allMatch(t -> t.getSource() == as2);
    }
}