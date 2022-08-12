package net.automatalib.counterExamples.CounterExampleSolver.Visualisation;

import net.automatalib.graphs.base.compact.CompactBidiEdge;
import net.automatalib.visualization.DefaultVisualizationHelper;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractVisualizationHelper extends DefaultVisualizationHelper<Integer, CompactBidiEdge<String>> {
    MagicTree resultTree;
    int maxRecDepth;

    public AbstractVisualizationHelper(MagicTree resultTree){
        this.resultTree = resultTree;
        maxRecDepth = resultTree.calcMaxRecDepth();
    }

    @Override
    public boolean getNodeProperties(Integer node, Map<String, String> properties) {
        super.getNodeProperties(node, properties);
        properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
        properties.put(NodeAttrs.LABEL, resultTree.getNodeProperty(node).printSearchStateWrapper() );
        return true;
    }

    @Override
    public boolean getEdgeProperties(Integer src, CompactBidiEdge<String> edge, Integer tgt, Map<String, String> properties) {
        super.getEdgeProperties(src, edge, tgt, properties);
        properties.put(EdgeAttrs.ARROWHEAD, "none");
        properties.put(EdgeAttrs.LABEL, Objects.toString(edge.getProperty()));
        return true;
    }
}
