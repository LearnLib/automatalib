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
package net.automatalib.automata.words.vpda;

import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Abstract test class defining several sanity checks for visibly-pushdown alphabets.
 *
 * @param <I>
 *         input symbol type
 * @param <M>
 *         concrete alphabet type
 *
 * @author frohme
 */
public abstract class AbstractVPDAlphabetTest<I, M extends VPDAlphabet<I>> {

    private List<I> callSymbols;
    private List<I> internalSymbols;
    private List<I> returnSymbols;
    private List<I> nonAlphabetSymbols;
    private M alphabet;

    @BeforeClass
    public void setUp() {
        this.callSymbols = getCallSymbols();
        this.internalSymbols = getInternalSymbols();
        this.returnSymbols = getReturnSymbols();
        this.nonAlphabetSymbols = getNonAlphabetSymbols();
        this.alphabet = getAlphabet();
    }

    protected abstract List<I> getCallSymbols();

    protected abstract List<I> getInternalSymbols();

    protected abstract List<I> getReturnSymbols();

    protected abstract List<I> getNonAlphabetSymbols();

    protected abstract M getAlphabet();

    @Test
    public void testSize() {
        Assert.assertEquals(callSymbols.size() + internalSymbols.size() + returnSymbols.size(), alphabet.size());
    }

    @Test
    public void testIndices() {
        testIndices(callSymbols, alphabet.getCallAlphabet());
        testIndices(internalSymbols, alphabet.getInternalAlphabet());
        testIndices(returnSymbols, alphabet.getReturnAlphabet());
    }

    private void testIndices(final List<I> source, final Alphabet<I> alphabet) {
        for (int i = 0; i < source.size(); i++) {
            final I sym = source.get(i);

            Assert.assertEquals(i, alphabet.getSymbolIndex(sym));
            Assert.assertEquals(sym, alphabet.getSymbol(i));
        }
    }

    @Test
    public void testOutOfBoundsIndex() {
        testOutOfBoundsIndex(callSymbols, alphabet.getCallAlphabet());
        testOutOfBoundsIndex(internalSymbols, alphabet.getInternalAlphabet());
        testOutOfBoundsIndex(returnSymbols, alphabet.getReturnAlphabet());
    }

    private void testOutOfBoundsIndex(final List<I> source, final Alphabet<I> alphabet) {
        Assert.assertThrows(() -> alphabet.getSymbol(-1));
        Assert.assertThrows(() -> alphabet.getSymbol(source.size() + 1));
    }

    @Test
    public void testNonContainedSymbols() {
        testNonContainedSymbols(alphabet.getCallAlphabet());
        testNonContainedSymbols(alphabet.getInternalAlphabet());
        testNonContainedSymbols(alphabet.getReturnAlphabet());
    }

    private void testNonContainedSymbols(final Alphabet<I> alphabet) {
        for (final I i : this.nonAlphabetSymbols) {
            Assert.assertThrows(() -> alphabet.getSymbolIndex(i));
        }
    }
}
