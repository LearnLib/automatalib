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
package net.automatalib.word;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class AbstractNonemptyWordTest extends AbstractWordTest {

    protected List<Word<?>> realPrefixes;
    protected List<Word<?>> realSuffixes;

    @BeforeClass
    @Override
    public void setup() {
        super.setup();
        this.realPrefixes = realPrefixes();
        this.realSuffixes = realSuffixes();
    }

    protected abstract List<Word<?>> realPrefixes();

    protected abstract List<Word<?>> realSuffixes();

    @Override
    @Test
    public void testIsEmpty() {
        Assert.assertFalse(testWord.isEmpty());
    }

    @Override
    @Test
    public void testLongestCommonPrefix() {
        super.testLongestCommonPrefix();

        for (Word<?> rp : realPrefixes) {
            Word<?> lcp = testWord.longestCommonPrefix(rp);
            Assert.assertEquals(rp, lcp);
        }
    }

    @Override
    @Test
    public void testLongestCommonSuffix() {
        super.testLongestCommonSuffix();

        for (Word<?> rs : realSuffixes) {
            Word<?> lcs = testWord.longestCommonSuffix(rs);
            Assert.assertEquals(rs, lcs);
        }
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testSubword3() {
        testWord.subWord(testWord.length(), 0);
    }

}
