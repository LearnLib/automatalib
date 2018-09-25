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
package net.automatalib.words.impl;

import java.util.NoSuchElementException;
import java.util.Objects;

import net.automatalib.words.Word;

public class ExtensionWord<I> extends Word<I> {

    private final Word<I> word;
    private final I letter;

    public ExtensionWord(Word<I> word, I letter) {
        this.word = word;
        this.letter = letter;
    }

    @Override
    public int length() {
        return word.length() + 1;
    }

    @Override
    public java.util.Iterator<I> iterator() {
        return new Iterator<>(word.iterator(), letter);
    }

    @Override
    protected Word<I> subWordInternal(int fromIndex, int toIndex) {
        int wLen = word.length();
        if (fromIndex < wLen) {
            if (toIndex <= wLen) {
                return word.subWord(fromIndex, toIndex);
            }
            return new ExtensionWord<>(word.subWord(fromIndex, wLen), letter);
        } else if (fromIndex == wLen) {
            return Word.fromLetter(letter);
        }
        return Word.epsilon();
    }

    @Override
    public void writeToArray(int offset, Object[] array, int tgtOffset, int length) {
        int wordLen = word.length();
        boolean writeLetter = (offset + length > wordLen);
        int letterDependentLength = length;
        if (offset < wordLen) {
            if (writeLetter) {
                letterDependentLength--;
            }
            word.writeToArray(offset, array, tgtOffset, letterDependentLength);
        }
        if (writeLetter) {
            array[tgtOffset + letterDependentLength] = letter;
        }
    }

    @Override
    public I getSymbol(int index) {
        if (index == word.length()) {
            return letter;
        }
        return word.getSymbol(index);
    }

    @Override
    public Word<I> prepend(I symbol) {
        return new ExtensionWord<>(word.prepend(symbol), letter);
    }

    @Override
    public boolean isPrefixOf(Word<?> other) {
        int wordLen = word.length();
        if (wordLen >= other.length()) {
            return false;
        }

        if (!word.isPrefixOf(other)) {
            return false;
        }
        return Objects.equals(other.getSymbol(wordLen), letter);
    }

    private static final class Iterator<I> implements java.util.Iterator<I> {

        private final java.util.Iterator<I> wordIt;
        private final I letter;
        private boolean next = true;

        Iterator(java.util.Iterator<I> wordIt, I letter) {
            this.wordIt = wordIt;
            this.letter = letter;
        }

        @Override
        public boolean hasNext() {
            return next;
        }

        @Override
        public I next() {
            if (wordIt.hasNext()) {
                return wordIt.next();
            }
            if (!next) {
                throw new NoSuchElementException();
            }
            next = false;
            return letter;
        }

    }

}
