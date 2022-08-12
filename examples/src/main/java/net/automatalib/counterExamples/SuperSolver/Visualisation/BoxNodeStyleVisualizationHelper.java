package net.automatalib.counterExamples.SuperSolver.Visualisation;

import net.automatalib.counterExamples.SuperSolver.MagicTree;

import java.util.Map;

public class BoxNodeStyleVisualizationHelper extends AbstractVisualizationHelper {
    private boolean shapeResultPath;
    private boolean thickBordersDepth;

    public BoxNodeStyleVisualizationHelper(MagicTree resultTree, boolean shapeResultPath, boolean thickBordersDepth){
        super(resultTree);
        this.shapeResultPath = shapeResultPath;
        this.thickBordersDepth = thickBordersDepth;
    }

    @Override
    public boolean getNodeProperties(Integer node, Map<String, String> properties) {
        if(shapeResultPath){
            shapeResultPath(node, properties);
        }
        if(thickBordersDepth){
            thickBordersDepth(node, properties);
        }
        return true;
    }

    private void thickBordersDepth(Integer node, Map<String, String> properties){
        super.getNodeProperties(node, properties);
        if(resultTree.getNodeProperty(node).isPartOfResult){
            properties.put(NodeAttrs.STYLE, "bold");
        }
    }

    private void shapeResultPath(Integer node, Map<String, String> properties){
        super.getNodeProperties(node, properties);
        if(resultTree.getNodeProperty(node).isPartOfResult){
            properties.put(NodeAttrs.STYLE, NodeStyles.DASHED);
        }

    }

}
