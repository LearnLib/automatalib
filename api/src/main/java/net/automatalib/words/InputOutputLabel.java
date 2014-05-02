/* Copyright (C) 2014 AutomataLib
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

/**
 * Interface for labels distinguishable as inputs or outputs.
 * 
 * @author Michele Volpato
 *
 * @param <L> label class
 */
public interface InputOutputLabel {
	
    /**
     * Returns true if the label is an input.
     */
    public abstract boolean isInput();
    
    /**
     * Returns true if the label is an output.
     */
    public abstract boolean isOutput();
    
    /**
     * Return the label as input.
     */
    public abstract Object getLabel();
    
    /**
     * Return a string representation of the label label as input.
     */
    public abstract String toString();


}
