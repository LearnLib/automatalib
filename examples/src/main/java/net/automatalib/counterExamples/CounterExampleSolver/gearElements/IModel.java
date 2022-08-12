package net.automatalib.counterExamples.CounterExampleSolver.gearElements;

import java.util.Set;

/**
 * This interface describes a model that can be used for model checking.
 *
 * @author Clemens Renner (clemens.renner@uni-dortmund.de)
 * @version $Revision: 1.3 $ $Date: 2008-03-20 11:29:28 $
 */
public interface IModel<N, E> {
    /**
     * Two types of edges are supported. MUST-edges refer to mandatory behavior
     * of the model while MAY-edges refer to optional behavior.
     */
    public enum EdgeType {MAY, MUST}

    /**
     * Returns a unique identifier for a given node.
     * @see #getNodes()
     */
    public String getIdentifier(N node);

    /**
     * Returns a set of nodes the model consists of.
     */
    public Set<N> getNodes();

    /**
     * Returns the outgoing edges at a certain node in the model.
     * @see #getNodes()
     */
    public Set<E> getOutgoingEdges(N node);

    /**
     * Returns the incoming edges at a certain node in the model.
     * @see #getNodes()
     */
    public Set<E> getIncomingEdges(N node);

    /**
     * Returns the set of edges in the model.
     */
    public Set<E> getEdges();

    /**
     * Returns whether this edge is mandatory ({@link EdgeType#MUST}) or
     * optional ({@link EdgeType#MAY}) -- a concept derived from Larsen's
     * Modal Transition Systems.
     */
    public EdgeType getEdgeType(E edge);

    /**
     * Returns the source node for the given (directed) edge.
     * @see #getEdges()
     */
    public N getSource(E edge);

    /**
     * Returns the target node for the given (directed) edge.
     * @see #getEdges()
     */
    public N getTarget(E edge);

    /**
     * Returns the labels for the given edge in the model.
     * @see #getEdges()
     */
    public Set<String> getEdgeLabels(E edge);

    /**
     * Returns the set of atomic propositions holding at the given node.
     * @see #getNodes()
     */
    public Set<Integer> getAtomicPropositions(N node);

    /**
     * Returns the initial nodes of the model.
     */
    public Set<N> getInitialNodes();
}