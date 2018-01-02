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
package net.automatalib.automata.vpda;

import java.util.Collections;

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
}
