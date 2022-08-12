package net.automatalib.counterExamples.CounterExampleSolver.GraphGenerator;

import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.graphs.base.DefaultCFMPS;
import net.automatalib.graphs.base.compact.CompactPMPG;
import net.automatalib.graphs.base.compact.CompactPMPGEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class GraphGenerator {

    static Logger logger = LoggerFactory.getLogger(GraphGenerator.class);
    public ArrayList<DefaultCFMPS<String, Void>> cfmpsList;

    //benchmark stuff
    public ArrayList<Long> constructionTimes = new ArrayList<Long>();

    public GraphGenerator(boolean test) throws IOException {

        cfmpsList = new ArrayList<>();

        // "D:\Uni\Master\Master Arbeit\Code\automatalib\examples\src\main\java\net\automatalib\counterExamples\SuperSolver\GraphGenerator\GraphData"
        //C:\Master Thesis Freese\Solver\examples\src\main\java\net\automatalib\counterExamples\SuperSolver\GraphGenerator\GraphData
        String pathname = "D:\\Uni\\Master\\Master Arbeit\\Code\\Solver\\examples\\src\\main\\java\\net\\automatalib\\counterExamples\\SuperSolver\\GraphGenerator\\GraphData";
        if(test){
            pathname += "\\tests";
        }
        File folder = new File(pathname);

        for (File f: folder.listFiles()) {
            if(!f.isDirectory()) {
                FileInputStream seed = new FileInputStream(folder.toPath() + "\\" + f.getName());
                try {
                    long startTime = System.nanoTime();
                    ContextFreeModalProcessSystem<String, Void> cfmps = ExternalSystemDeserializer.parse(seed);

                    cfmpsList.add( (DefaultCFMPS<String, Void>) cfmps);
                    long endTime = System.nanoTime();

                    constructionTimes.add(endTime - startTime);
                } catch (Exception e) {
                    logger.info("exception while adding");
                }
            }
        }
        logger.debug("added " + cfmpsList.size() + " objects to list");
    }

    /*private Map<String, CompactPMPG<Character, Void>> getCharPMPGMap(Map<String, Object> pmpgMap, char defaultLabel){

    }

    //does not generate a new final node
    public void blowUp(int graphToBlowUp, int nodesToAdd, int edgesToAdd) {
        CompactPMPG exploded = pmpgList.get(graphToBlowUp).get(0);

        ArrayList symbols = new ArrayList();        //get current symbol list for edges
        Iterator nodeIter = exploded.getNodes().iterator();
        while(nodeIter.hasNext()) {
            Iterator edges = exploded.getOutgoingEdges((int)nodeIter.next()).iterator();
            while(edges.hasNext()){
                Character label = (Character)((CompactPMPGEdge) (edges.next())).getLabel();
                if(!symbols.contains(label)){
                    symbols.add(label);
                }
            }
        }


        for(int i=0; i<nodesToAdd; i++){ //add |nodesToAdd| nodes to the graph
            exploded.addNode();
        }

        int initialNode = exploded.getInitialNode();
        int finalNode = exploded.getFinalNode();

        //add random edges to the graph
        Random random = new Random();
        for(int i=0; i<edgesToAdd; i++){ //add |nodesToAdd| nodes to the graph
            int bound = exploded.getNodes().size();
            int sourceNode = random.nextInt(bound);
            while(sourceNode == finalNode) {        //the final node is always a final node
                sourceNode = random.nextInt(bound);
            }
            int targetNode = random.nextInt(bound);
            int edgeSymbol = random.nextInt(symbols.size());

            while( Character.isUpperCase( (char)symbols.get(edgeSymbol).toString().charAt(0))) { // do NOT add proceduraledges just yet
                edgeSymbol = random.nextInt(symbols.size());
            }

            CompactPMPGEdge edge = (CompactPMPGEdge) exploded.connect(sourceNode, targetNode);
            ProceduralModalEdgePropertyImpl property = new ProceduralModalEdgePropertyImpl(ProceduralModalEdgePropertyImpl.ProceduralType.INTERNAL, ModalEdgeProperty.ModalType.MUST);
            edge.setProperty( property );
            exploded.setEdgeLabel(edge, symbols.get(edgeSymbol));
        }

        pmpgList.get(0).set(graphToBlowUp, exploded);
    }

    public void replaceInternalWithProceduralEdges(int graphToCheck, int proceduralEdgesToAdd, FormulaNode<Character, Void> formula, M3CSolver.TypedM3CSolver<FormulaNode<Character, Void>> solver){
        CompactPMPG exploded = pmpgList.get(graphToCheck).get(0);
        ArrayList symbols = new ArrayList();        //get current symbol list for edges
        ArrayList edges = new ArrayList();
        int edgesFromStart = exploded.getOutgoingEdges(exploded.getInitialNode()).size();
        Iterator nodeIter = exploded.getNodes().iterator();
        while(nodeIter.hasNext()) {
            Iterator edgeIter = exploded.getOutgoingEdges((int)nodeIter.next()).iterator();
            while(edgeIter.hasNext()){
                CompactPMPGEdge edge = (CompactPMPGEdge) (edgeIter.next());
                Character label = (Character) edge.getLabel();

                if(!symbols.contains(label) && Character.isUpperCase(label)){
                    symbols.add(label);
                }

                if(!Character.isUpperCase(label)){
                    edges.add(edge);
                }
            }
        }

        if(edges.size()-edgesFromStart < proceduralEdgesToAdd) {
            System.out.println("too many procedural edges!");
        }

        Random random = new Random();
        int replacedCounter = 0;
        while( proceduralEdgesToAdd > 0 && edges.size() > 0 ) {
            //edge cannot start from initialNode
            if(edges.size() - edgesFromStart <= 0 ) {
                System.out.println("no more edges to replace, aborting");
                break;
            }
            int edgeToChange = random.nextInt(edges.size()-edgesFromStart) + edgesFromStart;

            int proceduralReplace = random.nextInt(symbols.size());
            CompactPMPGEdge tmpEdge = (CompactPMPGEdge) edges.get(edgeToChange);
            ProceduralModalEdgePropertyImpl oldProperty = new ProceduralModalEdgePropertyImpl(ProceduralModalEdgePropertyImpl.ProceduralType.INTERNAL, ModalEdgeProperty.ModalType.MUST);
            Character oldLabel = (Character) tmpEdge.getLabel();

            //get random edge with random new label
            ProceduralModalEdgePropertyImpl newProperty = new ProceduralModalEdgePropertyImpl(ProceduralModalEdgePropertyImpl.ProceduralType.PROCESS, ModalEdgeProperty.ModalType.MUST);
            Character newLabel = (Character) symbols.get(proceduralReplace);
            exploded.setEdgeProperty(tmpEdge, newProperty);
            exploded.setEdgeLabel(tmpEdge, newLabel);

            //initialize new solver and check if valid
            DefaultCFMPS<Character, Void> mcfps = new DefaultCFMPS<>('P',
                    Collections.singletonMap('P', pmpgList.get(graphToCheck).get(0)));
            M3CSolver.TypedM3CSolver<FormulaNode<Character, Void>> m3c =
                    M3CSolvers.typedSolver(mcfps);

            if(m3c.solve(formula)){         //if valid change, keep it and adjust counters
                //System.out.println("valid change detected for: " + tmpEdge.getTarget() + "  with label: " + tmpEdge.getLabel());
                proceduralEdgesToAdd--;
                replacedCounter++;
                edges.remove(edgeToChange);
            } else {                        // if not valid, replace tmpEdge with old values, and remove said edge from possible list
                exploded.setEdgeProperty(tmpEdge, oldProperty);
                exploded.setEdgeLabel(tmpEdge, oldLabel);
                edges.remove(edgeToChange);
            }
        }
        //System.out.println("edges replaced: " + replacedCounter);
    }

    public void printSymbols(int graphToCheck){
        CompactPMPG graph = pmpgList.get(graphToCheck).get(0);

        ArrayList symbols = new ArrayList();        //get current symbol list for edges
        Iterator nodeIter = graph.getNodes().iterator();
        while(nodeIter.hasNext()) {
            Iterator edges = graph.getOutgoingEdges((int)nodeIter.next()).iterator();
            while(edges.hasNext()){
                char label = (char)((CompactPMPGEdge) (edges.next())).getLabel();
                if(!symbols.contains(label)){
                    symbols.add(label);
                }
            }
        }
        System.out.println("Alphabet of edges in graph:");
        for (Object s: symbols) {
            System.out.print(s + " ");
        }
        System.out.println();
    }*/

    public void printEdges(CompactPMPG pmpg) {
        Iterator nodeIterator = pmpg.getNodes().iterator();
        while(nodeIterator.hasNext()) {
            int node = (int) nodeIterator.next();
            Iterator edgeIterator = pmpg.getOutgoingEdges(node).iterator();

            while(edgeIterator.hasNext()) {
                CompactPMPGEdge edge = (CompactPMPGEdge) edgeIterator.next();
                System.out.println("from " + node + " to " + edge.getTarget() + " with label " + edge.getLabel());
            }
        }
    }
}
