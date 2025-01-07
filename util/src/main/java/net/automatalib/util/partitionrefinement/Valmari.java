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
package net.automatalib.util.partitionrefinement;

import net.automatalib.common.util.array.ArrayUtil;

/**
 * Valmari's algorithm for computing the relational coarsest partition as presented in <a
 * href="https://dx.doi.org/10.3233/FI-2010-369">Simple Bisimilarity Minimization in O(m log n) Time</a> by Antti
 * Valmari.
 * <p>
 * To ensure maximal performance, this class is designed in a very low-level fashion, exposing most of its internal
 * fields directly. For a more convenient access to its functionality, resort to the utility functions in
 * {@link ValmariInitializers} and {@link ValmariExtractors}, respectively.
 */
public class Valmari {

    public final RefinablePartition blocks;
    public final RefinablePartition clusters;

    private final int n, m;

    private final int[] tail;
    private final int[] label;
    private final int[] head;

    private final int[] inTransitionsTrans;
    private final int[] inTransitionsStates;
    private final int[] link;

    private final int[] touchedItems;
    private int touchedItemsPtr;

    private final int[] sCount;
    private final int[] lCount;
    private int lCounters;

    /**
     * Default constructor. Note that the transitions are expected to be sorted first by their label and second by their
     * source states. This can be typically achieved by initializing the arrays in a nested loop similar to
     * <pre> {@code
     * for (int in : inputs) {
     *     for (int src : states) {
     *         for (int tgt : successors(src, in)) {
     *             tail[m] = src;
     *             label[m] = in;
     *             head[m] = tgt;
     *             m++;
     *         }
     *     }
     * }
     * }</pre>
     * Further constraints are documented in the respective parameter descriptions.
     * <p>
     * Note that the contents of the arrays may be modified during the computation of the relational coarsest
     * partition.
     *
     * @param blocks
     *         The initial classification of states. That is, {@code blocks[i]} denotes that class of state {@code i}.
     *         The classes need to be numbered continuously from 0 to k - 1, where k denotes the number of initial
     *         partition classes. Thus, the length of {@code blocks} implies the number of states.
     * @param tail
     *         The source states of transitions. That is, {@code tail[i]} denotes the source state of transition
     *         {@code i}. Each value must be smaller than {@code blocks.length}.
     * @param label
     *         The labels of transitions. That is, {@code label[i]} denotes the label of transition {@code i}. The
     *         labels need to be numbered continuously from 0 to l - 1, where l denotes the number of labels.
     * @param head
     *         The target states of transitions. That is, {@code tail[i]} denotes the target state of transition
     *         {@code i}. Each value must be smaller than {@code blocks.length}.
     */
    public Valmari(int[] blocks, int[] tail, int[] label, int[] head) {

        this.n = blocks.length;
        this.m = tail.length;

        this.blocks = new RefinablePartition(n, blocks);
        this.clusters = new RefinablePartition(m);

        this.tail = tail;
        this.label = label;
        this.head = head;

        this.inTransitionsTrans = new int[m];
        this.inTransitionsStates = new int[n + 1];
        this.link = new int[m];

        this.touchedItems = new int[Math.max(blocks.length, tail.length)];

        this.sCount = new int[n];
        this.lCount = new int[m];
        this.lCounters = -1;
    }

    public void computeCoarsestStablePartition() {
        if (n > 0 && m > 0) {
            initializeBlocks();
            initializeClusters();
            initializeInTransitions();

            mainLoop();
        }
    }

    void initializeBlocks() {
        // sidx is initialized in the constructor

        for (int i = 0; i < n; i++) {
            blocks.elems[i] = i;
        }

        ArrayUtil.heapsort(blocks.elems, blocks.sidx);

        int set1 = blocks.sidx[blocks.elems[0]];
        for (int l = 0; l < n; l++) {
            blocks.loc[blocks.elems[l]] = l;
            int set2 = blocks.sidx[blocks.elems[l]];
            if (set1 != set2) {
                blocks.end[blocks.sets] = l;
                blocks.sets++;
                blocks.first[blocks.sets] = l;
                blocks.mid[blocks.sets] = l;
                set1 = set2;
            }
        }

        blocks.end[blocks.sets] = n;
    }

    void initializeClusters() {

        int prevTail = -1, prevLabel = -1, prevLabel2 = label[0];

        for (int t = 0; t < m; t++) {
            clusters.elems[t] = t;
            clusters.loc[clusters.elems[t]] = t;

            // setup counters
            if (tail[t] != prevTail || label[t] != prevLabel) {
                prevTail = tail[t];
                prevLabel = label[t];
                lCounters++;
            }

            link[t] = lCounters;
            lCount[lCounters]++;

            // setup boundaries
            if (label[t] != prevLabel2) {
                clusters.end[clusters.sets] = t;
                clusters.sets++;
                clusters.first[clusters.sets] = t;
                clusters.mid[clusters.sets] = t;
                prevLabel2 = label[t];
            }
            clusters.sidx[t] = clusters.sets;
        }

        clusters.end[clusters.sets] = m;
    }

    void initializeInTransitions() {
        // essentially a counting sort on head

        for (int i = 0; i < m; i++) {
            inTransitionsStates[head[i]]++;
        }

        // prefix sum
        for (int i = 1; i <= n; i++) {
            inTransitionsStates[i] += inTransitionsStates[i - 1];
        }

        for (int i = 0; i < m; i++) {
            inTransitionsTrans[--inTransitionsStates[head[i]]] = i;
        }
    }

    void mainLoop() {
        int currentCluster = 0;
        int currentBlock = 1;

        while (currentCluster <= clusters.sets) {
            splitBlocks(currentCluster);
            currentCluster++;
            while (currentBlock <= blocks.sets) {
                splitClusters(currentBlock);
                currentBlock++;
            }
        }
    }

    void splitClusters(int currentBlock) {
        for (int i = blocks.first[currentBlock]; i < blocks.end[currentBlock]; i++) {
            int s = blocks.elems[i];
            for (int j = inTransitionsStates[s]; j < inTransitionsStates[s + 1]; j++) {
                int t = inTransitionsTrans[j];
                int c = clusters.mark(t);
                if (c >= 0) {
                    touchedItems[touchedItemsPtr++] = c;
                }
            }
        }

        while (touchedItemsPtr > 0) {
            clusters.split(touchedItems[--touchedItemsPtr]);
        }
    }

    void splitBlocks(int currentCluster) {

        for (int e = clusters.first[currentCluster]; e < clusters.end[currentCluster]; e++) {
            int t = clusters.elems[e];
            sCount[tail[t]]++;
        }

        // Extract left blocks
        for (int j = clusters.first[currentCluster]; j < clusters.end[currentCluster]; j++) {
            int t = clusters.elems[j];
            int s = tail[t];
            int i = link[t];
            if (sCount[s] == lCount[i]) {
                int b = blocks.mark(s);
                if (b >= 0) {
                    touchedItems[touchedItemsPtr++] = b;
                }
                sCount[s] = 0;
            }
        }

        while (touchedItemsPtr > 0) {
            blocks.split(touchedItems[--touchedItemsPtr]);
        }

        // Extract middle blocks
        for (int j = clusters.first[currentCluster]; j < clusters.end[currentCluster]; j++) {
            int t = clusters.elems[j];
            int s = tail[t];
            int i = link[t];

            if (sCount[s] < 0) {
                link[t] = -sCount[s];
            } else if (sCount[s] > 0) {
                int b = blocks.mark(s);
                if (b >= 0) {
                    touchedItems[touchedItemsPtr++] = b;
                }
                lCounters++;
                lCount[lCounters] = sCount[s];
                lCount[i] -= sCount[s];
                sCount[s] = -lCounters;
                link[t] = lCounters;
            }
        }

        for (int i = clusters.first[currentCluster]; i < clusters.end[currentCluster]; i++) {
            int t = clusters.elems[i];
            sCount[tail[t]] = 0;
        }

        while (touchedItemsPtr > 0) {
            blocks.split(touchedItems[--touchedItemsPtr]);
        }
    }

    /**
     * A refinable partition data structure. It maintains a partition {A<sub>0</sub>, A<sub>1</sub>, ..., A<sub>sets -
     * 1</sub>} of the set {0, 1, ..., items - 1} for some integer constant {@code items}.
     */
    public static class RefinablePartition {

        /**
         * Tells the number of sets. This value is 0-indexed, so there exist {@code sets + 1} partition classes.
         */
        public int sets;
        /**
         * Contains {@code 0, 1, ..., items-1} in such an order that elements that belong to the same set are adjacent.
         */
        public final int[] elems;
        /**
         * This field and {@code end} indicate the segment in {@code elems} where the elements of a set are stored. That
         * is, A<sub>s</sub> = {{@code elems[f]}, {@code elems[f+1]}, ..., {@code elems[l−1]}}, where f =
         * {@code first[s]} and l = {@code end[s]}.
         */
        public final int[] first;
        /**
         * Divides the segment of a set to the subsegments of marked and unmarked elements. Let f = {@code first[s]} and
         * l = {@code end[s]}. Then A'<sub>s</sub> = {elems[f], ..., elems[mid[s]−1]} and the unmarked elements are
         * {elems[mid[s]], ..., elems[l−1]}.
         */
        public final int[] mid;
        /**
         * This field and {@code first} indicate the segment in {@code elems} where the elements of a set are stored.
         * That is, A<sub>s</sub> = {{@code elems[f]}, {@code elems[f+1]}, ..., {@code elems[l−1]}}, where f =
         * {@code first[s]} and l = {@code end[s]}.
         */
        public final int[] end;
        /**
         * Tells the locations of elements in {@code elems}. That is, {@code elems[loc[e]] = e} and
         * {@code loc[elems[i]] = i}.
         */
        public final int[] loc;
        /**
         * Maps elements to the indices of the sets that the elements belong to. That is, e &isin;
         * A<sub>{@code sidx[e]}</sub>.
         */
        public final int[] sidx;

        RefinablePartition(int size) {
            this(size, new int[size]);
        }

        RefinablePartition(int size, int[] sidx) {
            this.sets = 0;
            this.elems = new int[size];
            this.first = new int[size];
            this.mid = new int[size];
            this.end = new int[size];
            this.loc = new int[size];
            this.sidx = sidx;
        }

        int mark(int e) {
            final int s = sidx[e];
            final int l = loc[e];
            final int m = mid[s];

            mid[s] = m + 1;
            elems[l] = elems[m];
            loc[elems[l]] = l;
            elems[m] = e;
            loc[e] = m;

            if (m == first[s]) {
                return s;
            } else {
                return -1;
            }
        }

        void split(int s) {
            if (mid[s] == end[s]) {
                mid[s] = first[s];
            }

            if (mid[s] == first[s]) {
                return;
            }

            sets++;

            if (mid[s] - first[s] < end[s] - mid[s]) {
                first[sets] = first[s];
                end[sets] = mid[s];
                first[s] = mid[s];
            } else {
                end[sets] = end[s];
                first[sets] = mid[s];
                end[s] = mid[s];
                mid[s] = first[s];
            }

            mid[sets] = first[sets];

            for (int l = first[sets]; l < end[sets]; l++) {
                sidx[elems[l]] = sets;
            }
        }
    }
}
