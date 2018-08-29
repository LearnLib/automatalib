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
package net.automatalib.modelchecking.lasso;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public class DFALassoTest extends AbstractLassoTest<DFALassoImpl<String>> {

    @Override
    protected DFALassoImpl<String> getLasso(Word<String> prefix, Word<String> loop, int unfoldTimes) {
        return new DFALassoImpl<>(new DFAMock(prefix, loop), getAlphabet(), unfoldTimes);
    }

    @Test
    public void testGetOutput() {
        final DFALassoImpl<String> lasso = getLasso(Word.epsilon(), Word.fromSymbols("a"), 1);
        Assert.assertTrue(lasso.getOutput());
    }

    private class DFAMock implements DFA<Integer, String> {

        private Word<String> prefix;
        private Word<String> word;

        DFAMock(Word<String> prefix, Word<String> loop) {
            this.prefix = prefix;
            word = prefix.concat(loop);
        }

        @Nonnull
        @Override
        public Collection<Integer> getStates() {
            return CollectionsUtil.intRange(0, word.length());
        }

        @Nullable
        @Override
        public Integer getTransition(Integer state, @Nullable String input) {
            final Integer result;

            if (word.getSymbol(state).equals(input)) {
                if (state < word.length() - 1) {
                    result = state + 1;
                } else {
                    result = prefix.length();
                }
            } else {
                result = null;
            }

            return result;
        }

        @Override
        public boolean isAccepting(Integer state) {
            // dfa is prefix-closed; always return true
            return true;
        }

        @Nullable
        @Override
        public Integer getInitialState() {
            return 0;
        }
    }
}
