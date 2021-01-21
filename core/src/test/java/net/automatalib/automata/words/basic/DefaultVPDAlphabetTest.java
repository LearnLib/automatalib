/* Copyright (C) 2013-2021 TU Dortmund
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

import net.automatalib.automata.words.util.DefaultVPDAlphabetTestUtil;
import net.automatalib.words.impl.DefaultVPDAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class DefaultVPDAlphabetTest extends AbstractAlphabetTest<Character, DefaultVPDAlphabet<Character>> {

    @Override
    protected List<Character> getAlphabetSymbols() {
        return DefaultVPDAlphabetTestUtil.JOINED_SYMBOLS;
    }

    @Override
    protected List<Character> getNonAlphabetSymbols() {
        return DefaultVPDAlphabetTestUtil.NON_CONTAINED_SYMBOLS;
    }

    @Override
    protected DefaultVPDAlphabet<Character> getAlphabet() {
        return new DefaultVPDAlphabet<>(DefaultVPDAlphabetTestUtil.INTERNAL_SYMBOLS,
                                        DefaultVPDAlphabetTestUtil.CALL_SYMBOLS,
                                        DefaultVPDAlphabetTestUtil.RETURN_SYMBOLS);
    }

    @Test
    public void testNonDisjointSubAlphabets() {
        Assert.assertThrows(() -> new DefaultVPDAlphabet<>(DefaultVPDAlphabetTestUtil.INTERNAL_SYMBOLS,
                                                           DefaultVPDAlphabetTestUtil.INTERNAL_SYMBOLS,
                                                           DefaultVPDAlphabetTestUtil.CALL_SYMBOLS));
        Assert.assertThrows(() -> new DefaultVPDAlphabet<>(DefaultVPDAlphabetTestUtil.INTERNAL_SYMBOLS,
                                                           DefaultVPDAlphabetTestUtil.CALL_SYMBOLS,
                                                           DefaultVPDAlphabetTestUtil.CALL_SYMBOLS));
        Assert.assertThrows(() -> new DefaultVPDAlphabet<>(DefaultVPDAlphabetTestUtil.INTERNAL_SYMBOLS,
                                                           DefaultVPDAlphabetTestUtil.CALL_SYMBOLS,
                                                           DefaultVPDAlphabetTestUtil.INTERNAL_SYMBOLS));
    }
}