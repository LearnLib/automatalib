package net.automatalib.util.automata.minimizer.hopcroft;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.simple.SimpleDeterministicAutomaton;
import net.automatalib.commons.util.array.RichArray;
import net.automatalib.words.Alphabet;

public class PaigeTarjan {
	
	private Block worklistHead;
	private Block worklistTail;
	
	private Block touchedHead;
	
	protected int numInputs;
	protected int numStates;
	
	protected int[] blockData;
	protected int blockDataLow;
	
	protected int[] posData;
	protected int posDataLow;
	
	protected int[] predOfsData;
	protected int predOfsDataLow;
	
	protected int[] predData;
	protected int predDataLow;
	
	protected Block[] blockForState;
	
	
	private Block blocklistHead = null;
	private int numBlocks = 0;
	
	public <S,I> StateIDs<S> initDeterministic(
			SimpleDeterministicAutomaton<S, I> automaton,
			Alphabet<I> inputs,
			Function<? super S,?> initialClassification,
			Object sinkClassification) {
		int numStates = automaton.size();
		int numInputs = inputs.size();
		
		int sinkId = numStates;
		int numStatesWithSink = numStates + 1;
		int blockDataLow = 0;
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
		
		Block initBlock = createBlock();
		initBlock.high = 1;
		blockForState[initId] = initBlock;
		
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
						succBlock = createBlock();
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
		for (Block b = blocklistHead; b != null; b = b.nextBlock) {
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
		
		this.blockData = data;
		this.blockDataLow = 0;
		this.posData = data;
		this.posDataLow = posDataLow;
		this.predOfsData = data;
		this.predOfsDataLow = predOfsDataLow;
		this.predData = data;
		this.predDataLow = predDataLow;
		this.blockForState = blockForState;
		this.numStates = numStatesWithSink;
		this.numInputs = numInputs;
		
		return ids;
	}
	
	public <S,I,T> StateIDs<S> initDeterministic(DeterministicAutomaton<S, I, T> automaton,
			Alphabet<I> inputs,
			Predicate<? super S> initialClassification,
			boolean sinkClassification) {
		int numStates = automaton.size();
		int numInputs = inputs.size();
		
		int sinkId = numStates;
		int numStatesWithSink = numStates + 1;
		int blockDataLow = 0;
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
		
		Block falseBlock = createBlock();
//		falseBlock.data = data;
		Block trueBlock = createBlock();
//		trueBlock.data = data;
		
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
		
		this.blockData = data;
		this.blockDataLow = 0;
		this.posData = data;
		this.posDataLow = posDataLow;
		this.predOfsData = data;
		this.predOfsDataLow = predOfsDataLow;
		this.predData = data;
		this.predDataLow = predDataLow;
		this.blockForState = blockForState;
		this.numStates = numStatesWithSink;
		this.numInputs = numInputs;
		
		removeEmptyBlocks();
		
		return ids;
	}
		
	private static void prefixSum(int[] array, int startInclusive, int endExclusive) {
		int curr = array[startInclusive];
		for (int i = startInclusive + 1; i < endExclusive; i++) {
			curr += array[i];
			array[i] = curr;
		}
	}
	
	private Block poll() {
		if (worklistHead == null) {
			return null;
		}
		Block b = worklistHead;
		worklistHead = b.nextInWorklist;
		b.nextInWorklist = null;
		if (worklistHead == null) {
			worklistTail = null;
		}
		
		return b;
	}
	
	public void removeEmptyBlocks() {
		Block curr = blocklistHead;
		Block prev = null;
		int effId = 0;
		while (curr != null) {
			if (!curr.isEmpty()) {
				curr.id = effId++;
				if (prev != null) {
					prev.nextBlock = curr;
				}
				else {
					blocklistHead = curr;
				}
				prev = curr;
			}
			curr = curr.nextBlock;
		}
		if (prev != null) {
			prev.nextBlock = null;
		}
		else {
			blocklistHead = null;
		}
		numBlocks = effId;
	}
	
	public void initWorklist(boolean addAll) {
		if (addAll) {
			Block last = null;
			for (Block b = blocklistHead; b != null; b = b.nextBlock) {
				b.nextInWorklist = b.nextBlock;
				last = b;
			}
			worklistHead = blocklistHead;
			worklistTail = last;
		}
		else {
			Block largest = blocklistHead;
			if (largest == null) {
				return;
			}
			int largestSize = largest.size();
			for (Block b = largest.nextBlock; b != null; b = b.nextBlock) {
				int size = b.size();
				if (size > largestSize) {
					addToWorklist(largest);
					largest = b;
					largestSize = size;
				}
				else {
					addToWorklist(b);
				}
			}
		}
	}
	
	public void computeCoarsestStablePartition() {
		Block curr;
		while ((curr = poll()) != null) {
			int currLow = curr.low, currHigh = curr.high;
			int predOfsBase = predOfsDataLow;
//			int[] blockData = curr.data;
			for (int i = 0; i < numInputs; i++) {
				for (int j = currLow; j < currHigh; j++) {
					int state = blockData[j];
					int predOfsIdx = predOfsBase + state;
					int predLow = predOfsData[predOfsIdx], predHigh = predOfsData[predOfsIdx + 1];
					for (int k = predLow; k < predHigh; k++) {
						int pred = predData[k];
						moveLeft(pred);
					}
				}
				predOfsBase += numStates;
				processTouched();
			}
		}
	}
	
	private Block split(Block b) {
		Block splt = b.split(numBlocks, blocklistHead);
		if (splt == null) {
			return null;
		}
		numBlocks++;
		blocklistHead = splt;
		int spltLow = splt.low, spltHigh = splt.high;
//		int[] blockData = splt.data;
		for (int i = spltLow; i < spltHigh; i++) {
			int state = blockData[i];
			blockForState[state] = splt;
		}
		return splt;
	}
	
	private void processTouched() {
		Block b = touchedHead;
		while (b != null) {
			Block next = b.nextTouched;
			b.nextTouched = null;
			Block splt = split(b);
			if (splt != null) {
				addToWorklist(splt);
			}
			b.ptr = -1;
			b = next;
		}
			
		touchedHead = null;
	}
	
	private void addToWorklist(Block b) {
		if (worklistHead == null) {
			worklistHead = b;
			worklistTail = b;
		}
		else {
			worklistTail.nextInWorklist = b;
			worklistTail = b;
		}
	}
	
	private void moveLeft(int state) {
		Block b = blockForState[state];
//		int[] blockData = b.data;
		int posIdx = posDataLow + state;
		int inBlockIdx = posData[posIdx];
		int ptr = b.ptr;
		
		if (ptr == -1) {
			b.nextTouched = touchedHead;
			touchedHead = b;
			b.ptr = ptr = b.low;
		}
		
		if (ptr <= inBlockIdx) {
			if (ptr < inBlockIdx) {
				int other = blockData[ptr];
				blockData[ptr] = blockData[inBlockIdx];
				blockData[inBlockIdx] = other;
				
				posData[posIdx] = ptr;
				posData[posDataLow + other] = inBlockIdx;
			}
			b.ptr = ++ptr;
		}
	}
	
	public Block createBlock() {
		Block b = new Block(-1, -1, numBlocks++, blocklistHead);
		blocklistHead = b;
		return b;
	}
	
	public Block getBlockForState(int id) {
		return blockForState[id];
	}
	
	public int getRepresentative(Block b) {
//		return b.data[b.low];
		return blockData[b.low];
	}
	
	public Spliterator.OfInt statesInBlockSpliterator(Block b) {
		return Arrays.spliterator(blockData, b.low, b.high);
	}
	
	public PrimitiveIterator.OfInt statesInBlockIterator(Block b) {
		return Spliterators.iterator(statesInBlockSpliterator(b));
	}
	
	
	public Iterator<Block> blockListIterator() {
		return blockListIterator();
	}
	
	public Iterable<Block> blockList() {
		return () -> blockListIterator();
	}
	
	
	public <S1,S2,I,T1,T2,SP,TP,A extends MutableDeterministic<S2, I, T2, SP, TP>>
	A toDeterministic(AutomatonCreator<A, I> creator, Alphabet<I> inputs,
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
		
		A result = creator.createAutomaton(inputs, numBlocks);
		RichArray<S2> states = new RichArray<>(numBlocks);
		
		for (Block curr = blocklistHead; curr != null; curr = curr.nextBlock) {
			int blockId = curr.id;
			S1 rep = origIds.getState(getRepresentative(curr));
			SP sp = spExtractor.apply(rep);
			S2 resState = result.addState(sp);
			states.update(blockId, resState);
		}
		for (Block curr = blocklistHead; curr != null; curr = curr.nextBlock) {
			int blockId = curr.id;
			S1 rep = origIds.getState(getRepresentative(curr));
			S2 resultState = states.get(blockId);
			
			for (I sym : inputs) {
				T1 origTrans = original.getTransition(rep, sym);
				Block succBlock;
				TP tp;
				if (origTrans != null) {
					tp = tpExtractor.apply(origTrans);
					S1 origSucc = original.getSuccessor(origTrans);
					int origSuccId = origIds.getStateId(origSucc);
					succBlock = blockForState[origSuccId];
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
		S2 resInit = states.get(blockForState[origInitId].id);
		result.setInitialState(resInit);
		
		return result;
	}

}
