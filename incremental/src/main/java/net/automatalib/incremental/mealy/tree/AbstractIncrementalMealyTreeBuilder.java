/* Copyright (C) 2013-2022 TU Dortmund
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
package net.automatalib.incremental.mealy.tree;

import java.util.Iterator;
import java.util.Objects;

import net.automatalib.incremental.ConflictException;
import net.automatalib.words.Word;

public abstract class AbstractIncrementalMealyTreeBuilder<N, I, O> extends AbstractMealyTreeBuilder<N, I, O> {

    public AbstractIncrementalMealyTreeBuilder(N root) {
        super(root);
    }

    public void insert(Word<? extends I> input, Word<? extends O> outputWord) {
        N curr = root;

        Iterator<? extends O> outputIt = outputWord.iterator();
        for (I sym : input) {
            O out = outputIt.next();
            Edge<N, O> edge = getEdge(curr, sym);
            if (edge == null) {
                curr = insertNode(curr, sym, out);
            } else {
                if (!Objects.equals(out, edge.getOutput())) {
                    throw new ConflictException();
                }
                curr = edge.getTarget();
            }
        }
    }
}
