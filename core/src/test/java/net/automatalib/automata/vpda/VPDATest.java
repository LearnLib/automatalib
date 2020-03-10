/* Copyright (C) 2013-2020 TU Dortmund
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
package net.automatalib.automata.vpda;

import java.util.Collections;
import java.util.HashSet;

import net.automatalib.automata.vpda.AbstractOneSEVPA.SevpaViewEdge;
import net.automatalib.graphs.Graph;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultVPDAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class VPDATest {

    /**
     * Tests the language of correctly matched brace-words, which can be described by the EBNF
     * <code>S -> ( S ) | [ S ] | () | []</code>.
     */
    @Test
    public void testBracketLanguage() {

        final Alphabet<Character> callAlphabet = Alphabets.fromArray('(', '[');
        final Alphabet<Character> creturnAlphabet = Alphabets.fromArray(')', ']');
        final VPDAlphabet<Character> alphabet =
                new DefaultVPDAlphabet<>(Collections.emptyList(), callAlphabet, creturnAlphabet);

        final DefaultOneSEVPA<Character> vpda = new DefaultOneSEVPA<>(alphabet);

        final Location init = vpda.addInitialLocation(false);
        final Location accepting = vpda.addLocation(true);

        vpda.setReturnSuccessor(init, ')', vpda.encodeStackSym(init, callAlphabet.getSymbolIndex('(')), accepting);
        vpda.setReturnSuccessor(init, ']', vpda.encodeStackSym(init, callAlphabet.getSymbolIndex('[')), accepting);
        vpda.setReturnSuccessor(accepting, ')', vpda.encodeStackSym(init, callAlphabet.getSymbolIndex('(')), accepting);
        vpda.setReturnSuccessor(accepting, ']', vpda.encodeStackSym(init, callAlphabet.getSymbolIndex('[')), accepting);

        Assert.assertTrue(vpda.accepts(Word.fromCharSequence("(([[]]))")));
        Assert.assertTrue(vpda.accepts(Word.fromCharSequence("([([])])")));
        Assert.assertTrue(vpda.accepts(Word.fromCharSequence("[(())]")));

        Assert.assertFalse(vpda.accepts(Word.fromCharSequence("([([")));
        Assert.assertFalse(vpda.accepts(Word.fromCharSequence("(((]]]")));
        Assert.assertFalse(vpda.accepts(Word.fromCharSequence(")(")));
        Assert.assertFalse(vpda.accepts(Word.fromCharSequence("()()")));
    }

    /**
     * Test case for reported issue <a href="https://github.com/LearnLib/automatalib/pull/39">#39</a>.
     */
    @Test
    public void testGraphRepresentation() {

        final Alphabet<Integer> callAlphabet = Alphabets.integers(1, 10);
        final Alphabet<Integer> internalAlphabet = Alphabets.integers(11, 20);
        final Alphabet<Integer> returnAlphabet = Alphabets.integers(21, 30);
        final VPDAlphabet<Integer> alphabet = new DefaultVPDAlphabet<>(internalAlphabet, callAlphabet, returnAlphabet);

        // create arbitrary VPA
        final DefaultOneSEVPA<Integer> vpa = new DefaultOneSEVPA<>(alphabet);
        final Location init = vpa.addInitialLocation(false);
        final Location accepting = vpa.addLocation(true);

        // criss-cross internal successors
        for (final Integer i : internalAlphabet) {
            final Location initSucc;
            final Location accSucc;

            if (i % 2 == 0) {
                initSucc = init;
                accSucc = accepting;
            } else {
                initSucc = accepting;
                accSucc = initSucc;
            }

            vpa.setInternalSuccessor(init, i, initSucc);
            vpa.setInternalSuccessor(accepting, i, accSucc);
        }

        // criss-cross return successors
        for (final Integer r : returnAlphabet) {

            for (int i = 0; i < callAlphabet.size(); i++) {

                final Location initSucc;
                final Location accSucc;

                final int initSym = vpa.encodeStackSym(init, i);
                final int accSym = vpa.encodeStackSym(accepting, i);

                if (i % 2 == 0) {
                    initSucc = init;
                    accSucc = accepting;
                } else {
                    initSucc = accepting;
                    accSucc = initSucc;
                }

                vpa.setReturnSuccessor(init, r, initSym, initSucc);
                vpa.setReturnSuccessor(init, r, accSym, accSucc);
                vpa.setReturnSuccessor(accepting, r, initSym, accSucc);
                vpa.setReturnSuccessor(accepting, r, accSym, initSucc);
            }
        }

        verifyGraphRepresentation(alphabet, vpa, vpa);
    }

    private static <L, I> void verifyGraphRepresentation(VPDAlphabet<I> alphabet,
                                                         OneSEVPA<L, I> vpa,
                                                         Graph<L, SevpaViewEdge<L, I>> graph) {

        Assert.assertEquals(new HashSet<>(vpa.getLocations()), new HashSet<>(graph.getNodes()));

        for (final L loc : vpa.getLocations()) {
            for (SevpaViewEdge<L, I> edge : graph.getOutgoingEdges(loc)) {

                final I input = edge.input;
                final int stack = edge.stack;
                final L target = edge.target;

                switch (alphabet.getSymbolType(input)) {
                    case CALL:
                        throw new IllegalStateException("Call edges are implicit in a 1-SEVPA");
                    case INTERNAL:
                        Assert.assertEquals(vpa.getInternalSuccessor(loc, input), target);
                        continue;
                    case RETURN:
                        Assert.assertEquals(vpa.getReturnSuccessor(loc, input, stack), target);
                        continue;
                    default:
                        throw new IllegalStateException("Unknown symbol type: " + alphabet.getSymbolType(input));
                }
            }
        }
    }
}
