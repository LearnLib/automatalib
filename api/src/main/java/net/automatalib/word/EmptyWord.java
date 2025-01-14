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

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;

import net.automatalib.alphabet.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The empty word.
 *
 * @see Collections#emptyList()
 */
final class EmptyWord<I> extends Word<I> {

    public static final EmptyWord<?> INSTANCE = new EmptyWord<>();

    @Override
    public int length() {
        return 0;
    }

    @Override
    public Spliterator<I> spliterator() {
        return Spliterators.emptySpliterator();
    }

    @Override
    public Word<I> subWordInternal(int fromIndex, int toIndex) {
        return this;
    }

    @Override
    public void writeToArray(int offset, @Nullable Object[] array, int tgtOffset, int length) {}

    @Override
    public I getSymbol(int index) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
    }

    @Override
    public List<I> asList() {
        return Collections.emptyList();
    }

    @Override
    public Word<I> canonicalNext(Alphabet<I> sigma) {
        return new LetterWord<>(sigma.getSymbol(0));
    }

    @Override
    public I lastSymbol() {
        throw new NoSuchElementException();
    }

    @Override
    public Word<I> append(I symbol) {
        return new LetterWord<>(symbol);
    }

    @Override
    public Word<I> prepend(I symbol) {
        return append(symbol);
    }

    @Override
    public boolean isPrefixOf(Word<?> other) {
        return true;
    }

    @Override
    public Word<I> longestCommonPrefix(Word<?> other) {
        return this;
    }

    @Override
    public boolean isSuffixOf(Word<?> other) {
        return true;
    }

    @Override
    public Word<I> longestCommonSuffix(Word<?> other) {
        return this;
    }

    @Override
    public Word<I> flatten() {
        return this;
    }

    @Override
    public Word<I> trimmed() {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Word<T> transform(Function<? super I, ? extends T> transformer) {
        return (Word<T>) this;
    }
}
