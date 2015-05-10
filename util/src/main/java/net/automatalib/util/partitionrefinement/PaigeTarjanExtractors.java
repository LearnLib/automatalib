package net.automatalib.util.partitionrefinement;

import java.util.function.Function;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.array.RichArray;
import net.automatalib.words.Alphabet;

public class PaigeTarjanExtractors {
	
	

	public static <S1,S2,I,T1,T2,SP,TP,A extends MutableDeterministic<S2, I, T2, SP, TP>>
	A toDeterministic(
			PaigeTarjan pt,
			AutomatonCreator<A, I> creator, Alphabet<I> inputs,
			DeterministicAutomaton<S1, I, T1> original,
			StateIDs<S1> origIds,
			Function<? super S1,? extends SP> spExtractor,
			Function<? super T1,? extends TP> tpExtractor) {
		
		if (spExtractor == null) {
			spExtractor = (s) -> null;
		}
		if (tpExtractor == null) {
			tpExtractor = (t) -> null;
		}
		
		int numBlocks = pt.getNumBlocks();
		
		A result = creator.createAutomaton(inputs, numBlocks);
		RichArray<S2> states = new RichArray<>(numBlocks);
		
		for (Block curr : pt.blockList()) {
			int blockId = curr.id;
			S1 rep = origIds.getState(pt.getRepresentative(curr));
			SP sp = spExtractor.apply(rep);
			S2 resState = result.addState(sp);
			states.update(blockId, resState);
		}
		for (Block curr : pt.blockList()) {
			int blockId = curr.id;
			S1 rep = origIds.getState(pt.getRepresentative(curr));
			S2 resultState = states.get(blockId);
			
			for (I sym : inputs) {
				T1 origTrans = original.getTransition(rep, sym);
				Block succBlock;
				TP tp;
				if (origTrans != null) {
					tp = tpExtractor.apply(origTrans);
					S1 origSucc = original.getSuccessor(origTrans);
					int origSuccId = origIds.getStateId(origSucc);
					succBlock = pt.getBlockForState(origSuccId);
				}
				else {
					succBlock = null;
					tp = null;
				}
				S2 resultSucc = states.get(succBlock.id);
				result.setTransition(resultState, sym, resultSucc, tp);
			}
		}
		
		S1 origInit = original.getInitialState();
		int origInitId = origIds.getStateId(origInit);
		S2 resInit = states.get(pt.getBlockForState(origInitId).id);
		result.setInitialState(resInit);
		
		return result;
	}
}
