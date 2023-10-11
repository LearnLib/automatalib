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
package net.automatalib.word.procedural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.word.Word;
import net.automatalib.word.vpa.AbstractVPAlphabetTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractProceduralInputAlphabetTest<M extends ProceduralInputAlphabet<Character>>
        extends AbstractVPAlphabetTest<Character, M> {

    private final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
    private final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'T');
    private final char returnSymbol = 'R';

    protected abstract M getAlphabet(Alphabet<Character> internalAlphabet,
                                     Alphabet<Character> callAlphabet,
                                     char returnSymbol);

    @Override
    protected M getAlphabet() {
        return getAlphabet(internalAlphabet, callAlphabet, returnSymbol);
    }

    @Override
    protected List<Character> getCallSymbols() {
        return new ArrayList<>(callAlphabet);
    }

    @Override
    protected List<Character> getInternalSymbols() {
        return new ArrayList<>(internalAlphabet);
    }

    @Override
    protected List<Character> getReturnSymbols() {
        return Collections.singletonList(returnSymbol);
    }

    @Override
    protected List<Character> getNonAlphabetSymbols() {
        return new ArrayList<>(Alphabets.characters('x', 'z'));
    }

    @Test
    public void testFindCallIndex() {
        final M alphabet = getAlphabet();
        final Word<Character> word = Word.fromCharSequence("SaSTcRRaR");
        //                                                  012345678

        Assert.assertEquals(alphabet.findCallIndex(word, -1), -1);
        Assert.assertEquals(alphabet.findCallIndex(word, 0), -1); //S
        Assert.assertEquals(alphabet.findCallIndex(word, 1), 0); //a
        Assert.assertEquals(alphabet.findCallIndex(word, 2), 0); //S
        Assert.assertEquals(alphabet.findCallIndex(word, 3), 2); //T
        Assert.assertEquals(alphabet.findCallIndex(word, 4), 3); //c
        Assert.assertEquals(alphabet.findCallIndex(word, 5), 3); //R
        Assert.assertEquals(alphabet.findCallIndex(word, 6), 2); //R
        Assert.assertEquals(alphabet.findCallIndex(word, 7), 0); //a
        Assert.assertEquals(alphabet.findCallIndex(word, 8), 0); //R
        Assert.assertEquals(alphabet.findCallIndex(word, 9), -1);
        Assert.assertEquals(alphabet.findCallIndex(word, 10), -1);
    }

    @Test
    public void testFindReturnIndex() {
        final M alphabet = getAlphabet();
        final Word<Character> word = Word.fromCharSequence("SaSTcRRaR");
        //                                                  012345678

        Assert.assertEquals(alphabet.findReturnIndex(word, -1), -1);
        Assert.assertEquals(alphabet.findReturnIndex(word, 0), -1); //S
        Assert.assertEquals(alphabet.findReturnIndex(word, 1), 8); //a
        Assert.assertEquals(alphabet.findReturnIndex(word, 2), 8); //S
        Assert.assertEquals(alphabet.findReturnIndex(word, 3), 6); //T
        Assert.assertEquals(alphabet.findReturnIndex(word, 4), 5); //c
        Assert.assertEquals(alphabet.findReturnIndex(word, 5), 5); //R
        Assert.assertEquals(alphabet.findReturnIndex(word, 6), 6); //R
        Assert.assertEquals(alphabet.findReturnIndex(word, 7), 8); //a
        Assert.assertEquals(alphabet.findReturnIndex(word, 8), 8); //R
        Assert.assertEquals(alphabet.findReturnIndex(word, 9), -1);
        Assert.assertEquals(alphabet.findReturnIndex(word, 10), -1);
    }

    @Test
    public void testExpand() {
        final M alphabet = getAlphabet();
        final Mapping<Character, Word<Character>> ts =
                ImmutableMap.of('S', Word.fromLetter('x'), 'T', Word.fromLetter('y'))::get;

        Assert.assertEquals(alphabet.expand(Word.epsilon(), ts), Word.epsilon());
        Assert.assertEquals(alphabet.expand(Word.fromCharSequence("aSa"), ts), Word.fromCharSequence("aSxRa"));
        Assert.assertEquals(alphabet.expand(Word.fromCharSequence("bTb"), ts), Word.fromCharSequence("bTyRb"));
        Assert.assertEquals(alphabet.expand(Word.fromCharSequence("aSbTbSc"), ts),
                            Word.fromCharSequence("aSxRbTyRbSxRc"));
    }

    @Test
    public void testProjectWellMatched() {
        final M alphabet = getAlphabet();
        final Word<Character> word = Word.fromCharSequence("SaSTcRRaR");
        final Word<Character> outs = Word.fromCharSequence("012345678");

        final Pair<Word<Character>, Word<Character>> r0 = Pair.of(Word.fromLetter('S'), Word.fromLetter('0'));
        final Pair<Word<Character>, Word<Character>> r1 = Pair.of(Word.fromString("aSaR"), Word.fromString("1278"));
        final Pair<Word<Character>, Word<Character>> r2 = Pair.of(Word.fromString("SaR"), Word.fromString("278"));
        final Pair<Word<Character>, Word<Character>> r3 = Pair.of(Word.fromString("TRaR"), Word.fromString("3678"));
        final Pair<Word<Character>, Word<Character>> r4 = Pair.of(Word.fromString("cRRaR"), Word.fromString("45678"));
        final Pair<Word<Character>, Word<Character>> r5 = Pair.of(Word.fromString("RRaR"), Word.fromString("5678"));
        final Pair<Word<Character>, Word<Character>> r6 = Pair.of(Word.fromString("RaR"), Word.fromString("678"));
        final Pair<Word<Character>, Word<Character>> r7 = Pair.of(Word.fromString("aR"), Word.fromString("78"));
        final Pair<Word<Character>, Word<Character>> r8 = Pair.of(Word.fromString("R"), Word.fromString("8"));
        final Pair<Word<Character>, Word<Character>> r9 = Pair.of(Word.epsilon(), Word.epsilon());

        Assert.assertEquals(alphabet.project(word, -1), r0.getFirst());
        Assert.assertEquals(alphabet.project(word, 0), r0.getFirst()); //S
        Assert.assertEquals(alphabet.project(word, 1), r1.getFirst()); //a
        Assert.assertEquals(alphabet.project(word, 2), r2.getFirst()); //S
        Assert.assertEquals(alphabet.project(word, 3), r3.getFirst()); //T
        Assert.assertEquals(alphabet.project(word, 4), r4.getFirst()); //c
        Assert.assertEquals(alphabet.project(word, 5), r5.getFirst()); //R
        Assert.assertEquals(alphabet.project(word, 6), r6.getFirst()); //R
        Assert.assertEquals(alphabet.project(word, 7), r7.getFirst()); //a
        Assert.assertEquals(alphabet.project(word, 8), r8.getFirst()); //R
        Assert.assertEquals(alphabet.project(word, 9), r9.getFirst());

        Assert.assertEquals(alphabet.project(word, outs, -1), r0);
        Assert.assertEquals(alphabet.project(word, outs, 0), r0); //S
        Assert.assertEquals(alphabet.project(word, outs, 1), r1); //a
        Assert.assertEquals(alphabet.project(word, outs, 2), r2); //S
        Assert.assertEquals(alphabet.project(word, outs, 3), r3); //T
        Assert.assertEquals(alphabet.project(word, outs, 4), r4); //c
        Assert.assertEquals(alphabet.project(word, outs, 5), r5); //R
        Assert.assertEquals(alphabet.project(word, outs, 6), r6); //R
        Assert.assertEquals(alphabet.project(word, outs, 7), r7); //a
        Assert.assertEquals(alphabet.project(word, outs, 8), r8); //R
        Assert.assertEquals(alphabet.project(word, outs, 9), r9);
    }

    @Test
    public void testProjectReturnMatched() {
        final M alphabet = getAlphabet();
        final Word<Character> word = Word.fromCharSequence("SaSTcRRa");
        final Word<Character> outs = Word.fromCharSequence("01234567");

        final Pair<Word<Character>, Word<Character>> r0 = Pair.of(Word.fromString("SaSa"), Word.fromString("0127"));
        final Pair<Word<Character>, Word<Character>> r1 = Pair.of(Word.fromString("aSa"), Word.fromString("127"));
        final Pair<Word<Character>, Word<Character>> r2 = Pair.of(Word.fromString("Sa"), Word.fromString("27"));
        final Pair<Word<Character>, Word<Character>> r3 = Pair.of(Word.fromString("TRa"), Word.fromString("367"));
        final Pair<Word<Character>, Word<Character>> r4 = Pair.of(Word.fromString("cRRa"), Word.fromString("4567"));
        final Pair<Word<Character>, Word<Character>> r5 = Pair.of(Word.fromString("RRa"), Word.fromString("567"));
        final Pair<Word<Character>, Word<Character>> r6 = Pair.of(Word.fromString("Ra"), Word.fromString("67"));
        final Pair<Word<Character>, Word<Character>> r7 = Pair.of(Word.fromString("a"), Word.fromString("7"));
        final Pair<Word<Character>, Word<Character>> r8 = Pair.of(Word.epsilon(), Word.epsilon());

        Assert.assertEquals(alphabet.project(word, -1), r0.getFirst());
        Assert.assertEquals(alphabet.project(word, 0), r0.getFirst()); //S
        Assert.assertEquals(alphabet.project(word, 1), r1.getFirst()); //a
        Assert.assertEquals(alphabet.project(word, 2), r2.getFirst()); //S
        Assert.assertEquals(alphabet.project(word, 3), r3.getFirst()); //T
        Assert.assertEquals(alphabet.project(word, 4), r4.getFirst()); //c
        Assert.assertEquals(alphabet.project(word, 5), r5.getFirst()); //R
        Assert.assertEquals(alphabet.project(word, 6), r6.getFirst()); //R
        Assert.assertEquals(alphabet.project(word, 7), r7.getFirst()); //a
        Assert.assertEquals(alphabet.project(word, 8), r8.getFirst());

        Assert.assertEquals(alphabet.project(word, outs, -1), r0);
        Assert.assertEquals(alphabet.project(word, outs, 0), r0); //S
        Assert.assertEquals(alphabet.project(word, outs, 1), r1); //a
        Assert.assertEquals(alphabet.project(word, outs, 2), r2); //S
        Assert.assertEquals(alphabet.project(word, outs, 3), r3); //T
        Assert.assertEquals(alphabet.project(word, outs, 4), r4); //c
        Assert.assertEquals(alphabet.project(word, outs, 5), r5); //R
        Assert.assertEquals(alphabet.project(word, outs, 6), r6); //R
        Assert.assertEquals(alphabet.project(word, outs, 7), r7); //a
        Assert.assertEquals(alphabet.project(word, outs, 8), r8);
    }
}
