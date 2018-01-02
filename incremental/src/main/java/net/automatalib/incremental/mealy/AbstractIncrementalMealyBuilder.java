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
package net.automatalib.incremental.mealy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public abstract class AbstractIncrementalMealyBuilder<I, O> implements IncrementalMealyBuilder<I, O> {

    protected final Alphabet<I> inputAlphabet;

    public AbstractIncrementalMealyBuilder(Alphabet<I> alphabet) {
        this.inputAlphabet = alphabet;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return inputAlphabet;
    }

    @Override
    public boolean hasDefinitiveInformation(Word<? extends I> word) {
        List<O> unused = new ArrayList<>(word.length());
        return lookup(word, unused);
    }

    @Override
    public Word<O> lookup(Word<? extends I> inputWord) {
        WordBuilder<O> wb = new WordBuilder<>(inputWord.size());
        lookup(inputWord, wb);
        return wb.toWord();
    }

    public abstract static class AbstractGraphView<I, O, N, E> implements GraphView<I, O, N, E> {

        @Override
        public VisualizationHelper<N, E> getVisualizationHelper() {
            return new DefaultVisualizationHelper<N, E>() {

                @Override
                public Collection<N> initialNodes() {
                    return Collections.singleton(getInitialNode());
                }

                @Override
                public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
                    if (!super.getEdgeProperties(src, edge, tgt, properties)) {
                        return false;
                    }
                    I input = getInputSymbol(edge);
                    O output = getOutputSymbol(edge);
                    properties.put(EdgeAttrs.LABEL, input + " / " + output);
                    return true;
                }

            };
        }
    }

}
