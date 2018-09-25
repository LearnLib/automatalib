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
package net.automatalib.util.tries;

import java.util.List;
import java.util.NoSuchElementException;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * A node in a {@link SuffixTrie}.
 *
 * @param <I>
 *         symbol class.
 *
 * @author Malte Isberner
 */
public class SuffixTrieNode<I> extends Word<I> {

    private final I symbol;
    private final SuffixTrieNode<I> parent;

    /**
     * Root constructor.
     */
    public SuffixTrieNode() {
        this.symbol = null;
        this.parent = null;
    }

    public SuffixTrieNode(I symbol, SuffixTrieNode<I> parent) {
        this.symbol = symbol;
        this.parent = parent;
    }

    public static <I> Word<I> toExplicitWord(SuffixTrieNode<I> node) {
        WordBuilder<I> wb = new WordBuilder<>(node.depth());
        appendSuffix(node, wb);
        return wb.toWord();
    }

    // TODO: replace by getter/attribute?
    public int depth() {
        return depth(this);
    }

    public static <I> int depth(SuffixTrieNode<I> node) {
        int d = 0;
        SuffixTrieNode<I> iter = node;

        while (iter.parent != null) {
            d++;
            iter = iter.parent;
        }
        return d;
    }

    public static <I> void appendSuffix(SuffixTrieNode<I> node, List<? super I> symList) {
        SuffixTrieNode<I> iter = node;

        while (iter.parent != null) {
            symList.add(iter.symbol);
            iter = iter.parent;
        }
    }

    public void appendSuffix(List<? super I> symList) {
        appendSuffix(this, symList);
    }

    public I getSymbol() {
        return symbol;
    }

    @Override
    public I getSymbol(int index) {
        return getSymbol(this, index);
    }

    public static <I> I getSymbol(SuffixTrieNode<I> node, int index) {
        SuffixTrieNode<I> iter = node;
        for (int i = index; i > 0; i--) {
            iter = iter.parent;
        }
        return iter.symbol;
    }

    public SuffixTrieNode<I> getParent() {
        return parent;
    }

    public boolean isRoot() {
        return (parent == null);
    }

    public Word<I> getSuffix() {
        if (parent == null) {
            return Word.epsilon();
        }
        WordBuilder<I> wb = new WordBuilder<>(depth());
        appendSuffix(wb);
        return wb.toWord();
    }

    @Override
    public int length() {
        return depth();
    }

    @Override
    public Iterator<I> iterator() {
        return new Iterator<>(this);
    }

    /**
     * Optimized iterator for the implicit word representation.
     *
     * @param <I>
     *         symbol class
     *
     * @author Malte Isberner
     */
    private static final class Iterator<I> implements java.util.Iterator<I> {

        private SuffixTrieNode<I> current;

        Iterator(SuffixTrieNode<I> node) {
            this.current = node;
        }

        @Override
        public boolean hasNext() {
            return !current.isRoot();
        }

        @Override
        public I next() {
            if (current.isRoot()) {
                throw new NoSuchElementException();
            }
            I sym = current.symbol;
            current = current.parent;
            return sym;
        }
    }
}
