package net.automatalib.counterExamples.SuperSolver;

import java.lang.reflect.Array;
import java.util.*;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import net.automatalib.counterExamples.SuperSolver.Wrapper.SearchStateNode;
import net.automatalib.graphs.base.compact.*;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MagicTree extends AbstractCompactBidiGraph<SearchStateNode, String> {

    public String defaultLabel;
    public final ResizingArrayStorage<@Nullable SearchStateNode> nodeProperties;
    public int finishingNode;
    public String resultString;

    //benchmark stuff
    public int nodesOnPath;
    public long extractionTime;
    public int maxDepth;

    MagicTree(String label){
        this.defaultLabel = label;
        this.nodeProperties = new ResizingArrayStorage(SearchStateNode.class);
        finishingNode = -1;
        resultString = "";

        nodesOnPath = 1;
        extractionTime = 0;
        maxDepth = 1;
    }

    public void extractPath(){
        int currentNode = this.getNode(this.finishingNode);

        ArrayList<String> tmpPath = new ArrayList<>();

        long startTime = System.nanoTime();
        while( currentNode >= 0){
            nodeProperties.array[currentNode].isPartOfResult = true;
            tmpPath.add(nodeProperties.array[currentNode].edgeLabel);
            currentNode = nodeProperties.array[currentNode].parentNode;
            if(currentNode == 0){
                nodeProperties.array[currentNode].isPartOfResult = true;
                break;
            }
            nodesOnPath++;
        }

        tmpPath.add(nodeProperties.array[currentNode].edgeLabel);

        Collections.reverse(tmpPath);

        ArrayList<Character> resultPath = new ArrayList<>();

        for (String s: tmpPath) {
            if(s.length() == 1 && Character.isLowerCase(s.charAt(0)) ){
                resultPath.add(s.charAt(0));
            }
        }

        for (Character s: resultPath) {
            resultString += s;
        }
        long endTime = System.nanoTime();
        extractionTime = endTime - startTime;
    }

    public void calcMaxDepthInit(){
        int root = 0;
        maxDepth = calcMaxDepth(root);
    }
    public int calcMaxDepth(Integer node){
        if(node < 0 || node == null){
            return 0;
        }
        ArrayList<Integer> childrensHeights = new ArrayList<>();
        for (int child: this.getAdjacentTargets(node)) {
            childrensHeights.add( calcMaxDepth(child) );
        }
        if(childrensHeights.isEmpty()){
            return 1;
        }
        return Collections.max(childrensHeights)+1;
    }

    public int calcMaxRecDepth(){
        int result = 0;
        for(SearchStateNode s: nodeProperties.array){
            if(s!=null && s.expansionDepth > result){
                result = s.expansionDepth;
            }
        }
        return result;
    }

    @Override
    public void setNodeProperty(int node, SearchStateNode property) {
        nodeProperties.ensureCapacity(node + 1);
        nodeProperties.array[node] = property;
    }

    @Override
    public SearchStateNode getNodeProperty(int node) {
        if (node > nodeProperties.array.length) {
            return null;
        }

        final SearchStateNode props = nodeProperties.array[node];
        return props == null ? null : props;
    }
}
