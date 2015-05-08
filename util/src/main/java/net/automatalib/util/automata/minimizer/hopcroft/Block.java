package net.automatalib.util.automata.minimizer.hopcroft;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Block {
	
	private static final class BlockListIterator implements Iterator<Block> {
		private Block curr;
		
		public BlockListIterator(Block start) {
			this.curr = start;
		}
		
		@Override
		public boolean hasNext() {
			return curr != null;
		}
		
		@Override
		public Block next() {
			Block result = curr;
			if (result == null) {
				throw new NoSuchElementException();
			}
			curr = result.nextBlock;
			return result;
		}
	}
	
	static Iterator<Block> blockListIterator(Block start) {
			return new BlockListIterator(start);
	}
	
	
	
	protected int low;
	protected int ptr = -1;
	protected int high;
	//protected int[] data;
	
	protected Block nextInWorklist = null;
	protected Block nextTouched = null;
	
	public Block nextBlock;
	public int id;
	
	public Block(int low, int high, int id, Block next) {
		this.low = low;
		this.high = high;
		this.id = id;
		this.nextBlock = next;
	}
	
	public int size() {
		return high - low;
	}
	
	public boolean isEmpty() {
		return low >= high;
	}
	
	public Block split(int newId, Block newNext) {
		int ptr = this.ptr;
		this.ptr = -1;
		int high = this.high;
		int ptrHighDiff = high - ptr;
		if (ptrHighDiff == 0) {
			return null;
		}
		int low = this.low;
		Block splt;
		if (ptrHighDiff > ptr - low) {
			splt = new Block(low, ptr, newId, newNext);
			this.low = ptr;
		}
		else {
			splt = new Block(ptr, high, newId, newNext);
			this.high = ptr;
		}
		//splt.data = data;
		this.ptr = -1;
		return splt;
	}
}
