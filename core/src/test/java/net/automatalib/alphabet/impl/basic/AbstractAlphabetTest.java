/* Copyright (C) 2013-2025 TU Dortmund University
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
package net.automatalib.alphabet.impl.basic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.common.util.mapping.Mapping;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Abstract test class defining several sanity checks for regular, immutable alphabets.
 *
 * @param <I>
 *         input symbol type
 * @param <M>
 *         concrete alphabet type
 */
public abstract class AbstractAlphabetTest<I, M extends Alphabet<I>> {

    private List<I> alphabetSymbols;
    private List<I> nonAlphabetSymbols;
    private M alphabet;

    @BeforeClass
    public void setUp() {
        this.alphabetSymbols = getAlphabetSymbols();
        this.nonAlphabetSymbols = getNonAlphabetSymbols();
        this.alphabet = getAlphabet();
    }

    protected abstract List<I> getAlphabetSymbols();

    protected abstract List<I> getNonAlphabetSymbols();

    protected abstract M getAlphabet();

    @Test
    public void testSize() {
        Assert.assertEquals(alphabetSymbols.size(), alphabet.size());
    }

    @Test
    public void testIndices() {
        for (int i = 0; i < alphabetSymbols.size(); i++) {
            final I sym = alphabetSymbols.get(i);

            Assert.assertEquals(i, this.alphabet.getSymbolIndex(sym));
            Assert.assertEquals(sym, this.alphabet.getSymbol(i));
        }
    }

    @Test
    public void testOutOfBoundsIndex() {
        Assert.assertThrows(() -> this.alphabet.getSymbol(-1));
        Assert.assertThrows(() -> this.alphabet.getSymbol(alphabetSymbols.size() + 1));
    }

    @Test
    public void testContains() {
        for (I sym : alphabetSymbols) {
            Assert.assertTrue(this.alphabet.containsSymbol(sym));
        }

        for (I sym : nonAlphabetSymbols) {
            Assert.assertFalse(this.alphabet.containsSymbol(sym));
        }
    }

    @Test
    public void testNonContainedSymbols() {
        for (I i : this.nonAlphabetSymbols) {
            Assert.assertThrows(() -> this.alphabet.getSymbolIndex(i));
        }
    }

    @Test
    public void testTranslate() {
        final Alphabet<Integer> source = Alphabets.integers(0, alphabetSymbols.size() - 1);
        final Mapping<Integer, I> mapping = this.alphabet.translateFrom(source);

        for (int i = 0; i < alphabetSymbols.size(); i++) {
            Assert.assertEquals(alphabet.getSymbol(i), mapping.get(i));
        }
    }

    @Test
    public void testWriteToArray() {
        final int numOfSyms = alphabetSymbols.size();
        final Object[] target = new Object[numOfSyms];
        alphabet.writeToArray(0, target, 0, numOfSyms);

        for (int i = 0; i < numOfSyms; i++) {
            Assert.assertEquals(alphabetSymbols.get(i), target[i]);
        }

        // also test offsets
        if (numOfSyms > 1) {
            final Object[] offsetTarget = new Object[numOfSyms];
            alphabet.writeToArray(1, offsetTarget, 1, numOfSyms - 1);

            Assert.assertNull(offsetTarget[0]);
            for (int i = 1; i < numOfSyms; i++) {
                Assert.assertEquals(alphabetSymbols.get(i), target[i]);
            }
        }
    }

    @Test
    public void testImmutableIterator() {
        final Iterator<I> iter = alphabet.iterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertNotNull(iter.next());
        Assert.assertThrows(UnsupportedOperationException.class, iter::remove);

        if (alphabet instanceof List) {
            @SuppressWarnings("unchecked")
            final List<I> alphabetAsList = (List<I>) alphabet;

            final ListIterator<I> listIter = alphabetAsList.listIterator();
            Assert.assertTrue(listIter.hasNext());
            I next = listIter.next();
            Assert.assertNotNull(next);
            Assert.assertThrows(UnsupportedOperationException.class, listIter::remove);
            Assert.assertThrows(UnsupportedOperationException.class, () -> listIter.set(next));
            Assert.assertThrows(UnsupportedOperationException.class, () -> listIter.add(next));
        }
    }

    @Test
    public void testReversed() {
        final Comparator<I> r = alphabet.reversed();
        final Alphabet<I> reversed;

        if (r instanceof Alphabet) {
            reversed = (Alphabet<I>) r;
        } else {
            return;
        }

        for (I i : alphabet) {
            Assert.assertTrue(reversed.containsSymbol(i));
        }

        Assert.assertEquals(reversed.size(), alphabet.size());

        for (int i = 0, j = reversed.size() - 1; i < alphabet.size() && j >= 0; i++, j--) {
            Assert.assertEquals(reversed.getSymbol(j), alphabet.getSymbol(i));
        }

        int idx = alphabet.size() - 1;
        for (I i : alphabet) {
            Assert.assertEquals(reversed.getSymbolIndex(i), idx--);
        }

        final List<I> list = new ArrayList<>(alphabet);
        list.sort(reversed);
        Assert.assertEquals(list, reversed);
    }
}
