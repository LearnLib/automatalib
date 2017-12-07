package ${package};

import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * Creates an example automaton over the alphabet {a, b} that accepts any word with an even number of a's.
 */
public class Example {

    public static void main(String[] args) {

        // input alphabet containing characters 'a'..'b'
        final Alphabet<Character> sigma = Alphabets.characters('a', 'b');

        // create automaton
        final DFA<?, Character> automaton = AutomatonBuilders.newDFA(sigma)
                                                             .withInitial("q0")
                                                             .from("q0")
                                                                 .on('a').to("q1")
                                                                 .on('b').loop()
                                                             .from("q1")
                                                                 .on('a').to("q0")
                                                                 .on('b').loop()
                                                             .withAccepting("q0")
                                                             .create();

        // check some words
        System.out.println(automaton.accepts(Word.fromCharSequence("aab")));
        System.out.println(automaton.accepts(Word.fromCharSequence("aaaaa")));
        System.out.println(automaton.accepts(Word.fromCharSequence("babab")));

        // visualize automaton
        Visualization.visualizeAutomaton(automaton, sigma, false);
    }
}
