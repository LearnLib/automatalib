/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.modelchecker.m3c.util;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.alphabet.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.CompactDFA;
import net.automatalib.automaton.fsa.FastDFA;
import net.automatalib.automaton.procedural.StackSBA;
import net.automatalib.graph.CompactPMPG;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.graph.DefaultCFMPS;
import net.automatalib.graph.MutableProceduralModalProcessGraph;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.fsa.MutableDFAs;
import net.automatalib.util.automaton.procedural.SBAs;

public final class Examples {

    private Examples() {}

    public static ContextFreeModalProcessSystem<String, String> getCfmpsAnBn(Set<String> finalNodesAP) {
        final CompactPMPG<String, String> pmpg = buildPMPG(new CompactPMPG<>(""), finalNodesAP);
        return new DefaultCFMPS<>("P", Collections.singletonMap("P", pmpg));
    }

    public static ContextFreeModalProcessSystem<String, Void> getSBASystem() {
        final Alphabet<String> internalAlphabet = Alphabets.closedCharStringRange('a', 'd');
        final Alphabet<String> callAlphabet = Alphabets.fromArray("P1", "P2", "P3", "P4");
        final ProceduralInputAlphabet<String> alphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, "R");

        // @formatter:off
        final CompactDFA<String> p1 = AutomatonBuilders.forDFA(new CompactDFA<>(alphabet))
                                                       .withInitial("s0")
                                                       .from("s0").on("P2").to("s1")
                                                       .from("s0").on("a").to("s1")
                                                       .from("s1").on("R").to("s2")
                                                       .withAccepting("s0", "s1", "s2")
                                                       .create();
        final FastDFA<String> p2 = AutomatonBuilders.forDFA(new FastDFA<>(alphabet))
                                                    .withInitial("t0")
                                                    .from("t0").on("b").to("t1")
                                                    .from("t0").on("P3").to("t2")
                                                    .from("t0").on("P4").to("t2")
                                                    .from("t1").on("R").to("t2")
                                                    .withAccepting("t0", "t1", "t2")
                                                    .create();
        final FastDFA<String> p3 = AutomatonBuilders.forDFA(new FastDFA<>(alphabet))
                                                    .withInitial("t0")
                                                    .from("t0").on("c").to("t1")
                                                    .from("t0").on("P4").to("t2")
                                                    .from("t1").on("d").to("t2")
                                                    .withAccepting("t0", "t1", "t2")
                                                    .create();
        final CompactDFA<String> p4 = AutomatonBuilders.forDFA(new CompactDFA<>(alphabet))
                                                       .withInitial("t0")
                                                       .withAccepting("t0", "t1", "t2")
                                                       .create();
        // @formatter:on

        MutableDFAs.complete(p3, alphabet, true);

        // explicit type variable declaration to make checker-framework happy
        final StackSBA<?, String> sba =
                new StackSBA<>(alphabet, "P1", ImmutableMap.of("P1", p1, "P2", p2, "P3", p3, "P4", p4));

        return SBAs.toCFMPS(sba);
    }

    private static <N, E, AP, M extends MutableProceduralModalProcessGraph<N, String, E, AP, ?>> M buildPMPG(M pmpg,
                                                                                                             Set<AP> finalNodeAPs) {

        final N start = pmpg.addNode();
        final N end = pmpg.addNode();
        final N s1 = pmpg.addNode();
        final N s2 = pmpg.addNode();

        pmpg.setInitialNode(start);
        pmpg.setFinalNode(end);
        pmpg.setAtomicPropositions(s2, finalNodeAPs);
        pmpg.setAtomicPropositions(end, finalNodeAPs);

        final E e1 = pmpg.connect(start, s1);
        final E e2 = pmpg.connect(start, end);
        final E e3 = pmpg.connect(s1, s2);
        final E e4 = pmpg.connect(s2, end);

        pmpg.getEdgeProperty(e1).setMust();
        pmpg.setEdgeLabel(e1, "a");

        pmpg.getEdgeProperty(e2).setMust();
        pmpg.setEdgeLabel(e2, "e");

        pmpg.getEdgeProperty(e3).setMust();
        pmpg.getEdgeProperty(e3).setProcess();
        pmpg.setEdgeLabel(e3, "P");

        pmpg.getEdgeProperty(e4).setMust();
        pmpg.setEdgeLabel(e4, "b");

        return pmpg;
    }

}
