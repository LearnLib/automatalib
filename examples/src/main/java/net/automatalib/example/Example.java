package net.automatalib.example;

import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.semantics.DeterministicFiniteSemantics;
import net.automatalib.util.automaton.Automata;

public class Example {

    public static void main(String[] args) {

        var alphabet = Alphabets.integers(1,3);

        var dfa = new CompactDFA<>(alphabet);
        var nfa = new CompactNFA<>(alphabet);
        var mealy = new CompactMealy<>(alphabet);
        var mmlt = new CompactMMLT<>(alphabet, "", StringSymbolCombiner.getInstance());

        Automata.findSeparatingWord(nfa, nfa, alphabet);
        Automata.findSeparatingWord(nfa, dfa, alphabet);
        Automata.findSeparatingWord(dfa, dfa, alphabet);
        Automata.findSeparatingWord(dfa, mealy, alphabet);
        Automata.findSeparatingWord(mealy, mealy, alphabet);
        Automata.findSeparatingWord(mealy, mmlt, alphabet);
        Automata.findSeparatingWord(mealy, DeterministicFiniteSemantics.fromAutomaton(mmlt), alphabet);
    }

}
