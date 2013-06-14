/* Copyright (C) 2013-2014 TU Dortmund
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


/**
 * {@link Alphabet} class that supports adding new symbols.
 * 
 * @param <I> symbol class.
 * 
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public interface GrowingAlphabet<I> extends Alphabet<I> {

    /**
     * Adds a new symbol to the alphabet. Some alphabets may prevent
     * symbols from being added twice. In this case, the original alphabet
     * remains unchanged, but this is not considered an error.
     *
     * @return the index of the symbol in the alphabet, after adding it.
     */
    public abstract int addSymbol(@Nullable I a);
    
}
