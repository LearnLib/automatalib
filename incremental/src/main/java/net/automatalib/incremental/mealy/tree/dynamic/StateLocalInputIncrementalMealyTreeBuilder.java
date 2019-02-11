/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.incremental.mealy.tree.dynamic;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.mealy.tree.Edge;
import net.automatalib.words.Word;

/**
 * A variation of the {@link DynamicIncrementalMealyTreeBuilder} that intelligently handles the information provided by
 * the {@link OutputAndLocalInputs} output class.
 * <p>
 * Words that would traverse paths whose input symbols are not part of the {@link OutputAndLocalInputs#getLocalInputs()}
 * set are answered with {@link OutputAndLocalInputs#undefined()}, even though the tree holds no information about them
 * (and it doesn't need to, given the contract of {@link OutputAndLocalInputs}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Maren Geske
 * @author frohme
 */
public class StateLocalInputIncrementalMealyTreeBuilder<I, O>
        extends DynamicIncrementalMealyTreeBuilder<I, OutputAndLocalInputs<I, O>> {

    public StateLocalInputIncrementalMealyTreeBuilder(Collection<I> initialAvailableInputs) {
        super(constructRoot(initialAvailableInputs));
    }

    private static <I, O> Node<I, OutputAndLocalInputs<I, O>> constructRoot(Collection<I> initialAvailableInputs) {
        final Node<I, OutputAndLocalInputs<I, O>> root = new Node<>(initialAvailableInputs.size());

        for (I i : initialAvailableInputs) {
            root.setEdge(i, null);
        }

        return root;
    }

    @Override
    public boolean lookup(Word<? extends I> word, List<? super OutputAndLocalInputs<I, O>> output) {

        Node<I, OutputAndLocalInputs<I, O>> curr = root;
        final Iterator<? extends I> it = word.iterator();

        while (it.hasNext()) {
            final I sym = it.next();

            if (!curr.getOutEdges().containsKey(sym)) {
                output.add(OutputAndLocalInputs.undefined());

                while (it.hasNext()) {
                    output.add(OutputAndLocalInputs.undefined());
                    it.next();
                }
                return true;
            }

            final Edge<Node<I, OutputAndLocalInputs<I, O>>, OutputAndLocalInputs<I, O>> edge = getEdge(curr, sym);

            if (edge == null) {
                return false;
            }

            output.add(edge.getOutput());
            curr = edge.getTarget();
        }

        return true;
    }

    @Override
    public void insert(Word<? extends I> input, Word<? extends OutputAndLocalInputs<I, O>> outputWord)
            throws ConflictException {

        Node<I, OutputAndLocalInputs<I, O>> curr = root;
        final Iterator<? extends OutputAndLocalInputs<I, O>> outputIt = outputWord.iterator();

        for (I sym : input) {

            // if we see output that is undefined, we can skip the remaining insertion (lookup will return #undefined)
            if (!curr.getOutEdges().containsKey(sym)) {
                return;
            }

            final OutputAndLocalInputs<I, O> out = outputIt.next();
            final Edge<Node<I, OutputAndLocalInputs<I, O>>, OutputAndLocalInputs<I, O>> edge = getEdge(curr, sym);

            if (edge == null) {
                curr = insertNode(curr, sym, out);
            } else {
                if (!Objects.equal(out, edge.getOutput())) {
                    throw new ConflictException();
                }
                curr = edge.getTarget();
            }
        }
    }

    @Override
    protected Node<I, OutputAndLocalInputs<I, O>> insertNode(Node<I, OutputAndLocalInputs<I, O>> parent,
                                                             I symbol,
                                                             OutputAndLocalInputs<I, O> output) {

        if (!parent.getOutEdges().containsKey(symbol)) {
            throw new ConflictException();
        }

        final Set<I> localInputs = output.getLocalInputs();
        final Node<I, OutputAndLocalInputs<I, O>> succ = new Node<>(localInputs.size());

        for (I i : localInputs) {
            succ.setEdge(i, null);
        }

        final Edge<Node<I, OutputAndLocalInputs<I, O>>, OutputAndLocalInputs<I, O>> edge = new Edge<>(output, succ);
        parent.setEdge(symbol, edge);
        return succ;
    }
}
