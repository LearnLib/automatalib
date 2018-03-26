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
package net.automatalib.automata.words.growing;

import net.automatalib.automata.words.util.FastAlphabetTestUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.SymbolHidingAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class SymbolHidingAlphabetTest {

    @Test
    public void testImmutableAlphabet() {
        testAlphabet(Alphabets.characters('a', 'c'), 'b', true, true);
        testAlphabet(Alphabets.characters('a', 'c'), 'd', false, true);

    }

    @Test
    public void testMutableAlphabet() {
        testAlphabet(new FastAlphabet<>(FastAlphabetTestUtil.ALPHABET_SYMBOLS),
                     FastAlphabetTestUtil.ALPHABET_SYMBOLS.get(0),
                     false,
                     false);
        testAlphabet(new FastAlphabet<>(FastAlphabetTestUtil.ALPHABET_SYMBOLS),
                     FastAlphabetTestUtil.NON_ALPHABET_SYMBOLS.get(0),
                     false,
                     true);

    }

    private static <I> void testAlphabet(final Alphabet<I> alphabet,
                                         final I symbolToHide,
                                         final boolean singleContains,
                                         final boolean multiContains) {

        final Alphabet<I> wrapped = SymbolHidingAlphabet.wrapIfMutable(alphabet);

        SymbolHidingAlphabet.runWhileHiding(wrapped, symbolToHide, () -> {
            Assert.assertEquals(wrapped.containsSymbol(symbolToHide), singleContains);
            Assert.assertEquals(wrapped.contains(symbolToHide), singleContains);
            Assert.assertEquals(wrapped.containsAll(alphabet), multiContains);

            if (wrapped instanceof GrowingAlphabet) {
                final GrowingAlphabet<I> asGrowingAlphabet = (GrowingAlphabet<I>) wrapped;

                Assert.assertTrue(asGrowingAlphabet.addSymbol(symbolToHide) >= 0);

                Assert.assertEquals(asGrowingAlphabet.containsSymbol(symbolToHide), singleContains);
                Assert.assertEquals(asGrowingAlphabet.contains(symbolToHide), singleContains);
                Assert.assertEquals(asGrowingAlphabet.containsAll(alphabet), singleContains);
            }
        });
    }
}
