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
package net.automatalib.example.ads;

import java.util.HashSet;
import java.util.Optional;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.graph.ads.ADSNode;
import net.automatalib.util.automaton.ads.ADS;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.visualization.Visualization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A small example for computing and displaying adaptive distinguishing sequences.
 */
public final class ADSExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(ADSExample.class);
    private static final Alphabet<Character> ALPHABET = Alphabets.characters('a', 'b');

    private ADSExample() {}

    public static void main(String[] args) {

        final CompactMealy<Character, Integer> mealy = buildAutomaton();

        final Optional<ADSNode<Integer, Character, Integer>> adsOpt =
                ADS.compute(mealy, ALPHABET, new HashSet<>(mealy.getStates()));

        if (adsOpt.isPresent()) {
            Visualization.visualize(adsOpt.get());
        } else {
            LOGGER.info("ADS does not exist");
        }
    }

    /**
     * Builds and returns the automaton M_6 from the chapter "State Identification" (in "Model-Based Testing of Reactive
     * Systems") by Moez Krichen.
     *
     * @return M_6
     */
    private static CompactMealy<Character, Integer> buildAutomaton() {
        // @formatter:off
        return AutomatonBuilders.<Character, Integer>newMealy(ALPHABET)
                .withInitial("s1")
                .from("s1")
                    .on('a').withOutput(0).to("s2")
                    .on('b').withOutput(0).loop()
                .from("s2")
                    .on('a').withOutput(1).to("s3")
                    .on('b').withOutput(0).to("s1")
                .from("s3")
                    .on('a', 'b').withOutput(0).to("s4")
                .from("s4")
                    .on('a').withOutput(1).to("s5")
                    .on('b').withOutput(0).to("s5")
                .from("s5")
                    .on('a', 'b').withOutput(0).to("s6")
                .from("s6")
                    .on('a').withOutput(1).to("s1")
                    .on('b').withOutput(0).to("s1")
                .create();
        // @formatter:on
    }
}
