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
package net.automatalib.automata.transout.probabilistic;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class ProbabilisticOutput<O> {
	
	private final float probability;
	@Nullable
	private final O output;

	public ProbabilisticOutput(float probability, @Nullable O output) {
		this.probability = probability;
		this.output = output;
	}
	
	public float getProbability() {
		return probability;
	}
	
	@Nullable
	public O getOutput() {
		return output;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(output);
		result = prime * result + Float.floatToIntBits(probability);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj.getClass() != ProbabilisticOutput.class)
			return false;
		ProbabilisticOutput<?> other = (ProbabilisticOutput<?>)obj;
		if(!Objects.equals(output, other.output))
			return false;
		return (probability == other.probability);
	}
	
	

}
