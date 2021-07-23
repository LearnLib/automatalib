/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.modelcheckers.m3c.util;

import java.util.Collections;
import java.util.Set;

import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.MutableModalProcessGraph;
import net.automatalib.graphs.base.DefaultMCFPS;
import net.automatalib.graphs.base.compact.CompactMPG;

public final class Examples {

    private Examples() {}

    public static ModalContextFreeProcessSystem<String, String> getMcfpsAnBn(Set<String> finalNodesAP) {
        final CompactMPG<String, String> mpg = buildMPG(new CompactMPG<>(), finalNodesAP);
        return new DefaultMCFPS<>("P", Collections.singletonMap("P", mpg));
    }

    private static <N, E, AP, MMPG extends MutableModalProcessGraph<N, String, E, AP, ?>> MMPG buildMPG(MMPG mpg,
                                                                                                        Set<AP> finalNodeAPs) {

        final N start = mpg.addNode();
        final N end = mpg.addNode();
        final N s1 = mpg.addNode();
        final N s2 = mpg.addNode();

        mpg.setInitialNode(start);
        mpg.setFinalNode(end);
        mpg.setAtomicPropositions(s2, finalNodeAPs);
        mpg.setAtomicPropositions(end, finalNodeAPs);

        final E e1 = mpg.connect(start, s1);
        final E e2 = mpg.connect(start, end);
        final E e3 = mpg.connect(s1, s2);
        final E e4 = mpg.connect(s2, end);

        mpg.getEdgeProperty(e1).setMust();
        mpg.setEdgeLabel(e1, "a");

        mpg.getEdgeProperty(e2).setMust();
        mpg.setEdgeLabel(e2, "e");

        mpg.getEdgeProperty(e3).setMust();
        mpg.getEdgeProperty(e3).setProcess();
        mpg.setEdgeLabel(e3, "P");

        mpg.getEdgeProperty(e4).setMust();
        mpg.setEdgeLabel(e4, "b");

        return mpg;
    }

}
