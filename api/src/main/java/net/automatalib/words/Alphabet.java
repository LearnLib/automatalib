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
package net.automatalib.words;

import java.util.Collection;
import java.util.Comparator;

import net.automatalib.commons.util.array.ArrayWritable;

/**
 * Class implementing an (indexed) alphabet. An alphabet is a collection of symbols, where
 * each symbol has a (unique) index. Apart from serving as a collection, this class also provides
 * a one-to-one mapping between symbols and indices.
 * 
 * @param <I> symbol class.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 */
public interface Alphabet<I> extends ArrayWritable<I>, Collection<I>, Comparator<I> {

    /**
     * Returns the symbol with the given index in this alphabet.
     * @param index the index of the requested symbol.
     * @return symbol with the given index. 
     * @throws IllegalArgumentException if there is no symbol with this index.
     */
    public abstract I getSymbol(int index) throws IllegalArgumentException;
    
    /**
     * Returns the index of the given symbol in the alphabet. 
     * @param symbol
     * @return
     * @throws IllegalArgumentException if the provided symbol does not belong to the alphabet.
     */
    public abstract int getSymbolIndex(I symbol) throws IllegalArgumentException;
}
