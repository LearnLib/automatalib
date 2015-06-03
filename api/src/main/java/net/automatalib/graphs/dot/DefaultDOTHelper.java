/* Copyright (C) 2013-2014 TU Dortmund
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
package net.automatalib.graphs.dot;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.automatalib.commons.util.mappings.Mapping;

public class DefaultDOTHelper<N, E> extends EmptyDOTHelper<N, E> {
		
	protected static final String START_PREFIX = "__start";
	
	private final GraphDOTHelper<N, ? super E> delegate;
	
	public DefaultDOTHelper() {
		this(null);
	}
	
	public DefaultDOTHelper(GraphDOTHelper<N,? super E> delegate) {
		this.delegate = delegate;
	}
	
	protected Collection<? extends N> initialNodes() {
		return Collections.emptySet();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.GraphDOTHelper#writePreamble(java.lang.Appendable)
	 */
	@Override
	public void writePreamble(Appendable a) throws IOException {
		if(delegate != null) {
			delegate.writePreamble(a);
		}
		
		int size = initialNodes().size();
		
		for(int i = 0; i < size; i++) {
			a.append(START_PREFIX).append(Integer.toString(i));
			a.append(" [label=\"\" shape=\"none\"];\n");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.GraphDOTHelper#writePostamble(java.lang.Appendable)
	 */
	@Override
	public void writePostamble(Mapping<N,String> identifiers, Appendable a) throws IOException {
		if(delegate != null) {
			delegate.writePostamble(identifiers, a);
		}
		
		Collection<? extends N> initials = initialNodes();
		
		int i = 0;
		for(N init : initials) {
			a.append(START_PREFIX).append(Integer.toString(i++));
			a.append(" -> ").append(identifiers.get(init)).append(";\n");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.GraphDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getNodeProperties(N node, Map<String,String> properties) {
		if(delegate != null) {
			if(!delegate.getNodeProperties(node, properties)) {
				return false;
			}
		}
		
		if(!properties.containsKey(NodeAttrs.LABEL)) {
			String label = String.valueOf(node);
			properties.put(NodeAttrs.LABEL, label);
		}
		if(!properties.containsKey(NodeAttrs.SHAPE)) {
			properties.put(NodeAttrs.SHAPE, NodeShapes.CIRCLE);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.GraphDOTHelper#getEdgeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getEdgeProperties(N src, E edge, N tgt, Map<String,String> properties) {
		if(delegate != null) {
			return delegate.getEdgeProperties(src, edge, tgt, properties);
		}
		return true;
	}
	

}
