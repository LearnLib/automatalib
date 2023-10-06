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
package net.automatalib.automata.words.basic;

import java.util.List;

import net.automatalib.automata.words.util.DefaultVPAlphabetTestUtil;
import net.automatalib.words.impl.DefaultVPAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DefaultVPAlphabetTest extends AbstractAlphabetTest<Character, DefaultVPAlphabet<Character>> {

    @Override
    protected List<Character> getAlphabetSymbols() {
        return DefaultVPAlphabetTestUtil.JOINED_SYMBOLS;
    }

    @Override
    protected List<Character> getNonAlphabetSymbols() {
        return DefaultVPAlphabetTestUtil.NON_CONTAINED_SYMBOLS;
    }

    @Override
    protected DefaultVPAlphabet<Character> getAlphabet() {
        return new DefaultVPAlphabet<>(DefaultVPAlphabetTestUtil.INTERNAL_SYMBOLS,
                                       DefaultVPAlphabetTestUtil.CALL_SYMBOLS,
                                       DefaultVPAlphabetTestUtil.RETURN_SYMBOLS);
    }

    @Test
    public void testNonDisjointSubAlphabets() {
        Assert.assertThrows(() -> new DefaultVPAlphabet<>(DefaultVPAlphabetTestUtil.INTERNAL_SYMBOLS,
                                                          DefaultVPAlphabetTestUtil.INTERNAL_SYMBOLS,
                                                          DefaultVPAlphabetTestUtil.CALL_SYMBOLS));
        Assert.assertThrows(() -> new DefaultVPAlphabet<>(DefaultVPAlphabetTestUtil.INTERNAL_SYMBOLS,
                                                          DefaultVPAlphabetTestUtil.CALL_SYMBOLS,
                                                          DefaultVPAlphabetTestUtil.CALL_SYMBOLS));
        Assert.assertThrows(() -> new DefaultVPAlphabet<>(DefaultVPAlphabetTestUtil.INTERNAL_SYMBOLS,
                                                          DefaultVPAlphabetTestUtil.CALL_SYMBOLS,
                                                          DefaultVPAlphabetTestUtil.INTERNAL_SYMBOLS));
    }
}
