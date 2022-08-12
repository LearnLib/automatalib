package net.automatalib.counterExamples.SuperSolver.Wrapper;

import net.automatalib.graphs.base.compact.CompactPMPG;
import net.automatalib.graphs.base.compact.CompactPMPGEdge;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgePropertyImpl;

import java.util.Iterator;

public class GraphTraversalWrapper<N, L, E, AP> {

    public final CompactPMPG<Character, Void> pmpg;

    public GraphTraversalWrapper(CompactPMPG pmpgOutside){
        pmpg = filterProceduralEdges(pmpgOutside);
    }

    private CompactPMPG filterProceduralEdges(CompactPMPG pmpg){
        Iterator nodeIterator = pmpg.getNodes().iterator();
        CompactPMPG<Character, Void> pmpgTemp = new CompactPMPG<>('µ');
        while(nodeIterator.hasNext()){
            //add each Node
            Integer n = pmpgTemp.addNode();
            //and for each Node, add just the INTERNAL edges
            Iterator edgeIterator = pmpg.getOutgoingEdges(n).iterator();
            while(edgeIterator.hasNext()){
                CompactPMPGEdge edge = (CompactPMPGEdge) edgeIterator.next();
                int target = edge.getTarget();
                if( Character.isLowerCase((Character)edge.getLabel()) ){
                    pmpgTemp.setEdgeLabel(pmpgTemp.connect((Integer)n, (Integer)target), (Character) edge.getLabel().toString().charAt(0));
                }
            }
            nodeIterator.next();
        }

        return pmpgTemp;
    }


}
