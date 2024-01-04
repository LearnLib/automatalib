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
package net.automatalib.util.automaton.vpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultVPAlphabet;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.automaton.vpa.State;
import net.automatalib.automaton.vpa.impl.DefaultOneSEVPA;
import net.automatalib.automaton.vpa.impl.Location;
import net.automatalib.common.smartcollection.ArrayStorage;
import net.automatalib.common.util.Pair;
import net.automatalib.util.automaton.conformance.SPATestsIterator;
import net.automatalib.util.automaton.conformance.WpMethodTestsIterator;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.util.automaton.vpa.SPAConverter.ConversionResult;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OneSEVPAsTest {

    @DataProvider(name = "systems")
    public static Object[][] getSystems() {
        return new Object[][] {{getRandomSystem()}, {getCCARCBRRSystem()}};
    }

    @DataProvider(name = "spaSystems")
    public static Object[][] getSPASystems() {

        final List<Object[]> configs = new ArrayList<>();

        for (DefaultOneSEVPA<Character> s : Arrays.asList(getSingleReturnRandomSystem(), getCCARCBRRSystem())) {
            for (Boolean m : Arrays.asList(true, false)) {
                configs.add(new Object[] {s, m});
            }
        }

        return configs.toArray(new Object[configs.size()][]);
    }

    @Test(dataProvider = "systems")
    public <L, I> void testAccessSequenceSet(OneSEVPA<L, I> sevpa) {
        final VPAlphabet<I> alphabet = sevpa.getInputAlphabet();

        final ArrayStorage<Word<I>> accessSequences = OneSEVPAs.computeAccessSequences(sevpa, alphabet);
        final Set<L> locations = Sets.newHashSetWithExpectedSize(sevpa.size());

        for (Word<I> as : accessSequences) {
            final State<L> s = sevpa.getState(as);
            Assert.assertNotNull(s);
            Assert.assertTrue(locations.add(s.getLocation()));
        }

        Assert.assertEquals(locations.size(), sevpa.size());
    }

    @Test(dataProvider = "systems")
    public <L, I> void testReachableLocations(OneSEVPA<L, I> sevpa) {
        final VPAlphabet<I> alphabet = sevpa.getInputAlphabet();

        final List<L> reachableLocations = OneSEVPAs.findReachableLocations(sevpa, alphabet);

        Assert.assertEquals(new HashSet<>(reachableLocations), new HashSet<>(sevpa.getLocations()));
    }

    @Test(dataProvider = "systems")
    public <L, I> void testFindSeparatingWord(OneSEVPA<L, I> sevpa) {
        final VPAlphabet<I> alphabet = sevpa.getInputAlphabet();
        final ArrayStorage<Word<I>> as = OneSEVPAs.computeAccessSequences(sevpa, alphabet);

        for (L l1 : sevpa.getLocations()) {
            for (L l2 : sevpa.getLocations()) {
                final Pair<Word<I>, Word<I>> sepWord = OneSEVPAs.findSeparatingWord(sevpa, l1, l2, alphabet);

                if (Objects.equals(l1, l2)) {
                    Assert.assertNull(sepWord);
                } else {
                    Assert.assertNotNull(sepWord);
                    final Word<I> pref = sepWord.getFirst();
                    final Word<I> suff = sepWord.getSecond();
                    Assert.assertNotEquals(sevpa.accepts(Word.fromWords(pref, as.get(sevpa.getLocationId(l1)), suff)),
                                           sevpa.accepts(Word.fromWords(pref, as.get(sevpa.getLocationId(l2)), suff)));
                }
            }
        }
    }

    @Test(dataProvider = "systems")
    public <L, I> void testCharacterizingSet(OneSEVPA<L, I> sevpa) {
        final VPAlphabet<I> alphabet = sevpa.getInputAlphabet();
        final ArrayStorage<Word<I>> accessSequences = OneSEVPAs.computeAccessSequences(sevpa, alphabet);
        final List<Pair<Word<I>, Word<I>>> cSet = new ArrayList<>(OneSEVPAs.findCharacterizingSet(sevpa, alphabet));

        final Set<boolean[]> signatures = Sets.newHashSetWithExpectedSize(sevpa.size());

        for (L l : sevpa.getLocations()) {
            final Word<I> as = accessSequences.get(sevpa.getLocationId(l));
            final boolean[] signature = new boolean[cSet.size()];
            int idx = 0;

            for (Pair<Word<I>, Word<I>> p : cSet) {
                final Word<I> w = Word.fromWords(p.getFirst(), as, p.getSecond());
                Assert.assertTrue(alphabet.isWellMatched(w));
                signature[idx++] = sevpa.accepts(w);
            }

            Assert.assertTrue(signatures.add(signature));
        }

        Assert.assertEquals(signatures.size(), sevpa.size());
    }

    @Test(dataProvider = "spaSystems")
    public <L, I> void testToSPA(OneSEVPA<L, I> sevpa, boolean minimize) {
        final VPAlphabet<I> alphabet = sevpa.getInputAlphabet();

        final String mainProcedure = "main";
        final ArrayStorage<Word<I>> accessSequences = OneSEVPAs.computeAccessSequences(sevpa, alphabet);
        final List<Pair<Word<I>, Word<I>>> cSet = new ArrayList<>(OneSEVPAs.findCharacterizingSet(sevpa, alphabet));
        final ConversionResult<I, String> conversionResult =
                OneSEVPAs.toSPA(sevpa, alphabet, mainProcedure, new StringSymbolMapper<>(), minimize);

        for (Word<I> as : accessSequences) {
            for (Pair<Word<I>, Word<I>> cs : cSet) {
                final Word<I> w = Word.fromWords(cs.getFirst(), as, cs.getSecond());
                final Word<String> mapped = conversionResult.mapper.apply(w);

                Assert.assertEquals(conversionResult.spa.accepts(mapped), sevpa.accepts(w), w + " -> " + mapped);
            }
        }

        if (minimize) { // in the non-minimized SPA, unreachable procedures have no access or return sequences
            final SPATestsIterator<String> wpIter =
                    new SPATestsIterator<>(conversionResult.spa, WpMethodTestsIterator::new);

            while (wpIter.hasNext()) {
                final Word<String> test = wpIter.next();
                final Word<String> cropped = test.subWord(1, test.length() - 1);

                if (!cropped.asList().contains(mainProcedure)) {
                    final Word<I> w = cropped.transform(conversionResult.reverseMapping::get);
                    final Word<String> mapped = conversionResult.mapper.apply(w);

                    Assert.assertEquals(conversionResult.spa.accepts(mapped), sevpa.accepts(w), w + " -> " + mapped);
                }
            }
        }
    }

    @Test
    public void testMinimization() {

        final VPAlphabet<Character> alphabet = new DefaultVPAlphabet<>(Alphabets.characters('1', '3'),
                                                                       Alphabets.characters('a', 'c'),
                                                                       Alphabets.characters('r', 't'));
        final int size = 10;
        final double accProb = 0.5;
        final double initRetProb = 0.1;

        final DefaultOneSEVPA<Character> orig =
                RandomAutomata.randomOneSEVPA(new Random(0), size, alphabet, accProb, initRetProb, false);
        final DefaultOneSEVPA<Character> copy =
                RandomAutomata.randomOneSEVPA(new Random(0), size, alphabet, accProb, initRetProb, false);

        Assert.assertNotNull(copy);
        Assert.assertNotNull(orig);
        Assert.assertTrue(OneSEVPAs.testEquivalence(orig, copy, alphabet));

        addRedundantState(copy, alphabet);

        Assert.assertTrue(orig.size() < copy.size());
        Assert.assertTrue(OneSEVPAs.testEquivalence(orig, copy, alphabet));

        final DefaultOneSEVPA<Character> minimized = OneSEVPAs.minimize(copy, alphabet);

        Assert.assertNotNull(minimized);
        Assert.assertTrue(minimized.size() < copy.size());
        Assert.assertTrue(OneSEVPAs.testEquivalence(copy, minimized, alphabet));
    }

    private static <I> void addRedundantState(DefaultOneSEVPA<I> automaton, VPAlphabet<? extends I> alphabet) {

        // cache reached states, so we copy the first state reached by two incoming transitions
        final Set<Location> locationCache = Sets.newHashSetWithExpectedSize(automaton.size());

        Location incomingLoc = null;
        I incomingInput = null;
        Location locToCopy = null;

        outer:
        for (Location l : automaton.getLocations()) {
            for (I i : alphabet.getInternalAlphabet()) {
                final Location succ = automaton.getInternalSuccessor(l, i);
                if (!locationCache.add(succ)) {
                    incomingLoc = l;
                    incomingInput = i;
                    locToCopy = succ;
                    break outer;
                }
            }
        }

        Assert.assertNotNull(incomingLoc);
        Assert.assertNotNull(incomingInput);
        Assert.assertNotNull(locToCopy);

        final Set<Location> oldStates = new HashSet<>(automaton.getLocations());
        final Location locCopy = automaton.addLocation(automaton.isAcceptingLocation(locToCopy));

        // make return transitions of old states behave identical for the new stack symbol
        for (I callSym : alphabet.getCallAlphabet()) {
            final int oldStackSym = automaton.encodeStackSym(locToCopy, callSym);
            final int newStackSym = automaton.encodeStackSym(locCopy, callSym);

            for (Location l : oldStates) {
                for (I retSym : alphabet.getReturnAlphabet()) {
                    final Location oldReturn = automaton.getReturnSuccessor(l, retSym, oldStackSym);
                    automaton.setReturnSuccessor(l, retSym, newStackSym, oldReturn);
                }
            }
        }

        // make internal transitions of new state behave identical to the state to copy
        for (I i : alphabet.getInternalAlphabet()) {
            final Location target = automaton.getInternalSuccessor(locToCopy, i);
            automaton.setInternalSuccessor(locCopy, i, target);
        }

        // make return transitions of new state behave identical to the state to copy
        for (I i : alphabet.getReturnAlphabet()) {
            for (int stackSym = 0; stackSym < automaton.getNumStackSymbols(); stackSym++) {
                final Location target = automaton.getReturnSuccessor(locToCopy, i, stackSym);
                automaton.setReturnSuccessor(locCopy, i, stackSym, target);
            }
        }

        // change old transition to redundant state
        automaton.setInternalSuccessor(incomingLoc, incomingInput, locCopy);
    }

    private static DefaultOneSEVPA<Character> getRandomSystem() {
        final Random random = new Random(42);
        final Alphabet<Character> callCalphabet = Alphabets.characters('A', 'C');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'f');
        final Alphabet<Character> returnAlphabet = Alphabets.characters('X', 'Z');
        final VPAlphabet<Character> alphabet = new DefaultVPAlphabet<>(internalAlphabet, callCalphabet, returnAlphabet);

        return RandomAutomata.randomOneSEVPA(random, 10, alphabet, 0.5, 0.5, true);
    }

    private static DefaultOneSEVPA<Character> getSingleReturnRandomSystem() {
        final Random random = new Random(1337);
        final Alphabet<Character> callCalphabet = Alphabets.characters('A', 'C');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'f');
        final Alphabet<Character> returnAlphabet = Alphabets.singleton('R');
        final VPAlphabet<Character> alphabet = new DefaultVPAlphabet<>(internalAlphabet, callCalphabet, returnAlphabet);

        return RandomAutomata.randomOneSEVPA(random, 10, alphabet, 0.5, 0.5, true);
    }

    private static DefaultOneSEVPA<Character> getCCARCBRRSystem() {
        final Alphabet<Character> callCalphabet = Alphabets.singleton('c');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'b');
        final Alphabet<Character> returnAlphabet = Alphabets.singleton('r');
        final VPAlphabet<Character> alphabet = new DefaultVPAlphabet<>(internalAlphabet, callCalphabet, returnAlphabet);

        final DefaultOneSEVPA<Character> result = new DefaultOneSEVPA<>(alphabet);

        final Location l0 = result.addInitialLocation(false);
        final Location l1 = result.addLocation(false);
        final Location l2 = result.addLocation(false);
        final Location l3 = result.addLocation(false);
        final Location l4 = result.addLocation(false);
        final Location l5 = result.addLocation(true);
        final Location l6 = result.addLocation(false);

        final int s0 = result.encodeStackSym(l0, (Character) 'c');
        final int s2 = result.encodeStackSym(l2, (Character) 'c');

        result.setInternalSuccessor(l0, 'a', l1);
        result.setInternalSuccessor(l0, 'b', l3);

        result.setReturnSuccessor(l1, 'r', s0, l2);
        result.setReturnSuccessor(l3, 'r', s2, l4);
        result.setReturnSuccessor(l4, 'r', s0, l5);

        for (Location l : result.getLocations()) {
            for (Character i : internalAlphabet) {
                final Location succ = result.getInternalSuccessor(l, i);
                if (succ == null) {
                    result.setInternalSuccessor(l, i, l6);
                }
            }
            for (Location s : result.getLocations()) {
                final int sym = result.encodeStackSym(s, (Character) 'c');
                final Location succ = result.getReturnSuccessor(l, 'r', sym);
                if (succ == null) {
                    result.setReturnSuccessor(l, 'r', sym, l6);
                }
            }
        }

        return result;
    }
}
