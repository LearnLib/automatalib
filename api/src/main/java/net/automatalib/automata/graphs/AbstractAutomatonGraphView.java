/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.automata.graphs;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automata.Automaton;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.NodeIDs;

public abstract class AbstractAutomatonGraphView<S,A extends Automaton<S,?,?>,E> implements Graph<S,E> {

	protected final A automaton;
	
	public AbstractAutomatonGraphView(A automaton) {
		this.automaton = automaton;
	}

	@Override
	public Iterator<S> iterator() {
		return automaton.iterator();
	}

	@Override
	public Collection<? extends S> getNodes() {
		return automaton.getStates();
	}
	

	@Override
	public <V> MutableMapping<S, V> createStaticNodeMapping() {
		return automaton.createStaticStateMapping();
	}

	@Override
	public <V> MutableMapping<S, V> createDynamicNodeMapping() {
		return automaton.createDynamicStateMapping();
	}
	

	@Override
	public NodeIDs<S> nodeIDs() {
		return new StateAsNodeIDs<>(automaton.stateIDs());
	}
	
	@Override
	public int size() {
		return automaton.size();
	}

}
