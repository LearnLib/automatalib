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
package net.automatalib.serialization.fsm.parser;

import java.io.IOException;

import net.automatalib.automata.base.compact.AbstractCompactDeterministic;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public abstract class AbstractFSM2AutParserTest {

    protected abstract AbstractCompactDeterministic<Character, ?, ?, ?> getAut() throws IOException, FSMParseException;

    /**
     * Asserts that no NullPointerException is thrown by implementations of
     * {@link AbstractCompactDeterministic#getTransition(int, Object)}.
     * These implementations can be constructed by the parsers in this package.
     * The NullPointerException should not be thrown when getTransitions() is called with a symbol that is not in
     * {@link AbstractCompactDeterministic#getInputAlphabet()}.
     *
     * @throws IOException
     * @throws FSMParseException
     */
    @Test
    public void testGetTransition() throws IOException, FSMParseException {
        final Character aCharNotInAlphabet = '[';
        final AbstractCompactDeterministic<Character, ?, ?, ?> aut = getAut();
        Assert.assertFalse(aut.getInputAlphabet().containsSymbol(aCharNotInAlphabet));

        Assert.assertNull(aut.getTransition(aut.getInitialState(), aCharNotInAlphabet));
    }
}
