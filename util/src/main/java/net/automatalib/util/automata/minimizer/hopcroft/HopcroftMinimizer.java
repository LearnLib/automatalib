package net.automatalib.util.automata.minimizer.hopcroft;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class HopcroftMinimizer<S,I> extends PaigeTarjan {
	
	
	public static final class MealySignature {
		
		public static <S,I,T>
		MealySignature build(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton, Alphabet<I> alphabet, S state) {
			int numInputs = alphabet.size();
			Object[] outputs = new Object[numInputs];
			for (int i = 0; i < numInputs; i++) {
				I sym = alphabet.getSymbol(i);
				T trans = automaton.getTransition(state, sym);
				Object transProp = automaton.getTransitionProperty(trans);
				outputs[i] = transProp;
			}
			return new MealySignature(outputs);
		}
		
		public static MealySignature build(int numInputs, Object output) {
			Object[] outputs = new Object[numInputs];
			Arrays.fill(outputs, output);
			return new MealySignature(outputs);
		}
		
		private final Object[] transOutputs;
		
		private MealySignature(Object[] transOutputs) {
			this.transOutputs = transOutputs;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null) {
				return false;
			}
			if (o.getClass() != MealySignature.class) {
				return false;
			}
			MealySignature other = (MealySignature) o;
			return Arrays.equals(transOutputs, other.transOutputs);
		}
		
		@Override
		public int hashCode() {
			return Arrays.hashCode(transOutputs);
		}
	}
	
	public static <I,O> CompactMealy<I, O> minimizeMealy(MealyMachine<?, I, ?, O> mealy, Alphabet<I> alphabet) {
		return doMinimizeMealy(mealy, alphabet);
	}
	
	public static <I,O,A extends MealyMachine<?,I,?,O> & InputAlphabetHolder<I>>
	CompactMealy<I,O> minimizeMealy(A mealy) {
		return doMinimizeMealy((MealyMachine<?,I,?,O>) mealy, mealy.getInputAlphabet());
	}
	
	public static <S,I> CompactDFA<I> minimizeDFA(DFA<S,I> dfa, Alphabet<I> alphabet) {
		PaigeTarjan pr = new PaigeTarjan();
		
		StateIDs<S> ids = pr.initDeterministic(dfa, alphabet, dfa::getStateProperty, Boolean.FALSE);	
		pr.initWorklist(false);
		pr.computeCoarsestStablePartition();
		
		return pr.toDeterministic(
				new CompactDFA.Creator<I>(),
				alphabet,
				dfa,
				ids,
				dfa::isAccepting,
				null);
	}
	
	private static <S,I,T,O> CompactMealy<I,O> doMinimizeMealy(MealyMachine<S,I,T,O> mealy, Alphabet<I> alphabet) {
		PaigeTarjan pr = new PaigeTarjan();
		
		StateIDs<S> ids = pr.initDeterministic(mealy, alphabet, s -> MealySignature.build(mealy, alphabet, s), MealySignature.build(alphabet.size(), null));
		pr.initWorklist(false);
		pr.computeCoarsestStablePartition();
		
		return pr.toDeterministic(
				new CompactMealy.Creator<I,O>(),
				alphabet,
				mealy,
				ids,
				null,
				mealy::getTransitionOutput);
	}
	
	public static int NUM_DFAS = 100;
	public static int NUM_STATES = 100000;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		Alphabet<Integer> alphabet = Alphabets.integers(0, 4);
		CompactMealy<Integer,String>[] dfas = new CompactMealy[NUM_DFAS];
		Random rand = new Random(2);
		for (int i = 0; i < NUM_DFAS; i++) {
			dfas[i] = RandomAutomata.randomMealy(rand, NUM_STATES, alphabet, Arrays.asList("foo", "bar", "baz"), false);
			// .randomDFA(rand, NUM_STATES, alphabet, false);
		}
		
		System.out.println("Press any key to start minimization");
//		System.in.read();
		long start = System.nanoTime();
		for (int i = 0; i < NUM_DFAS; i++) {
			CompactMealy<Integer,String> minimized = minimizeMealy(dfas[i], alphabet);
//			if (Automata.findShortestSeparatingWord(dfas[i], minimized, alphabet) != null) {
//				throw new AssertionError();
//			}
//			CompactMealy<Integer,String> oldMinimized = Automata.minimize(dfas[i], alphabet, new CompactMealy<Integer,String>(alphabet));
//			if (minimized.size() != oldMinimized.size()) {
//				throw new AssertionError();
//			}
		}
		long duration = System.nanoTime() - start;
		
		System.out.println("Minimization (new) took " + (duration/1000000L) + "ms");
		
//		if (true) 		return;
//		
//		start = System.nanoTime();
//		for (int i = 0; i < NUM_DFAS; i++) {
//			Automata.minimize(dfas[i], alphabet, new CompactDFA<Integer>(alphabet));
//		}
//		duration = System.nanoTime() - start;
//		
//		System.out.println("Minimization (old) took " + (duration/1000000L) + "ms");
	}

}
