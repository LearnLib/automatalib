package net.automatalib.counterExamples.CounterExampleSolver.Visualisation;

import java.util.Map;

public class ColorVisualizationHelper extends AbstractVisualizationHelper{

    private boolean colorResultPath;
    private boolean colorRecDepth;
    public ColorVisualizationHelper(MagicTree resultTree, boolean colorResultPath, boolean colorRecDepth){
        super(resultTree);
        this.colorResultPath = colorResultPath;
        this.colorRecDepth = colorRecDepth;
    }

    @Override
    public boolean getNodeProperties(Integer node, Map<String, String> properties) {
        if(colorResultPath){
            colorPath(node, properties);
        }
        if (colorRecDepth) {
            colorRecDepth(node,properties);
        }
        return true;
    }

    private void colorRecDepth(Integer node, Map<String, String> properties){
        super.getNodeProperties(node, properties);

        double nodeDepth = resultTree.nodeProperties.array[node].expansionDepth;

        int hexColor = (int)Math.floor((nodeDepth/ maxRecDepth)*255);
        properties.put(NodeAttrs.COLOR, "#" + Integer.toHexString(hexColor) + "0000");
    }

    private void colorPath(Integer node, Map<String, String> properties){
        super.getNodeProperties(node, properties);

        if( resultTree.nodeProperties.array[node].isPartOfResult ){
            properties.put(NodeAttrs.COLOR, "green");
        } else {
            properties.put(NodeAttrs.COLOR, "red");
        }
    }


}
