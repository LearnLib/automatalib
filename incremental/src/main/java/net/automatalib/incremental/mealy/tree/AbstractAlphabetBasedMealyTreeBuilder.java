/* Copyright (C) 2013-2023 TU Dortmund
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Iterators;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MealyMachine.MealyGraphView;
import net.automatalib.common.util.mapping.MapMapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.graph.Graph;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.visualization.VisualizationHelper;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractAlphabetBasedMealyTreeBuilder<I, O> extends AbstractMealyTreeBuilder<Node<O>, I, O>
        implements InputAlphabetHolder<I> {

    private final Alphabet<I> inputAlphabet;
    private int alphabetSize;

    AbstractAlphabetBasedMealyTreeBuilder(Alphabet<I> inputAlphabet) {
        super(new Node<>(inputAlphabet.size()));
        this.inputAlphabet = inputAlphabet;
        this.alphabetSize = inputAlphabet.size();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!inputAlphabet.containsSymbol(symbol)) {
            Alphabets.toGrowingAlphabetOrThrowException(inputAlphabet).addSymbol(symbol);
        }

        final int newAlphabetSize = inputAlphabet.size();
        // even if the symbol was already in the alphabet, we need to make sure to be able to store the new symbol
        if (alphabetSize < newAlphabetSize) {
            ensureInputCapacity(root, alphabetSize, newAlphabetSize);
            alphabetSize = newAlphabetSize;
        }
    }

    private void ensureInputCapacity(Node<O> node, int oldAlphabetSize, int newAlphabetSize) {
        node.ensureInputCapacity(newAlphabetSize);
        for (int i = 0; i < oldAlphabetSize; i++) {
            final Node<O> child = node.getSuccessor(i);
            if (child != null) {
                ensureInputCapacity(child, oldAlphabetSize, newAlphabetSize);
            }
        }
    }

    @Override
    @Nullable Edge<Node<O>, O> getEdge(Node<O> node, I symbol) {
        return node.getEdge(inputAlphabet.getSymbolIndex(symbol));
    }

    @Override
    Node<O> createNode() {
        return new Node<>(alphabetSize);
    }

    @Override
    Node<O> insertNode(Node<O> parent, I symIdx, O output) {
        Node<O> succ = createNode();
        Edge<Node<O>, O> edge = new Edge<>(output, succ);
        parent.setEdge(inputAlphabet.getSymbolIndex(symIdx), edge);
        return succ;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return inputAlphabet;
    }

    @Override
    public Graph<Node<O>, ?> asGraph() {
        return new MealyGraphView<Node<O>, I, Edge<Node<O>, O>, O, MealyMachineView>(new MealyMachineView(),
                                                                                     inputAlphabet) {
            @Override
            public VisualizationHelper<Node<O>, TransitionEdge<I, Edge<Node<O>, O>>> getVisualizationHelper() {
                return new net.automatalib.incremental.mealy.VisualizationHelper<>(automaton);
            }
        };
    }

    private class MealyMachineView extends TransitionSystemView
            implements MealyMachine<Node<O>, I, Edge<Node<O>, O>, O> {

        @Override
        public Collection<Node<O>> getStates() {
            List<Node<O>> result = new ArrayList<>();
            Iterators.addAll(result, TSTraversal.breathFirstIterator(this, inputAlphabet));
            return result;
        }

        /*
         * We need to override the default MooreMachine mapping, because its StateIDStaticMapping class requires our
         * nodeIDs, which requires our states, which requires our nodeIDs, which requires ... infinite loop!
         */
        @Override
        public <V> MutableMapping<Node<O>, V> createStaticStateMapping() {
            return new MapMapping<>();
        }
    }
}
