package net.automatalib.util.fixedpoint;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class ClosuresTest {

    @Test
    @Ignore
    void hideSymbols() {

        CompactDFA<String> dfa = new CompactDFA<String>(Alphabets.fromArray("a", "b", "c"));

        Integer s0 = dfa.addInitialState();
        Integer s1 = dfa.addState();
        Integer s2 = dfa.addState();
        Integer s3 = dfa.addState(true);

        dfa.addTransition(s0, "a", s1);
        dfa.addTransition(s1, "b", s2);
        dfa.addTransition(s2, "c", s3);

        Function<Set<Integer>, Set<Integer>> op = Closures.toClosureOperator(dfa,
                                                                             dfa.getInputAlphabet(),
                                                                             (s, i, t) -> "b".equals(i));

        Pair<Map<Set<Integer>, Integer>, CompactDFA<String>> rv = Closures.closure(dfa,
                                                                                  Alphabets.fromArray("a", "c"),
                                                                                  CompactDFA<String>::new,
                                                                                  op,
                                                                                  (s, i, t) -> true);
        CompactDFA<String> dfa2 = rv.getSecond();
        assertThat(dfa2.getStates(), hasSize(4));
    }

    @Test
    @Ignore
    void hideChain() {

        CompactDFA<String> dfa = new CompactDFA<String>(Alphabets.fromArray("a", "b", "c"));

        Integer s0 = dfa.addInitialState();
        Integer s1 = dfa.addState();
        Integer s2 = dfa.addState();
        Integer s3 = dfa.addState();
        Integer s4 = dfa.addState();
        Integer s5 = dfa.addState();
        Integer s6 = dfa.addState(true);

        dfa.addTransition(s0, "a", s1);
        dfa.addTransition(s1, "b", s2);
        dfa.addTransition(s2, "b", s3);
        dfa.addTransition(s3, "b", s4);
        dfa.addTransition(s4, "b", s5);
        dfa.addTransition(s5, "c", s6);

        Function<Set<Integer>, Set<Integer>> op = Closures.toClosureOperator(dfa,
                                                                             dfa.getInputAlphabet(),
                                                                             (s, i, t) -> "b".equals(i));

        Pair<Map<Set<Integer>, Integer>, CompactDFA<String>> rv = Closures.closure(dfa,
                                                                                   Alphabets.fromArray("a", "c"),
                                                                                   CompactDFA<String>::new,
                                                                                   op,
                                                                                   (s, i, t) -> true);
        CompactDFA<String> dfa2 = rv.getSecond();
        assertThat(dfa2.getStates(), hasSize(4));

        for (Map.Entry<Set<Integer>, Integer> stateClosures : rv.getFirst().entrySet()) {
            if (stateClosures.getKey().contains(s0)) {
                dfa2.setInitialState(stateClosures.getValue());
            }
            if (stateClosures.getKey().contains(s6)) {
                dfa2.setAccepting(stateClosures.getValue(), true);
            }
        }

        Assert.assertTrue(dfa2.accepts(Arrays.asList("a", "c")));
    }


}