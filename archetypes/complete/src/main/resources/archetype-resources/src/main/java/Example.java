package ${package};

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.visualization.Visualization;

/**
 * An example class that creates a simple Mealy Machine and displays it via the JUNG library.
 */
public final class Example {

    private Example() {
        // prevent instantiation
    }

    public static void main(String[] args) {

        final Alphabet<Character> inputAlphabet = Alphabets.characters('a', 'c');

        MealyMachine<?, Character, ?, Character> mealy = AutomatonBuilders.<Character, Character>newMealy(inputAlphabet)
                .withInitial("q0")
                .from("q0").on('a').withOutput('1').to("q1")
                .from("q1").on('b').withOutput('2').to("q2")
                .from("q2").on('c').withOutput('3').to("q0")
                .create();

        Visualization.visualize(mealy.transitionGraphView(inputAlphabet));
    }
}
