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
package net.automatalib.util.automata.vpda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.automata.vpda.State;
import net.automatalib.commons.smartcollections.ArrayStorage;
import net.automatalib.commons.util.Pair;
import net.automatalib.util.automata.conformance.SPATestsIterator;
import net.automatalib.util.automata.conformance.WpMethodTestsIterator;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.util.automata.vpda.SPAConverter.ConversionResult;
import net.automatalib.util.automata.vpda.SymbolMapper.StringSymbolMapper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultVPDAlphabet;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class OneSEVPAUtilTest {

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
        final VPDAlphabet<I> alphabet = sevpa.getInputAlphabet();

        final ArrayStorage<Word<I>> accessSequences = OneSEVPAUtil.computeAccessSequences(sevpa, alphabet);
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
        final VPDAlphabet<I> alphabet = sevpa.getInputAlphabet();

        final List<L> reachableLocations = OneSEVPAUtil.findReachableLocations(sevpa, alphabet);

        Assert.assertEquals(new HashSet<>(reachableLocations), new HashSet<>(sevpa.getLocations()));
    }

    @Test(dataProvider = "systems")
    public <L, I> void testFindSeparatingWord(OneSEVPA<L, I> sevpa) {
        final VPDAlphabet<I> alphabet = sevpa.getInputAlphabet();
        final ArrayStorage<Word<I>> as = OneSEVPAUtil.computeAccessSequences(sevpa, alphabet);

        for (L l1 : sevpa.getLocations()) {
            for (L l2 : sevpa.getLocations()) {
                final Pair<Word<I>, Word<I>> sepWord = OneSEVPAUtil.findSeparatingWord(sevpa, l1, l2, alphabet);

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
        final VPDAlphabet<I> alphabet = sevpa.getInputAlphabet();
        final ArrayStorage<Word<I>> accessSequences = OneSEVPAUtil.computeAccessSequences(sevpa, alphabet);
        final List<Pair<Word<I>, Word<I>>> cSet = new ArrayList<>(OneSEVPAUtil.findCharacterizingSet(sevpa, alphabet));

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
        final VPDAlphabet<I> alphabet = sevpa.getInputAlphabet();

        final String mainProcedure = "main";
        final ArrayStorage<Word<I>> accessSequences = OneSEVPAUtil.computeAccessSequences(sevpa, alphabet);
        final List<Pair<Word<I>, Word<I>>> cSet = new ArrayList<>(OneSEVPAUtil.findCharacterizingSet(sevpa, alphabet));
        final ConversionResult<I, String> conversionResult =
                OneSEVPAUtil.toSPA(sevpa, alphabet, mainProcedure, new StringSymbolMapper<>(), minimize);

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

    private static DefaultOneSEVPA<Character> getRandomSystem() {
        final Random random = new Random(42);
        final Alphabet<Character> callCalphabet = Alphabets.characters('A', 'C');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'f');
        final Alphabet<Character> returnAlphabet = Alphabets.characters('X', 'Z');
        final VPDAlphabet<Character> alphabet =
                new DefaultVPDAlphabet<>(internalAlphabet, callCalphabet, returnAlphabet);

        return RandomAutomata.randomOneSEVPA(random, 5, alphabet, 0.5, 0.5, true);
    }

    private static DefaultOneSEVPA<Character> getSingleReturnRandomSystem() {
        final Random random = new Random(1337);
        final Alphabet<Character> callCalphabet = Alphabets.characters('A', 'C');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'f');
        final Alphabet<Character> returnAlphabet = Alphabets.singleton('R');
        final VPDAlphabet<Character> alphabet =
                new DefaultVPDAlphabet<>(internalAlphabet, callCalphabet, returnAlphabet);

        return RandomAutomata.randomOneSEVPA(random, 10, alphabet, 0.5, 0.5, true);
    }

    private static DefaultOneSEVPA<Character> getCCARCBRRSystem() {
        final Alphabet<Character> callCalphabet = Alphabets.singleton('c');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'b');
        final Alphabet<Character> returnAlphabet = Alphabets.singleton('r');
        final VPDAlphabet<Character> alphabet =
                new DefaultVPDAlphabet<>(internalAlphabet, callCalphabet, returnAlphabet);

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
