/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.commons.util.collections;

import java.util.AbstractList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

final class NullList extends AbstractList<Object> implements RandomAccess {
	
	private final int size;
	
	private static final class Iterator implements ListIterator<Object> {
		private final int size;
		private int i = 0;
		
		public Iterator(int size) {
			this.size = size;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return (i < size);
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#next()
		 */
		@Override
		public Object next() {
			if(!hasNext())
				throw new NoSuchElementException();
			i++;
			return null;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#hasPrevious()
		 */
		@Override
		public boolean hasPrevious() {
			return (i > 0);
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#previous()
		 */
		@Override
		public Object previous() {
			if(!hasPrevious())
				throw new NoSuchElementException();
			--i;
			return null;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#nextIndex()
		 */
		@Override
		public int nextIndex() {
			return i;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#previousIndex()
		 */
		@Override
		public int previousIndex() {
			return i-1;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#set(java.lang.Object)
		 */
		@Override
		public void set(Object e) {
			throw new UnsupportedOperationException();
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.ListIterator#add(java.lang.Object)
		 */
		@Override
		public void add(Object e) {
			throw new UnsupportedOperationException();
		}
	}
	
	public NullList(int size) {
		this.size = size;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	public Object get(int index) {
		if(index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return size;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#iterator()
	 */
	@Override
	public java.util.Iterator<Object> iterator() {
		return new Iterator(size);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#listIterator()
	 */
	@Override
	public ListIterator<Object> listIterator() {
		return new Iterator(size);
	}

}
