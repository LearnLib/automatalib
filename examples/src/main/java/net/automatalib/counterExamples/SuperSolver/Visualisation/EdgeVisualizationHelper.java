package net.automatalib.counterExamples.SuperSolver.Visualisation;

import net.automatalib.counterExamples.SuperSolver.MagicTree;
import net.automatalib.graphs.base.compact.CompactBidiEdge;

import java.util.Map;
import java.util.Objects;

public class EdgeVisualizationHelper extends AbstractVisualizationHelper{
    private boolean colorResultPath;

    public EdgeVisualizationHelper(MagicTree resultTree, boolean colorResultPath){
        super(resultTree);
        this.colorResultPath = colorResultPath;
    }

    @Override
    public boolean getEdgeProperties(Integer src, CompactBidiEdge<String> edge, Integer tgt, Map<String, String> properties) {
        if(colorResultPath){
            colorResultPath(src, edge, tgt, properties);
        }
        return true;
    }

    private void colorResultPath(Integer src, CompactBidiEdge<String> edge, Integer tgt, Map<String, String> properties){
        if(resultTree.getNodeProperty(edge.getTarget()).isPartOfResult){
            super.getEdgeProperties(src, edge, tgt, properties);
            properties.put(EdgeAttrs.STYLE, EdgeStyles.BOLD);
            properties.put(EdgeAttrs.LABEL, Objects.toString(edge.getProperty()));
        }
    }
}
