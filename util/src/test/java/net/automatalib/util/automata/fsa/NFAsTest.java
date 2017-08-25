package net.automatalib.util.automata.fsa;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class NFAsTest {

    public void testDeterminize() throws Exception {
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

        CompactNFA<Integer> nfa = new CompactNFA<>(alphabet);

        int q0 = nfa.addInitialState(false);
        int q1 = nfa.addState(true);

        nfa.addTransition(q0, 0, q0);
        nfa.addTransition(q0, 1, q0);
        nfa.addTransition(q0, 1, q1);

        Assert.assertTrue(nfa.accepts(Word.fromSymbols(0, 1, 0, 1)));
        Assert.assertFalse(nfa.accepts(Word.fromSymbols(0, 1, 0, 1, 0)));

        CompactDFA<Integer> dfa = NFAs.determinize(nfa);

        Assert.assertEquals(dfa.size(), 2);
    }
}
