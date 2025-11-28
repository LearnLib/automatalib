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
package net.automatalib.util.automaton.cover;

import java.util.Collections;
import java.util.List;

import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.symbol.time.TimedInput;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class MMLTCoverTest {

    private CompactMMLT<String, String> buildBaseModel() {
        var alphabet = Alphabets.fromArray("p1", "p2", "abort", "collect");
        var model = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());

        var s0 = model.addInitialState();
        var s1 = model.addState();
        var s2 = model.addState();
        var s3 = model.addState();

        model.addTransition(s0, "p1", s1, "go");
        model.addTransition(s1, "abort", s1, "ok");
        model.addLocalReset(s1, "abort");

        model.addPeriodicTimer(s1, "a", 3, "part");
        model.addPeriodicTimer(s1, "b", 6, "noise");
        model.addOneShotTimer(s1, "c", 40, "done", s3);

        model.addTransition(s0, "p2", s2, "go");
        model.addTransition(s2, "abort", s3, "void");
        model.addOneShotTimer(s2, "d", 4, "done", s3);

        model.addTransition(s3, "collect", s0, "void");

        return model;
    }

    @Test
    public void computeFullCover() {
        var automaton = buildBaseModel();
        var cover = MMLTCover.getMMLTLocationCover(automaton, automaton.getSemantics().getInputAlphabet());

        // Verify that all states are covered:
        Assert.assertEquals(cover.size(), automaton.size());
        for (var state : automaton.getStates()) {
            Assert.assertTrue(cover.containsKey(state));
        }

        // Verify that the assigned prefixes are correct:
        for (var entry : cover.entrySet()) {
            var targetConfig = automaton.getSemantics().getState(entry.getValue());
            Assert.assertNotNull(targetConfig);
            Assert.assertTrue(targetConfig.isEntryConfig());
            Assert.assertEquals(targetConfig.getLocation(), entry.getKey());
        }
    }

    @Test
    public void computeIncompleteCover() {
        var automaton = buildBaseModel();

        // Create alphabet where state 3 is unreachable:
        var symbols = List.of("p1", "p2", "collect");
        var partialAlphabet = symbols.stream().map(TimedInput::input).collect(Alphabets.collector());

        // Test if detecting incomplete cover:
        Assert.assertThrows(IllegalStateException.class,
                            () -> MMLTCover.getMMLTLocationCover(automaton, partialAlphabet, false));

        // Verify that state 3 is not covered:
        var cover = MMLTCover.getMMLTLocationCover(automaton, partialAlphabet);
        Assert.assertTrue(cover.size() < automaton.size());
        for (var state : automaton.getStates()) {
            if (state != 3) {
                Assert.assertTrue(cover.containsKey(state));
            } else {
                Assert.assertFalse(cover.containsKey(state));
            }
        }
    }

    @Test
    public void computeEmptyCover() {
        var automaton = buildBaseModel();
        automaton.setInitial(automaton.getInitialState(), false);

        var alphabet = automaton.getSemantics().getInputAlphabet();

        // Test if cover is empty
        var cover = MMLTCover.getMMLTLocationCover(automaton, alphabet);
        Assert.assertTrue(cover.isEmpty());

        cover = MMLTCover.getMMLTLocationCover(automaton, Collections.emptyList());
        Assert.assertTrue(cover.isEmpty());

        // Test if detecting incomplete cover
        Assert.assertThrows(IllegalStateException.class,
                            () -> MMLTCover.getMMLTLocationCover(automaton, alphabet, false));

        Assert.assertThrows(IllegalStateException.class,
                            () -> MMLTCover.getMMLTLocationCover(automaton, Collections.emptyList(), false));

    }
}
