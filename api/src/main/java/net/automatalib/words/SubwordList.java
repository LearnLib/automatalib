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
package net.automatalib.words;

import java.util.AbstractList;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
class SubwordList<I> extends AbstractList<Word<I>> {

    private final Word<I> word;
    private final boolean reverse;
    private final boolean prefix;

    SubwordList(Word<I> word, boolean prefix, boolean reverse) {
        this.word = word;
        this.prefix = prefix;
        this.reverse = reverse;
    }

    @Override
    @Nonnull
    public Word<I> get(int index) {
        if (index < 0 || index > word.length()) {
            throw new IndexOutOfBoundsException();
        }
        if (prefix) {
            return word.prefix(index);
        }
        if (reverse) {
            return word.suffix(word.length() - index);
        }
        return word.suffix(index);
    }

    @Override
    public int size() {
        return word.length() + 1;
    }

}
