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
import net.automatalib.ts.modal.ModalEdgeProperty.ModalType;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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

}