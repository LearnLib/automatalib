package net.automatalib.util.automata.conformance;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class IncrementalWMethodTestsIteratorTest {

	public static void main(String[] args) {
		Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
		
		MealyMachine<?,Character,?,Character> mealy
			= AutomatonBuilders.<Character,Character>newMealy(alphabet)
				.from("q0").on('a', 'c').withOutput('x').to("q1")
					.on('b').withOutput('y').loop()
				.from("q1").on('a').withOutput('z').to("q2")
					.on('b').withOutput('y').loop()
					.on('c').withOutput('w').to("q0")
				.from("q2")
					.on('a', 'b', 'c').withOutput('u').loop()
				.withInitial("q0")
				.create();
		
		IncrementalWMethodTestsIterator<Character> incIt = new IncrementalWMethodTestsIterator<>(alphabet);
		incIt.setMaxDepth(2);
		incIt.update(mealy);
		
		for(int i = 0; i < 10 && incIt.hasNext(); i++) {
			System.err.println(incIt.next().toString());
		}
	}

}
