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

import net.automatalib.words.Alphabet;

public class SharedSuffixTrie<I> extends SuffixTrie<I> {

    private final Alphabet<I> alphabet;

    public SharedSuffixTrie(Alphabet<I> alphabet) {
        super(new SharedSuffixTrieNode<>());
        this.alphabet = alphabet;
    }

    public SharedSuffixTrie(Alphabet<I> alphabet, boolean graphRepresentable) {
        super(graphRepresentable, new SharedSuffixTrieNode<>());
        this.alphabet = alphabet;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SuffixTrieNode<I> add(I symbol, SuffixTrieNode<I> parent) {
        if (!(parent instanceof SharedSuffixTrieNode)) {
            throw new IllegalArgumentException("Invalid suffix trie node");
        }

        int symbolIdx = alphabet.getSymbolIndex(symbol);
        SharedSuffixTrieNode<I> sparent = (SharedSuffixTrieNode<I>) parent;

        SharedSuffixTrieNode<I>[] children = sparent.children;
        if (children == null) {
            sparent.children = new SharedSuffixTrieNode[alphabet.size()];
            children = sparent.children;
        } else if (children[symbolIdx] != null) {
            return children[symbolIdx];
        }
        SharedSuffixTrieNode<I> child = new SharedSuffixTrieNode<>(symbol, sparent);
        children[symbolIdx] = child;
        return child;
    }

}
