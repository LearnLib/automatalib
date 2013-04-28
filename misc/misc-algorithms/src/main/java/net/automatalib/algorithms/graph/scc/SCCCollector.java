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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.algorithms.graph.scc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SCCCollector<N> implements SCCListener<N> {
	
	private final List<List<N>> sccList = new ArrayList<>();

	
	@Override
	public void foundSCC(Collection<? extends N> scc) {
		sccList.add(new ArrayList<>(scc));
	}
	
	public List<List<N>> getSCCList() {
		return sccList;
	}

}
