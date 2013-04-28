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
package net.automatalib.util.graphs.dot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.dot.DefaultDOTHelperAutomaton;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.commons.util.strings.StringUtil;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.UndirectedGraph;
import net.automatalib.graphs.dot.AggregateDOTHelper;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.util.automata.Automata;


/**
 * Methods for rendering a {@link Graph} or {@link Automaton} in the GraphVIZ DOT format.
 * <p>
 * This class does not take care of actually processing the generated DOT data. For this,
 * please take a look at the <tt>automata-commons-dotutil</tt> artifact.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public abstract class GraphDOT {
	
	/**
	 * Renders a {@link DOTPlottableGraph} in the GraphVIZ DOT format. 
	 * @param graph the graph to render
	 * @param a the appendable to write to.
	 * @throws IOException if writing to <tt>a</tt> fails. 
	 */
	@SafeVarargs
	public static <N,E> void write(DOTPlottableGraph<N, E> graph,
			Appendable a, GraphDOTHelper<N,? super E> ...additionalHelpers) throws IOException {
		GraphDOTHelper<N,? super E> helper = graph.getGraphDOTHelper();
		write(graph, helper, a, additionalHelpers);
	}
	
	/**
	 * Renders an {@link Automaton} in the GraphVIZ DOT format.
	 * 
	 * @param automaton the automaton to render.
	 * @param helper the helper to use for rendering
	 * @param inputAlphabet the input alphabet to consider
	 * @param a the appendable to write to.
	 * @throws IOException if writing to <tt>a</tt> fails.
	 */
	@SafeVarargs
	public static <S,I,T> void write(Automaton<S,I,T> automaton,
			GraphDOTHelper<S, ? super Pair<I,T>> helper,
			Collection<? extends I> inputAlphabet,
			Appendable a, GraphDOTHelper<S,? super Pair<I,T>> ...additionalHelpers) throws IOException {
		Graph<S,Pair<I,T>> ag = Automata.asGraph(automaton, inputAlphabet);
		write(ag, helper, a, additionalHelpers);
	}
	
	/**
	 * Renders an {@link Automaton} in the GraphVIZ DOT format.
	 * 
	 * @param automaton the automaton to render.
	 * @param inputAlphabet the input alphabet to consider
	 * @param a the appendable to write to
	 * @throws IOException if writing to <tt>a</tt> fails
	 */
	@SafeVarargs
	public static <S,I,T> void write(Automaton<S,I,T> automaton,
			Collection<? extends I> inputAlphabet,
			Appendable a, GraphDOTHelper<S,? super Pair<I,T>> ...additionalHelpers) throws IOException {
		GraphDOTHelper<S,? super Pair<I,T>> helper;
		if(automaton instanceof DOTPlottableAutomaton) {
			DOTPlottableAutomaton<S,I,T> dp = (DOTPlottableAutomaton<S,I,T>)automaton;
			helper = dp.getDOTHelper();
		}
		else
			helper = new DefaultDOTHelperAutomaton<S, I, T, Automaton<S,I,T>>(automaton);
		
		write(automaton, helper, inputAlphabet, a, additionalHelpers);
	}
	
	
	/**
	 * Renders a {@link Graph} in the GraphVIZ DOT format.
	 * 
	 * @param graph the graph to render
	 * @param a the appendable to write to
	 * @throws IOException if writing to <tt>a</tt> fails
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <N,E> void write(Graph<N,E> graph,
			Appendable a, GraphDOTHelper<N,? super E> ...additionalHelpers) throws IOException {
		GraphDOTHelper<N, ? super E> helper = null;
		if(graph instanceof DOTPlottableGraph) {
			DOTPlottableGraph<N,E> plottable = (DOTPlottableGraph<N,E>)graph;
			helper = plottable.getGraphDOTHelper();
		}
		else
			helper = (GraphDOTHelper<N,? super E>)DefaultDOTHelper.getInstance();
		write(graph, helper, a, additionalHelpers);
	}
	
	@SafeVarargs
	public static <N,E> void write(Graph<N,E> graph, GraphDOTHelper<N, ? super E> helper, Appendable a, GraphDOTHelper<N, ? super E> ...additionalHelpers) throws IOException {
		List<GraphDOTHelper<N,? super E>> helpers = new ArrayList<>(additionalHelpers.length + 1);
		helpers.add(helper);
		helpers.addAll(Arrays.asList(additionalHelpers));
		
		write(graph, a, helpers);
	}
	
	public static <N,E> void write(Graph<N,E> graph, Appendable a, List<GraphDOTHelper<N,? super E>> helpers) throws IOException {
		AggregateDOTHelper<N, E> aggHelper = new AggregateDOTHelper<>(helpers);
		write(graph, aggHelper, a);
	}
	
	/**
	 * Renders a {@link Graph} in the GraphVIZ DOT format.
	 * 
	 * @param graph the graph to render
	 * @param dotHelper the helper to use for rendering
	 * @param a the appendable to write to
	 * @throws IOException if writing to <tt>a</tt> fails
	 */
	public static <N,E> void write(Graph<N, E> graph,
			GraphDOTHelper<N,? super E> dotHelper,
			Appendable a) throws IOException {
		
		if(dotHelper == null)
			dotHelper = new DefaultDOTHelper<N, E>();
		
		boolean directed = true;
		if(graph instanceof UndirectedGraph)
			directed = false;
		
		if(directed)
			a.append("di");
		a.append("graph g {\n");
		
		dotHelper.writePreamble(a);
		a.append('\n');
		
		Map<String,String> props = new HashMap<>();
		
		MutableMapping<N,String> nodeNames = graph.createStaticNodeMapping();
		
		int i = 0;
		
		for(N node : graph) {
			props.clear();
			if(!dotHelper.getNodeProperties(node, props))
				continue;
			String id = "s" + i++;
			a.append('\t').append(id);
			appendParams(props, a);
			a.append(";\n");
			nodeNames.put(node, id);
		}
		
		for(N node : graph) {
			String srcId = nodeNames.get(node);
			if(srcId == null)
				continue;
			Collection<E> outEdges = graph.getOutgoingEdges(node);
			if(outEdges == null || outEdges.isEmpty())
				continue;
			for(E e : outEdges) {
				N tgt = graph.getTarget(e);
				String tgtId = nodeNames.get(tgt);
				if(tgtId == null)
					continue;
				
				if(!directed && tgtId.compareTo(srcId) < 0)
					continue;
				
				props.clear();
				if(!dotHelper.getEdgeProperties(e, props))
					continue;
				
				a.append('\t').append(srcId).append(' ');
				if(directed)
					a.append("-> ");
				else
					a.append("-- ");
				a.append(tgtId);
				appendParams(props, a);
				a.append(";\n");
			}
		}
		
		a.append('\n');
		dotHelper.writePostamble(nodeNames, a);
		a.append("}\n");
	}
	
	public static <N,E> void writeToFile(Graph<N,E> graph,
			GraphDOTHelper<N,E> dotHelper,
			File file) throws IOException {
		
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			write(graph, dotHelper, writer);
		}
	}
	
	
	private static void appendParams(Map<String,String> params, Appendable a)
			throws IOException {
		if(params == null || params.isEmpty())
			return;
		a.append(" [");
		boolean first = true;
		for(Map.Entry<String,String> e : params.entrySet()) {
			if(first)
				first = false;
			else
				a.append(' ');
			a.append(e.getKey()).append("=");
			StringUtil.enquote(e.getValue(), a);
		}
		a.append(']');
	}
}
