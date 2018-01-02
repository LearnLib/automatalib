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

/**
 * A node in a {@link SharedSuffixTrie}. This class maintains an array containing all children, in order to avoid
 * inserting duplicates.
 *
 * @param <I>
 *         symbol class.
 *
 * @author Malte Isberner
 */
final class SharedSuffixTrieNode<I> extends SuffixTrieNode<I> {

    SharedSuffixTrieNode<I>[] children;

    /**
     * Root constructor.
     */
    SharedSuffixTrieNode() {
    }

    /**
     * Constructor.
     *
     * @param symbol
     *         the symbol to prepend.
     * @param parent
     *         the trie node representing the remaining suffix.
     */
    SharedSuffixTrieNode(I symbol, SuffixTrieNode<I> parent) {
        super(symbol, parent);
    }

}
