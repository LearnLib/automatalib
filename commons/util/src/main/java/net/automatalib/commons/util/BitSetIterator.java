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
package net.automatalib.commons.util;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/**
 * Iterator for iterating over a BitSet like over a normal collection. The type returned by next() is {@link Integer}.
 *
 * @author Malte Isberner
 */
public class BitSetIterator implements Iterator<Integer>, PrimitiveIterator.OfInt {

    private final BitSet bitSet;
    private int currBitIdx;
    private int lastBitIdx;

    /**
     * Constructor.
     *
     * @param bitSet
     *         the bitset over which to iterate.
     */
    public BitSetIterator(BitSet bitSet) {
        this.bitSet = bitSet;
        this.currBitIdx = bitSet.nextSetBit(0);
        this.lastBitIdx = -1;
    }

    @Override
    public boolean hasNext() {
        return (currBitIdx != -1);
    }

    @Override
    public void remove() {
        if (lastBitIdx == -1) {
            throw new NoSuchElementException();
        }
        bitSet.clear(lastBitIdx);
    }

    @Override
    public int nextInt() {
        if (currBitIdx == -1) {
            throw new NoSuchElementException();
        }
        lastBitIdx = currBitIdx;
        currBitIdx = bitSet.nextSetBit(currBitIdx + 1);

        return lastBitIdx;
    }
}
