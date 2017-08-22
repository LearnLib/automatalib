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
package net.automatalib.commons.util;

import java.io.IOException;

import net.automatalib.commons.util.strings.AbstractPrintable;
import net.automatalib.commons.util.strings.StringUtil;

@Deprecated
public class Triple<T1, T2, T3> extends AbstractPrintable {
	
	protected T1 first;
	protected T2 second;
	protected T3 third;
	
	public Triple() {
		
	}
	
	public Triple(T1 first, T2 second, T3 third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	
	
	
	public T1 getFirst() {
		return first;
	}

	public void setFirst(T1 first) {
		this.first = first;
	}

	public T2 getSecond() {
		return second;
	}

	public void setSecond(T2 second) {
		this.second = second;
	}

	public T3 getThird() {
		return third;
	}

	public void setThird(T3 third) {
		this.third = third;
	}

	@Override
	public void print(Appendable a) throws IOException {
		StringUtil.appendObject(a, first);
		a.append(", ");
		StringUtil.appendObject(a, second);
		a.append(", ");
		StringUtil.appendObject(a, third);
	}
	
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Triple<?,?,?> other = (Triple<?,?,?>) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		return true;
	}

	public Pair<T1,Pair<T2,T3>> asPair1() {
		return Pair.make(first, Pair.make(second, third));
	}
	
	public Pair<Pair<T1,T2>,T3> asPair2() {
		return Pair.make(Pair.make(first, second), third);
	}
	
	
	
	public static <T1,T2,T3> Triple<T1,T2,T3> make(T1 first, T2 second, T3 third) {
		return new Triple<>(first, second, third);
	}
	
	public static <T1,T2,T3> Triple<T1,T2,T3> fromPair1(Pair<T1,Pair<T2,T3>> pair) {
		T1 first = null;
		Pair<T2,T3> sndPair = null;
		if(pair != null) {
			first = pair.getFirst();
			sndPair = pair.getSecond();
		}
		T2 second = null;
		T3 third = null;
		if(sndPair != null) {
			second = sndPair.getFirst();
			third = sndPair.getSecond();
		}
		
		return make(first, second, third);
	}
	
	public static <T1,T2,T3> Triple<T1,T2,T3> fromPair2(Pair<Pair<T1,T2>,T3> pair) {
		Pair<T1,T2> fstPair = null;
		T3 third = null;
		if(pair != null) {
			fstPair = pair.getFirst();
			third = pair.getSecond();
		}
		T1 first = null;
		T2 second = null;
		if(fstPair != null) {
			first = fstPair.getFirst();
			second = fstPair.getSecond();
		}
		
		return make(first, second, third);
	}
}
