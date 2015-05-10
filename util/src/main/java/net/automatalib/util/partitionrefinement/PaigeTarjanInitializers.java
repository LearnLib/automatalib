package net.automatalib.util.partitionrefinement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.simple.SimpleDeterministicAutomaton;
import net.automatalib.words.Alphabet;

public class PaigeTarjanInitializers {
	
	public static void initCompleteDeterministic(
			PaigeTarjan pt,
			UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> absAutomaton) {
		initCompleteDeterministic(pt, absAutomaton, absAutomaton::getStateProperty);
	}
	
	public static void initCompleteDeterministic(
			PaigeTarjan pt,
			SimpleDeterministicAutomaton.FullIntAbstraction absAutomaton,
			IntFunction<?> initialClassification) {
		int numStates = absAutomaton.size();
		int numInputs = absAutomaton.numInputs();
		
		int posDataLow = numStates;
		int predOfsDataLow = posDataLow + numStates;
		int numTransitions = numStates * numInputs;
		int predDataLow = predOfsDataLow + numTransitions + 1;
		int dataSize = predDataLow + numTransitions;
		
		int[] data = new int[dataSize];
		Block[] blockForState = new Block[numStates];
		
		Map<Object,Block> blockMap = new HashMap<>();
		
		int init = absAutomaton.getIntInitialState();
		Object initClass = initialClassification.apply(init);
		
		Block initBlock = pt.createBlock();
		initBlock.high = 1;
		blockForState[init] = initBlock;
		blockMap.put(initClass, initBlock);
		
		int[] statesBuff = new int[numStates];
		statesBuff[0] = init;
		
		int statesPtr = 0;
		int reachableStates = 1;
		
		while (statesPtr < reachableStates) {
			int curr = statesBuff[statesPtr++];
			int predCountBase = predOfsDataLow;
			
			for (int i = 0; i < numInputs; i++) {
				int succ = absAutomaton.getSuccessor(curr, i);
				if (succ < 0) {
					throw new IllegalArgumentException("Automaton must not be partial");
				}
				
				Block succBlock = blockForState[succ];
				if (succBlock == null) {
					Object succClass = initialClassification.apply(succ);
					succBlock = blockMap.get(succClass);
					if (succBlock == null) {
						succBlock = pt.createBlock();
						succBlock.high = 0;
						blockMap.put(succClass, succBlock);
					}
					succBlock.high++;
					blockForState[succ] = succBlock;
					statesBuff[reachableStates++] = succ;
				}
				data[predCountBase + succ]++;
				predCountBase += numStates;
			}
		}
		
		int curr = 0;
		for (Block b : pt.blockList()) {
			curr += b.high;
			b.high = curr;
			b.low = curr;
		}
		
		data[predOfsDataLow] += predDataLow;
		prefixSum(data, predOfsDataLow, predDataLow);
		
		for (int i = 0; i < reachableStates; i++) {
			int stateId = statesBuff[i];
			Block b = blockForState[stateId];
			int pos = --b.low;
			data[pos] = stateId;
			data[posDataLow + stateId] = pos;
			
			int predOfsBase = predOfsDataLow;
			
			for (int j = 0; j < numInputs; j++) {
				int succ = absAutomaton.getSuccessor(stateId, j);
				assert succ >= 0;
			
				data[--data[predOfsBase + succ]] = stateId;
				predOfsBase += numStates;
			}
		}
		
		pt.blockData = data;
		pt.posData = data;
		pt.posDataLow = posDataLow;
		pt.predOfsData = data;
		pt.predOfsDataLow = predOfsDataLow;
		pt.predData = data;
		pt.blockForState = blockForState;
		pt.numStates = numStates;
		pt.numInputs = numInputs;
	}
	
	public static void initCompleteConnectedDeterministic(
			PaigeTarjan pt,
			SimpleDeterministicAutomaton.FullIntAbstraction absAutomaton,
			IntFunction<?> initialClassification) {
		int numStates = absAutomaton.size();
		int numInputs = absAutomaton.numInputs();
		
		int posDataLow = numStates;
		int predOfsDataLow = posDataLow + numStates;
		int numTransitions = numStates * numInputs;
		int predDataLow = predOfsDataLow + numTransitions + 1;
		int dataSize = predDataLow + numTransitions;
		
		int[] data = new int[dataSize];
		Block[] blockForState = new Block[numStates];
		
		Map<Object,Block> blockMap = new HashMap<>();
		
		for (int i = 0; i < numStates; i++) {
			Object classification = initialClassification.apply(i);
			Block block = blockMap.get(classification);
			if (block == null) {
				block = pt.createBlock();
				block.high = 0;
				blockMap.put(classification, block);
			}
			block.high++;
			blockForState[i] = block;
			
			int predCountBase = predOfsDataLow;
			
			for (int j = 0; j < numInputs; j++) {
				int succ = absAutomaton.getSuccessor(i, j);
				if (succ < 0) {
					throw new IllegalArgumentException("Automaton must not be partial");
				}
				
				data[predCountBase + succ]++;
				predCountBase += numStates;
			}
		}
		
		int curr = 0;
		for (Block b : pt.blockList()) {
			curr += b.high;
			b.high = curr;
			b.low = curr;
		}
		
		data[predOfsDataLow] += predDataLow;
		prefixSum(data, predOfsDataLow, predDataLow);
		
		for (int i = 0; i < numStates; i++) {
			Block b = blockForState[i];
			int pos = --b.low;
			data[pos] = i;
			data[posDataLow + i] = pos;			
			int predOfsBase = predOfsDataLow;
			
			for (int j = 0; j < numInputs; j++) {
				int succ = absAutomaton.getSuccessor(i, j);
				assert succ >= 0;
			
				data[--data[predOfsBase + succ]] = i;
				predOfsBase += numStates;
			}
		}
		
		pt.blockData = data;
		pt.posData = data;
		pt.posDataLow = posDataLow;
		pt.predOfsData = data;
		pt.predOfsDataLow = predOfsDataLow;
		pt.predData = data;
		pt.blockForState = blockForState;
		pt.numStates = numStates;
		pt.numInputs = numInputs;
	}

	public static <S,I> StateIDs<S> initDeterministic(
			PaigeTarjan pt,
			SimpleDeterministicAutomaton<S, I> automaton,
			Alphabet<I> inputs,
			Function<? super S,?> initialClassification,
			Object sinkClassification) {
		int numStates = automaton.size();
		int numInputs = inputs.size();
		
		int sinkId = numStates;
		int numStatesWithSink = numStates + 1;
		int posDataLow = numStatesWithSink;
		int predOfsDataLow = posDataLow + numStatesWithSink;
		int numTransitionsFull = numStatesWithSink * numInputs;
		int predDataLow = predOfsDataLow + numTransitionsFull + 1;
		int dataSize = predDataLow + numTransitionsFull;
		
		int[] data = new int[dataSize];
		Block[] blockForState = new Block[numStatesWithSink];
		
		StateIDs<S> ids = automaton.stateIDs();
		
		Map<Object,Block> blockMap = new HashMap<>();
		
		S init = automaton.getInitialState();
		int initId = ids.getStateId(init);
		
		Object initClass = initialClassification.apply(init);
		
		Block initBlock = pt.createBlock();
		initBlock.high = 1;
		blockForState[initId] = initBlock;
		blockMap.put(initClass, initBlock);
		
		int[] statesBuff = new int[numStatesWithSink];
		statesBuff[0] = initId;
		
		int statesPtr = 0;
		int reachableStates = 1;
		
		boolean partial = false;
		while (statesPtr < reachableStates) {
			int currId = statesBuff[statesPtr++];
			if (currId == sinkId) {
				continue;
			}
			S curr = ids.getState(currId);
			
			int predCountBase = predOfsDataLow;
			
			for (int i = 0; i < numInputs; i++) {
				I sym = inputs.getSymbol(i);
				
				S succ = automaton.getSuccessor(curr, sym);
				int succId;
				if (succ != null) {
					succId = ids.getStateId(succ);
				}
				else {
					succId = sinkId;
					partial = true;
				}
				Block succBlock = blockForState[succId];
				if (succBlock == null) {
					Object succClass;
					if (succ != null) {
						succClass = initialClassification.apply(succ);
					}
					else {
						succClass = sinkClassification;
					}
					succBlock = blockMap.get(succClass);
					if (succBlock == null) {
						succBlock = pt.createBlock();
						succBlock.high = 0;
						blockMap.put(succClass, succBlock);
					}
					succBlock.high++;
					blockForState[succId] = succBlock;
					statesBuff[reachableStates++] = succId;
				}
				data[predCountBase + succId]++;
				predCountBase += numStatesWithSink;
			}
		}
		
		if (partial) {
			int predCountIdx = predOfsDataLow + sinkId;
			for (int i = 0; i < numInputs; i++) {
				data[predCountIdx]++;
				predCountIdx += numStatesWithSink;
			}
		}
		
		int curr = 0;
		for (Block b : pt.blockList()) {
			curr += b.high;
			b.high = curr;
			b.low = curr;
		}
		
		data[predOfsDataLow] += predDataLow;
		prefixSum(data, predOfsDataLow, predDataLow);
		
		if (partial) {
			int predOfsIdx = predOfsDataLow + sinkId;
			for (int i = 0; i < numInputs; i++) {
				data[--data[predOfsIdx]] = sinkId;
				predOfsIdx += numStatesWithSink;
			}
		}
		
		for (int i = 0; i < reachableStates; i++) {
			int stateId = statesBuff[i];
			Block b = blockForState[stateId];
			int pos = --b.low;
			data[pos] = stateId;
			data[posDataLow + stateId] = pos;
			
			S state = ids.getState(stateId);
			
			int predOfsBase = predOfsDataLow;
			
			for (int j = 0; j < numInputs; j++) {
				I sym = inputs.getSymbol(j);
				S succ = automaton.getSuccessor(state, sym);
				int succId;
				if (succ == null) {
					succId = sinkId;
				}
				else {
					succId = ids.getStateId(succ);
				}
				
				data[--data[predOfsBase + succId]] = stateId;
				predOfsBase += numStatesWithSink;
			}
		}
		
		pt.blockData = data;
		pt.posData = data;
		pt.posDataLow = posDataLow;
		pt.predOfsData = data;
		pt.predOfsDataLow = predOfsDataLow;
		pt.predData = data;
		pt.blockForState = blockForState;
		pt.numStates = numStatesWithSink;
		pt.numInputs = numInputs;
		
		return ids;
	}
	
	public static <S,I,T> StateIDs<S> initDeterministic(
			PaigeTarjan pt,
			DeterministicAutomaton<S, I, T> automaton,
			Alphabet<I> inputs,
			Predicate<? super S> initialClassification,
			boolean sinkClassification) {
		int numStates = automaton.size();
		int numInputs = inputs.size();
		
		int sinkId = numStates;
		int numStatesWithSink = numStates + 1;
		int posDataLow = numStatesWithSink;
		int predOfsDataLow = posDataLow + numStatesWithSink;
		int numTransitionsFull = numStatesWithSink * numInputs;
		int predDataLow = predOfsDataLow + numTransitionsFull + 1;
		int dataSize = predDataLow + numTransitionsFull;
		
		int[] data = new int[dataSize];
		Block[] blockForState = new Block[numStatesWithSink];
		
		StateIDs<S> ids = automaton.stateIDs();
		
		S init = automaton.getInitialState();
		int initId = ids.getStateId(init);
		
		int falsePtr = 0;
		int truePtr = numStatesWithSink;
		
		Block falseBlock = pt.createBlock();
		Block trueBlock = pt.createBlock();
		
		boolean initClass = initialClassification.test(init);
		
		int initPos;
		if (initClass) {
			blockForState[initId] = trueBlock;
			initPos = --truePtr;
		}
		else {
			blockForState[initId] = falseBlock;
			initPos = falsePtr++;
		}
		data[initPos] = initId;
		data[posDataLow + initId] = initPos;
		
		int currFalse = 0;
		int currTrue = numStatesWithSink;
		
		int pending = 1;
		boolean partial = false;
		
		while (pending-- > 0) {
			int stateId = -1;
			if (currFalse < falsePtr) {
				stateId = data[currFalse++];
			}
			else if (currTrue > truePtr) {
				stateId = data[--currTrue];
			}
			else {
				throw new AssertionError();
			}
			
			
			S state = ids.getState(stateId);
			
			int predCountBase = predOfsDataLow;
			
			for (int i = 0; i < numInputs; i++) {
				I sym = inputs.getSymbol(i);
				T trans = automaton.getTransition(state, sym);
				int succId;
				if (trans == null) {
					partial = true;
					succId = sinkId;
				}
				else {
					S succ = automaton.getSuccessor(trans);
					succId = ids.getStateId(succ);
					
					if (blockForState[succId] == null) {
						boolean succClass = initialClassification.test(succ);
						int succPos;
						if (succClass) {
							blockForState[succId] = trueBlock;
							succPos = --truePtr;
						}
						else {
							blockForState[succId] = falseBlock;
							succPos = falsePtr++;
						}
						data[succPos] = succId;
						data[posDataLow + succId] = succPos;
						pending++;
					}
					
					data[predCountBase + succId]++;
				}
				
				predCountBase += numStatesWithSink;
			}
		}
		
		if (partial) {
			int pos;
			if (sinkClassification) {
				blockForState[sinkId] = trueBlock;
				pos = --truePtr;
			}
			else {
				blockForState[sinkId] = falseBlock;
				pos = falsePtr++;
			}
			data[pos] = sinkId;
			data[posDataLow + sinkId] = pos;
			
			int predCountIdx = predOfsDataLow + sinkId;
			
			for (int i = 0; i < numInputs; i++) {
				data[predCountIdx]++;
				predCountIdx += numStatesWithSink;
			}
		}
		
		falseBlock.low = 0;
		falseBlock.high = falsePtr;
		trueBlock.low = truePtr;
		trueBlock.high = numStatesWithSink;
		
		data[predOfsDataLow] += predDataLow;
		prefixSum(data, predOfsDataLow, predDataLow);
		
		if (partial) {
			int predOfsIdx = predOfsDataLow + sinkId;
			for (int i = 0; i < numInputs; i++) {
				data[--data[predOfsIdx]] = sinkId;
				predOfsIdx += numStatesWithSink;
			}
		}
		
		for (int i = 0; i < falsePtr; i++) {
			int stateId = data[i];
			S state = ids.getState(stateId);
			int predOfsBase = predOfsDataLow;
			for (int j = 0; j < numInputs; j++) {
				I sym = inputs.getSymbol(j);
				T trans = automaton.getTransition(state, sym);
				int succId;
				if (trans == null) {
					succId = sinkId;
				}
				else {
					S succ = automaton.getSuccessor(trans);
					succId = ids.getStateId(succ);
				}
				data[--data[predOfsBase + succId]] = stateId;
				
				predOfsBase += numStatesWithSink;
			}
		}
		
		for (int i = truePtr; i < numStatesWithSink; i++) {
			int stateId = data[i];
			S state = ids.getState(stateId);
			int predOfsBase = predOfsDataLow;
			for (int j = 0; j < numInputs; j++) {
				I sym = inputs.getSymbol(j);
				T trans = automaton.getTransition(state, sym);
				int succId;
				if (trans == null) {
					succId = sinkId;
				}
				else {
					S succ = automaton.getSuccessor(trans);
					succId = ids.getStateId(succ);
				}
				data[--data[predOfsBase + succId]] = stateId;
				
				predOfsBase += numStatesWithSink;
			}
		}
		
		pt.blockData = data;
		pt.posData = data;
		pt.posDataLow = posDataLow;
		pt.predOfsData = data;
		pt.predOfsDataLow = predOfsDataLow;
		pt.predData = data;
		pt.blockForState = blockForState;
		pt.numStates = numStatesWithSink;
		pt.numInputs = numInputs;
		
		pt.removeEmptyBlocks();
		
		return ids;
	}
	

	
	private static void prefixSum(int[] array, int startInclusive, int endExclusive) {
		int curr = array[startInclusive];
		for (int i = startInclusive + 1; i < endExclusive; i++) {
			curr += array[i];
			array[i] = curr;
		}
	}

}
