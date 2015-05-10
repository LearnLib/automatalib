package net.automatalib.util.partitionrefinement;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A low-level realization of the Paige/Tarjan partition refinement algorithm.
 * 
 * @author Malte Isberner
 *
 */
public class PaigeTarjan {
	
	public static enum WorklistPolicy {
		FIFO,
		LIFO
	}
	
	/**
	 * The number of input symbols.
	 */
	public int numInputs;
	/**
	 * The number of states.
	 */
	public int numStates;
	
	/**
	 * The array storing the raw block data, i.e., the states contained in a certain
	 * block. It is assumed that the positions {@link Block#low} and {@link Block#high}
	 * refer to this array.
	 */
	public int[] blockData;
	
	/**
	 * The array storing the position data, i.e., for each state, its index in the
	 * {@link #blockData} array.
	 * <p>
	 * The layout of this array is assumed to be the following: for the state {@code i},
	 * where <code>0 &lt;= i &lt; {@link #numStates}</code>, the index of {@code i}
	 * in {@link #blockData} is <code>{@link #posData}[{@link #posDataLow} + i]</code>.
	 */
	public int[] posData;
	/**
	 * The lowest index storing position data in the {@link #posData} array.
	 */
	public int posDataLow;
	
	/**
	 * The array storing the predecessor offset data, i.e., for each state and input symbol,
	 * the delimiting offsets of the respective predecessor list. The offsets are assumed to refer
	 * to the {@link #predData} array.
	 * <p>
	 * The layout of this array is assumed to be the following: for state {@code i} and input symbol
	 * {@code j}, where <code>0 &lt;= i &lt; {@link #numStates}</code> and
	 * <code>0 &lt;= j &lt; {@link #numInputs}</code>, the offset (in the {@link #predData} array)
	 * of the first {@code j}-predecessor of {@code i} is
	 * <code>{@link #predOfsData}[{@link #predOfsDataLow} + j*{@link #numStates} + i]</code>, and the
	 * last {@code j}-predecessor of {@code i} is
	 * <code>{@link #predOfsData}[{@link #predOfsDataLow} + j*{@link #numStates} + i + 1] - 1</code>.
	 * Note that this requires the index
	 * <code>{@link #predOfsDataLow} + {@link #numInputs} * {@link #numStates}</code> to be valid, and
	 * the contents of the {@link #predOfsData} array at this index must be the highest offset of any
	 * predecessor plus one.
	 */
	public int[] predOfsData;
	/**
	 * The lowest index storing predecessor offset data in the {@link #predOfsData} array.
	 */
	public int predOfsDataLow;
	
	/**
	 * The array storing the predecessor data, i.e., for each state and input symbol,
	 * a list of the respective predecessors.
	 * <p>
	 * The layout of this array is assumed to be the following: for state {@code i} and input symbol
	 * {@code j}, where <code>0 &lt;= i &lt; {@link #numStates}</code> and
	 * <code>0 &lt;= j &lt; {@link #numInputs}</code>, the {@code j}-predecessors of {@code i} are
	 * the elements of {@link #predData} from index
	 * <code>{@link #predOfsData}[{@link #predOfsDataLow + j*{@link #numStates} + i]</code>, inclusive, to index
	 * <code>{@link #predOfsData}[{@link #predOfsDataLow + j*{@link #numStates} + i + 1]</code>, exclusive.
	 */
	public int[] predData;
	
	/**
	 * The array mapping states (in the range between {@code 0} and {@link #numStates}) to their
	 * containing block.
	 */
	public Block[] blockForState;
	
	// the head of the block linked list
	private Block blocklistHead = null;
	// the block count
	private int numBlocks = 0;
	// the head of the worklist linked list
	private Block worklistHead = null;
	// the tail of the worklist linked list
	private Block worklistTail = null;
	// the head of the 'touched' list
	private Block touchedHead = null;
	
	@Nonnull
	private WorklistPolicy worklistPolicy = WorklistPolicy.FIFO;
	
	
	public void setWorklistPolicy(WorklistPolicy policy) {
		this.worklistPolicy = Objects.requireNonNull(policy);
	}
	
	public void setBlockForState(Block[] blockForState) {
		this.blockForState = blockForState;
	}
	
	public void setBlockData(int[] blockData) {
		this.blockData = blockData;
	}
	
	public void setPosData(int[] posData, int posDataLow) {
		this.posData = posData;
		this.posDataLow = posDataLow;
	}
	
	public void setPredOfsData(int[] predOfsData, int predOfsDataLow) {
		this.predOfsData = predOfsData;
		this.predOfsDataLow = predOfsDataLow;
	}
	
	public void setPredData(int[] predData) {
		this.predData = predData;
	}
	
	@Nullable
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
	
	public Block[] createBlockForStateMap() {
		Block[] map = new Block[numStates];
		for (Block b = blocklistHead; b != null; b = b.nextBlock) {
			int low = b.low, high = b.high;
			for (int i = low; i < high; i++) {
				int state = blockData[i];
				map[state] = b;
			}
		}
		return map;
	}
	
	public void initBlockForStateMap() {
		this.blockForState = createBlockForStateMap();
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
		return blockData[b.low];
	}
	
	public Spliterator.OfInt statesInBlockSpliterator(Block b) {
		return Arrays.spliterator(blockData, b.low, b.high);
	}
	
	public PrimitiveIterator.OfInt statesInBlockIterator(Block b) {
		return Spliterators.iterator(statesInBlockSpliterator(b));
	}
	
	
	public Iterator<Block> blockListIterator() {
		return Block.blockListIterator(blocklistHead);
	}
	
	public Iterable<Block> blockList() {
		return () -> blockListIterator();
	}
	
	
	public int getNumBlocks() {
		return numBlocks;
	}
	

}
