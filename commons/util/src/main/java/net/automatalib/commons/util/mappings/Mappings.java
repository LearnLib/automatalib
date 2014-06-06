/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.commons.util.mappings;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Collection of various methods dealing with {@link Mapping}s.
 * 
 * @author Malte Isberner
 *
 */
public abstract class Mappings {
	
	private static final Mapping<?,?> NULL_MAPPING = new Mapping<Object,Object>() {
		@Override
		public Object get(Object elem) {
			return null;
		}
	};
	
	private static final Mapping<?,?> IDENTITY_MAPPING = new Mapping<Object,Object>() {
		@Override
		public Object get(Object elem) {
			return elem;
		}
	};
	
	private static final Mapping<?,String> TOSTRING_MAPPING = new Mapping<Object,String>() {
		@Override
		public String get(Object elem) {
			return String.valueOf(elem);
		}
	};
	
	
	/**
	 * Retrieves the <code>null</code> mapping, which maps each domain value
	 * to <code>null</code>.
	 * @param <D> domain class.
	 * @param <R> range class.
	 * @return the <code>null</code> mapping.
	 */
	@SuppressWarnings("unchecked")
	public static <D,R> Mapping<D,R> nullMapping() {
		return (Mapping<D,R>)NULL_MAPPING;
	}
	
	/**
	 * Retrieves the identity mapping, which maps each domain value
	 * to itself.
	 * @param <T> domain/range class.
	 * @return the identity mapping.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Mapping<T,T> identity() {
		return (Mapping<T,T>)IDENTITY_MAPPING;
	}
	
	/**
	 * Returns a mapping that maps objects to their {@link String} representation,
	 * as obtained by {@link String#valueOf(Object)}.
	 * @return the "<tt>toString()</tt>" mapping
	 */
	@SuppressWarnings("unchecked")
	public static <D> Mapping<D,String> toStringMapping() {
		return (Mapping<D,String>)TOSTRING_MAPPING;
	}
	
	/**
	 * Returns a mapping that maps objects to a supertype representation.
	 * @return the "upcast" mapping
	 */
	@SuppressWarnings("unchecked")
	public static <S,T extends S> Mapping<T,S> upcast() {
		return (Mapping<T,S>)IDENTITY_MAPPING;
	}
	
	/**
	 * Retrieves the composition of two mappings, i.e., that mapping that
	 * results from applying the {@link Mapping#get(Object)} method
	 * consecutively.
	 * @param <D> domain class of the first (and resulting) mapping.
	 * @param <I> intermediate object class, range class of the first and domain
	 * class of the second mapping.
	 * @param <R> range class of the second (and resulting) mapping.
	 * @param first first mapping.
	 * @param second second mapping.
	 * @return the composed mapping.
	 */
	public static <D,I,R> Mapping<D,R> compose(Mapping<D, ? extends I> first, Mapping<? super I,R> second) {
		return new MappingComposition<D, I, R>(first, second);
	}
	

	/**
	 * Applies a mapping to a collection, resulting in a collection containing
	 * the result of applying the specified mapping to each element in the
	 * collection.
	 * 
	 * Note that more specific properties of the specified collection won't
	 * be preserved: If the given collection is e.g. a set, and the provided
	 * mapping is not bijective, then the resulting collections may contain
	 * some values multiple times.
	 * 
	 * @param <D> domain class.
	 * @param <R> range class.
	 * @param mapping the mapping to apply.
	 * @param coll the collection.
	 * @return the mapped collection.
	 */
	public static <D,R> Collection<R> apply(final Mapping<? super D,R> mapping, final Collection<? extends D> coll) {
		return new AbstractCollection<R>() {
			@Override
			public Iterator<R> iterator() {
				return apply(mapping, coll.iterator());
			}

			@Override
			public int size() {
				return coll.size();
			}
		};
	}
	
	/**
	 * Applies a mapping to a list, resulting in a list containing the result
	 * of applying the specified mapping to each element in the list.
	 * 
	 * @param mapping the mapping to apply.
	 * @param list the list.
	 * @return the mapped list.
	 */
	public static <D,R> List<R> apply(final Mapping<? super D,R> mapping, final List<? extends D> list) {
		return new AbstractList<R>() {
			/*
			 * (non-Javadoc)
			 * @see java.util.AbstractList#get(int)
			 */
			@Override
			public R get(int index) {
				return mapping.get(list.get(index));
			}

			/*
			 * (non-Javadoc)
			 * @see java.util.AbstractCollection#size()
			 */
			@Override
			public int size() {
				return list.size();
			}			
		};
	}
	
	/**
	 * Applies a mapping to an iterable. The result is an iterable whose
	 * iterator returns the results of applying the specified mapping
	 * to each of the elements returned by the original iterable.
	 * @param <D> domain class.
	 * @param <R> range clas.
	 * @param mapping the mapping to apply.
	 * @param it the underlying iterable.
	 * @return the mapped iterable.
	 */
	public static <D,R> Iterable<R> apply(final Mapping<? super D,R> mapping, final Iterable<? extends D> it) {
		return new Iterable<R>() {
			@Override
			public Iterator<R> iterator() {
				return apply(mapping, it.iterator());
			}
		};
	}
	
	/**
	 * Applies a mapping to an iterator. For the behavior, see
	 * {@link #apply(Mapping, Iterable)}. The resulting iterator supports
	 * each operation which the underlying supports.
	 * 
	 * @param <D> domain class.
	 * @param <R> range class.
	 * @param mapping the mapping to apply.
	 * @param baseIt the underlying iterator.
	 * @return the mapped iterator.
	 */
	public static <D,R> Iterator<R> apply(Mapping<? super D,R> mapping, Iterator<? extends D> baseIt) {
		return new MappedIterator<D, R>(mapping, baseIt);
	}
	
	
	/**
	 * Safely retrieves a value from a mapping. If the mapping is
	 * <code>null</code> or returns a <code>null</code> value, the given
	 * fallback value is returned.  
	 * @param mapping the mapping.
	 * @param key the key.
	 * @param fallback the fallback value to return if either the mapping or the
	 * originally returned value are <code>null</code>.
	 * @return the value returned by the specified mapping, or the fallback value.
	 */
	public static <D,R> R safeGet(Mapping<? super D,R> mapping, D key, R fallback) {
		if(mapping == null)
			return fallback;
		R val = mapping.get(key);
		if(val == null)
			return fallback;
		return val;
	}
	
	public static <D> D idGet(Mapping<D,D> mapping, D key) {
		return safeGet(mapping, key, key);
	}
	
	public static <D,R> R nullGet(Mapping<? super D,? extends R> mapping, D key) {
		return safeGet(mapping, key, null);
	}
	
	public static <D,R> Mapping<D,R> fromMap(Map<D,R> map) {
		return new MapMapping<D,R>(map);
	}

	
	
	/*
	 * Prevent inheritance.
	 */
	private Mappings() {
	}
}
