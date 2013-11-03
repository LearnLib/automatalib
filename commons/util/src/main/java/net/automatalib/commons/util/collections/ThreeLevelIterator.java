package net.automatalib.commons.util.collections;

import java.util.Iterator;


public abstract class ThreeLevelIterator<L1, L2, L3, O> implements Iterator<O> {
	
	private static class Outer<L1,L2> {
		private L1 l1Item;
		private L2 l2Item;
	}
	
	private class OuterIterator extends TwoLevelIterator<L1,L2,Outer<L1,L2>> {

		private final Outer<L1,L2> value = new Outer<>();
		
		public OuterIterator(Iterator<L1> l1Iterator) {
			super(l1Iterator);
		}

		@Override
		protected Iterator<L2> l2Iterator(L1 l1Object) {
			return ThreeLevelIterator.this.l2Iterator(l1Object);
		}

		@Override
		protected Outer<L1, L2> combine(L1 l1Object, L2 l2Object) {
			value.l1Item = l1Object;
			value.l2Item = l2Object;
			return value;
		}
	}
	
	private class InnerIterator extends TwoLevelIterator<Outer<L1,L2>,L3,O> {
		public InnerIterator(Iterator<Outer<L1, L2>> outerIterator) {
			super(outerIterator);
		}
		@Override
		protected Iterator<L3> l2Iterator(Outer<L1, L2> outer) {
			return ThreeLevelIterator.this.l3Iterator(outer.l1Item, outer.l2Item);
		}
		@Override
		protected O combine(Outer<L1, L2> outer, L3 l3Object) {
			return ThreeLevelIterator.this.combine(outer.l1Item, outer.l2Item, l3Object);
		}
	}
	
	private final InnerIterator innerIterator;
	
	public ThreeLevelIterator(Iterator<L1> l1Iterator) {
		OuterIterator outerIterator = new OuterIterator(l1Iterator);
		this.innerIterator = new InnerIterator(outerIterator);
	}

	
	protected abstract Iterator<L2> l2Iterator(L1 l1Object);
	protected abstract Iterator<L3> l3Iterator(L1 l1Object, L2 l2Object);
	
	
	protected abstract O combine(L1 l1Object, L2 l2Object, L3 l3Object);


	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return innerIterator.hasNext();
	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public O next() {
		return innerIterator.next();
	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		innerIterator.remove();
	}	
	
	

}
