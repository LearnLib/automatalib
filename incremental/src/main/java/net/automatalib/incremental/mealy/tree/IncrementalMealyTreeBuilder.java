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
package net.automatalib.incremental.mealy.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.mealy.AbstractIncrementalMealyBuilder;
import net.automatalib.ts.transout.MealyTransitionSystem;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.visualization.helper.DelegateVisualizationHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class IncrementalMealyTreeBuilder<I, O> extends AbstractIncrementalMealyBuilder<I, O> {

    private final Node<I, O> root;

    public IncrementalMealyTreeBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
        this.root = new Node<>(inputAlphabet.size());
    }

    @Override
    public boolean lookup(Word<? extends I> word, List<? super O> output) {
        Node<I, O> curr = root;

        for (I sym : word) {
            int symIdx = inputAlphabet.getSymbolIndex(sym);
            Edge<I, O> edge = curr.getEdge(symIdx);
            if (edge == null) {
                return false;
            }
            output.add(edge.getOutput());
            curr = edge.getTarget();
        }

        return true;
    }

    @Override
    public void insert(Word<? extends I> input, Word<? extends O> outputWord) throws ConflictException {
        Node<I, O> curr = root;

        Iterator<? extends O> outputIt = outputWord.iterator();
        for (I sym : input) {
            int symIdx = inputAlphabet.getSymbolIndex(sym);
            O out = outputIt.next();
            Edge<I, O> edge = curr.getEdge(symIdx);
            if (edge == null) {
                curr = insertNode(curr, symIdx, out);
            } else {
                if (!Objects.equal(out, edge.getOutput())) {
                    throw new ConflictException();
                }
                curr = curr.getSuccessor(symIdx);
            }
        }
    }

    private Node<I, O> insertNode(Node<I, O> parent, int symIdx, O output) {
        Node<I, O> succ = new Node<>(inputAlphabet.size());
        Edge<I, O> edge = new Edge<>(output, succ);
        parent.setEdge(symIdx, edge);
        return succ;
    }

    @Override
    public GraphView asGraph() {
        return new GraphView();
    }

    @Override
    public TransitionSystemView asTransitionSystem() {
        return new TransitionSystemView();
    }

    @Override
    public Word<I> findSeparatingWord(MealyMachine<?, I, ?, O> target,
                                      Collection<? extends I> inputs,
                                      boolean omitUndefined) {
        return doFindSeparatingWord(target, inputs, omitUndefined);
    }

    private <S, T> Word<I> doFindSeparatingWord(MealyMachine<S, I, T, O> target,
                                                Collection<? extends I> inputs,
                                                boolean omitUndefined) {
        Deque<Record<S, I, O>> dfsStack = new ArrayDeque<>();

        dfsStack.push(new Record<>(target.getInitialState(), root, null, inputs.iterator()));

        while (!dfsStack.isEmpty()) {
            Record<S, I, O> rec = dfsStack.peek();
            if (!rec.inputIt.hasNext()) {
                dfsStack.pop();
                continue;
            }
            I input = rec.inputIt.next();
            int inputIdx = inputAlphabet.getSymbolIndex(input);

            Edge<I, O> edge = rec.treeNode.getEdge(inputIdx);
            if (edge == null) {
                continue;
            }

            T trans = target.getTransition(rec.automatonState, input);
            if (omitUndefined && trans == null) {
                continue;
            }
            if (trans == null || !Objects.equal(target.getTransitionOutput(trans), edge.getOutput())) {

                WordBuilder<I> wb = new WordBuilder<>(dfsStack.size());
                wb.append(input);
                dfsStack.pop();

                while (!dfsStack.isEmpty()) {
                    wb.append(rec.incomingInput);
                    rec = dfsStack.pop();
                }
                return wb.reverse().toWord();
            }

            dfsStack.push(new Record<>(target.getSuccessor(trans), edge.getTarget(), input, inputs.iterator()));
        }

        return null;
    }

    @Override
    public boolean hasDefinitiveInformation(Word<? extends I> word) {
        Node<I, O> curr = root;

        Iterator<? extends I> symIt = word.iterator();
        while (symIt.hasNext() && curr != null) {
            int symIdx = inputAlphabet.getSymbolIndex(symIt.next());
            curr = curr.getSuccessor(symIdx);
        }
        return (curr != null);
    }

    private static final class Record<S, I, O> {

        private final S automatonState;
        private final Node<I, O> treeNode;
        private final I incomingInput;
        private final Iterator<? extends I> inputIt;

        Record(S automatonState, Node<I, O> treeNode, I incomingInput, Iterator<? extends I> inputIt) {
            this.automatonState = automatonState;
            this.treeNode = treeNode;
            this.inputIt = inputIt;
            this.incomingInput = incomingInput;
        }
    }

    public class GraphView extends AbstractGraphView<I, O, Node<I, O>, AnnotatedEdge<I, O>> {

        @Override
        public Collection<Node<I, O>> getNodes() {
            List<Node<I, O>> result = new ArrayList<>();
            Iterators.addAll(result, GraphTraversal.dfIterator(this, Collections.singleton(root)));
            return result;
        }

        @Override
        public Collection<AnnotatedEdge<I, O>> getOutgoingEdges(Node<I, O> node) {
            List<AnnotatedEdge<I, O>> result = new ArrayList<>();
            for (int i = 0; i < inputAlphabet.size(); i++) {
                Edge<I, O> edge = node.getEdge(i);
                if (edge != null) {
                    result.add(new AnnotatedEdge<>(edge, inputAlphabet.getSymbol(i)));
                }
            }
            return result;
        }

        @Override
        public Node<I, O> getTarget(AnnotatedEdge<I, O> edge) {
            return edge.getTarget();
        }

        @Override
        @Nullable
        public I getInputSymbol(AnnotatedEdge<I, O> edge) {
            return edge.getInput();
        }

        @Override
        @Nullable
        public O getOutputSymbol(AnnotatedEdge<I, O> edge) {
            return edge.getOutput();
        }

        @Override
        @Nonnull
        public Node<I, O> getInitialNode() {
            return root;
        }

        @Override
        public VisualizationHelper<Node<I, O>, AnnotatedEdge<I, O>> getVisualizationHelper() {
            return new DelegateVisualizationHelper<Node<I, O>, AnnotatedEdge<I, O>>(super.getVisualizationHelper()) {

                private int id;

                @Override
                public boolean getNodeProperties(Node<I, O> node, Map<String, String> properties) {
                    if (!super.getNodeProperties(node, properties)) {
                        return false;
                    }
                    properties.put(NodeAttrs.LABEL, "n" + (id++));
                    return true;
                }
            };
        }

    }

    public class TransitionSystemView implements MealyTransitionSystem<Node<I, O>, I, Edge<I, O>, O> {

        @Override
        public Edge<I, O> getTransition(Node<I, O> state, I input) {
            int inputIdx = inputAlphabet.getSymbolIndex(input);
            return state.getEdge(inputIdx);
        }

        @Override
        public Node<I, O> getSuccessor(Edge<I, O> transition) {
            return transition.getTarget();
        }

        @Override
        public Node<I, O> getInitialState() {
            return root;
        }

        @Override
        public O getTransitionOutput(Edge<I, O> transition) {
            return transition.getOutput();
        }
    }
}
