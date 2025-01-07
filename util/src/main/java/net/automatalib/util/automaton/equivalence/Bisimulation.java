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
package net.automatalib.util.automaton.equivalence;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.automaton.Automaton;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.util.partitionrefinement.Valmari;
import net.automatalib.util.partitionrefinement.Valmari.RefinablePartition;

public final class Bisimulation {

    private Bisimulation() {
        // prevent instantiation
    }

    public static <AS, BS, I, AT, BT> Set<Pair<AS, BS>> bisimulationEquivalenceRelation(Automaton<AS, I, AT> a,
                                                                                        Automaton<BS, I, BT> b,
                                                                                        Collection<? extends I> inputs) {

        final List<? extends I> alphabet = CollectionUtil.randomAccessList(inputs);
        final StateIDs<AS> aIDs = a.stateIDs();
        final StateIDs<BS> bIDs = b.stateIDs();

        // set up valmari data
        final int n1 = a.size();
        final int n2 = b.size();
        final int k = alphabet.size();
        int m = 0;

        for (I i : alphabet) {
            for (AS as : a) {
                m += a.getTransitions(as, i).size();
            }
            for (BS bs : b) {
                m += b.getTransitions(bs, i).size();
            }
        }

        final int[] tail = new int[m];
        final int[] label = new int[m];
        final int[] head = new int[m];

        int cnt = 0;
        for (int i = 0; i < k; i++) {
            final I sym = alphabet.get(i);
            for (int j = 0; j < n1; j++) {
                final AS as = aIDs.getState(j);
                for (AT t : a.getTransitions(as, sym)) {
                    tail[cnt] = j;
                    label[cnt] = i;
                    head[cnt] = aIDs.getStateId(a.getSuccessor(t));
                    cnt++;
                }
            }
            for (int j = 0; j < n2; j++) {
                final BS bs = bIDs.getState(j);
                for (BT t : b.getTransitions(bs, sym)) {
                    tail[cnt] = j + n1;
                    label[cnt] = i;
                    head[cnt] = bIDs.getStateId(b.getSuccessor(t)) + n1;
                    cnt++;
                }
            }
        }

        // compute bisimulation relation
        final Valmari valmari = new Valmari(new int[n1 + n2], tail, label, head);
        valmari.computeCoarsestStablePartition();
        final RefinablePartition blocks = valmari.blocks;

        // extract result
        final Set<Pair<AS, BS>> result = new HashSet<>();

        for (int block = 0; block <= blocks.sets; block++) {
            final int size = blocks.end[block] - blocks.first[block];
            final int[] elems = new int[size];
            int low = 0;
            int mid = 0;
            int high = size;
            for (int i = blocks.first[block]; i < blocks.end[block]; i++) {
                final int e = blocks.elems[i];
                if (e < n1) { // a states to the left
                    elems[low++] = e;
                    mid++;
                } else { // b states to the right
                    elems[--high] = e;
                }
            }

            // cartesian product on mid
            for (int i = 0; i < mid; i++) {
                for (int j = size - 1; j >= mid; j--) {
                    result.add(Pair.of(aIDs.getState(elems[i]), bIDs.getState(elems[j] - n1)));
                }
            }
        }

        return result;
    }
}
