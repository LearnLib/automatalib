
package net.automatalib.counterExamples.SuperSolver;

import com.google.common.collect.Sets;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.strings.AbstractPrintable;
import net.automatalib.counterExamples.SuperSolver.GraphGenerator.GraphGenerator;
import net.automatalib.counterExamples.SuperSolver.Visualisation.*;
import net.automatalib.counterExamples.SuperSolver.Wrapper.SearchStateNode;
import net.automatalib.counterExamples.SuperSolver.gearElements.IModel;
import net.automatalib.counterExamples.SuperSolver.gearElements.NodeExplorer;
import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.graphs.base.DefaultCFMPS;
import net.automatalib.graphs.base.compact.CompactBidiEdge;
import net.automatalib.graphs.base.compact.CompactPMPGEdge;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.modelcheckers.m3c.formula.*;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.solver.BDDSolver;
import net.automatalib.modelcheckers.m3c.solver.M3CSolver.TypedM3CSolver;
import net.automatalib.modelcheckers.m3c.solver.M3CSolvers;
import net.automatalib.ts.modal.transition.ProceduralModalEdgePropertyImpl;
import net.automatalib.util.graphs.Graphs;
import net.automatalib.visualization.Visualization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked", "WhileLoopReplaceableByForEach"})
public class SuperSolver<L, AP> extends BDDSolver<L, AP> {

    boolean continueCalc = true;
    int graphSize = 0;
    public MagicTree resultTree = new MagicTree("lambda");
    static Logger logger = LoggerFactory.getLogger(SuperSolver.class);

    //benchmark variables
    public long calcFulfilledTime;
    public long calcTreeTime;

    public SuperSolver(ContextFreeModalProcessSystem<L, AP> cfmps) {
        super(cfmps);
    }

    //main is rather used as a debugging function now, actual tests/benchmarks in happen in their respective classes
    public static void main(String[] args) throws ParseException, IOException, ParserConfigurationException, SAXException {

        //generate graphs
        GraphGenerator graphGenerator = new GraphGenerator(false);

        //initialise mcfps + solver
        DefaultCFMPS<String, Void> cfmps = graphGenerator.cfmpsList.get(0);

        TypedM3CSolver<FormulaNode<String, Void>> m3c =
                M3CSolvers.typedSolver(cfmps);
        SuperSolver<String, Void> solver = new SuperSolver<>(cfmps);

        //set up formulas under test
        String fGoal = "mu X.(<b>true || <>X)";
        FormulaNode<String, Void> formula = M3CParser.parse(fGoal, l -> l,
                ap -> null);

        //evaluate formula
        boolean isFulfilled = m3c.solve(formula);
        logger.info(formula + " erfüllt? -> " + isFulfilled);

        if(isFulfilled){
            MagicTree magicTree = solver.computeWitness(cfmps, formula);
            magicTree.calcMaxDepthInit();
            Visualization.visualize(magicTree,
                    //new ColorVisualizationHelper(magicTree, false, true),
                    new HTMLVisualizationHelper(magicTree, 20, false),
                    new BoxNodeStyleVisualizationHelper(magicTree, true, true)
                    //new EdgeVisualizationHelper(magicTree, true)
            );
        }
    }

    public MagicTree computeWitness(DefaultCFMPS cfmps, FormulaNode<L, AP> formula) {
        try {
            long startTimeFulfilled = System.nanoTime();
            boolean isFulfilled = solve(formula);
            long endTimeFulfilled = System.nanoTime();

            calcFulfilledTime = endTimeFulfilled - startTimeFulfilled;
            if(isFulfilled){
                return computeWitnessStep(cfmps, formula);
            }
        } finally {
            shutdownDDManager();
            logger.debug("Finished calculation");
        }
        return null;
    }

    public MagicTree computeWitnessStep(DefaultCFMPS cfmps, FormulaNode<L, AP> formula){

        LinkedList<SearchStateNode> queueBFS = new LinkedList<>();
        Wrapper apMain = new Wrapper<>(super.workUnits.get(cfmps.getMainProcess()));
        SearchStateNode currentSearchStateWrapper = fillInitialSearchStateWrapper(apMain, formula);
        logger.trace("first Wrapper initialized with: \n" + currentSearchStateWrapper.printSearchStateWrapper());

        queueBFS.add(currentSearchStateWrapper);

        logger.trace("starting pseudoBFS");
        long startTimePathCalc = System.nanoTime();

        while(!queueBFS.isEmpty()){
            if(!continueCalc){
                break;
            }
            logger.debug("------ starting work on next queue element ------");
            SearchStateNode queueElement = queueBFS.getFirst();
            logger.debug(queueElement.printSearchStateWrapper());
            queueBFS.removeFirst();

            int positionInResultTree = resultTree.addNode(queueElement);

            if(positionInResultTree <= 0){
                logger.trace("positionInResultTree is <= 0, so no connection needed");
            } else {
                logger.debug("connecting " + queueElement.parentNode + " with " + positionInResultTree);
                resultTree.connect(queueElement.parentNode.intValue(), positionInResultTree, queueElement.edgeLabel);
            }

            ArrayList<SearchStateNode> fulfilledNext = getValidNextWrapperState(queueElement);

            if(fulfilledNext.size() != 0 && continueCalc){
                queueBFS.addAll(fulfilledNext);
            } else {
                logger.debug("fulfilledNext is empty, terminating");
            }
        }
        long endTimePathCalc = System.nanoTime();
        calcTreeTime = endTimePathCalc - startTimePathCalc;
        resultTree.calcMaxDepthInit();
        logger.debug("maxDepth should be: " + resultTree.calcMaxDepth(0));
        resultTree.extractPath();

        return resultTree;
    }

    public ArrayList<SearchStateNode> getValidNextWrapperState(SearchStateNode queueElement){
        if(continueCalc){
            NodeExplorer nodeExplorer = new NodeExplorer<Character, Void>();
            FormulaNode visitedFormula = nodeExplorer.visit(queueElement.subformula, false);

            if(visitedFormula instanceof LfpNode) {
                FormulaNode child = ((LfpNode<Character, Void>) visitedFormula).getChild();
                visitedFormula = nodeExplorer.visit(child, false);
            }

            if (visitedFormula instanceof OrNode) {
                return exploreOR(visitedFormula, queueElement);
            }
            else if (visitedFormula instanceof DiamondNode) {
                return exploreDia(visitedFormula, queueElement);
            }
            else if (visitedFormula instanceof TrueNode){
                //logger.info("TrueNode found, terminating with empty list at " + queueElement.resultGraphSize);
                resultTree.finishingNode = queueElement.resultGraphSize;
                continueCalc = false;
            }
            else if (visitedFormula instanceof AtomicNode){
                logger.info("AtomicNode found, terminating with empty list at " + queueElement.resultGraphSize);
                resultTree.finishingNode = queueElement.resultGraphSize;
                continueCalc = false;
            }
        }

        return new ArrayList<>();
    }

    // starte einzelne formelnsuche hier
    private ArrayList<SearchStateNode> exploreOR(FormulaNode<L, AP> formula, SearchStateNode queueElement){
        String formulaLeft = findGlobalVarNumber(((OrNode<L, AP>) formula).getLeftChild().toNNF());
        String formulaRight = findGlobalVarNumber(((OrNode<L, AP>) formula).getRightChild().toNNF());
        logger.debug("debugging exploreOR of " + queueElement.state);
        logger.debug("formulaLeft " + formulaLeft + "  and formulaRight " + formulaRight);
        ArrayList<SearchStateNode> result = new ArrayList<>();

        int nextCurrentNode = graphSize;

        if(queueElement.apMain.getAtomicPropositions(queueElement.state).contains(Integer.parseInt(formulaLeft))) {
            logger.debug("left is valid");
            nextCurrentNode++;
            result.add(new SearchStateNode(queueElement.procedures,
                            queueElement.contexts, queueElement.expansionDepth, queueElement.state, nextCurrentNode, queueElement.resultGraphSize,
                            dependencyGraph.getFormulaNodes().get(Integer.parseInt(formulaLeft)),
                            queueElement.returnAddress,
                            queueElement.apMain,
                            dependencyGraph.getFormulaNodes().get(Integer.parseInt(formulaLeft)).toString())
                            );
            graphSize++;
        }
        if(queueElement.apMain.getAtomicPropositions(queueElement.state).contains(Integer.parseInt(formulaRight))){
            logger.debug("right is valid");
            nextCurrentNode++;
            result.add(new SearchStateNode(queueElement.procedures,
                            queueElement.contexts, queueElement.expansionDepth, queueElement.state, nextCurrentNode, queueElement.resultGraphSize,
                            dependencyGraph.getFormulaNodes().get(Integer.parseInt(formulaRight)),
                            queueElement.returnAddress,
                            queueElement.apMain,
                    dependencyGraph.getFormulaNodes().get(Integer.parseInt(formulaRight)).toString())
            );
            graphSize++;
        }
        logger.debug("exploring OR of " + queueElement.state + " found " + result.size() + " answer/s");
        return result;
    }
    private ArrayList<SearchStateNode> exploreDia(FormulaNode<L, AP> formula, SearchStateNode queueElement){
        ArrayList<SearchStateNode> result;

        logger.debug("exploring dia of " + queueElement.state);
        String diaMoveLabel = findDiaMoveLabel(formula);
        if(diaMoveLabel.equals(">")){
            logger.debug("dia move is via '>', check fulfilled in next");
            result = findDiaMoveWithEmpty(queueElement, formula);
        } else if (queueElement.state == (int)queueElement.apMain.pmpg.getFinalNode() && queueElement.returnAddress.size() > 0){
            logger.debug("dia move from endNode -> check returnAddress");
            result = findDiaMoveEndNodeReturn(queueElement);
        } else {
            logger.debug("dia move is via a specific path");
            result = findDiaMoveRegularStep(queueElement, formula);
        }

        return result;
    }

    private ArrayList<SearchStateNode> findDiaMoveWithEmpty(SearchStateNode queueElement, FormulaNode formula){
        ArrayList<SearchStateNode> result = new ArrayList<>();
        Iterator edgeIter = queueElement.apMain.getOutgoingEdges(queueElement.state).iterator();
        int formulaNode = queueElement.subformula.getVarNumber();
        while(edgeIter.hasNext()){
            CompactPMPGEdge edge = (CompactPMPGEdge) edgeIter.next();
            if(dependencyGraph.getFormulaNodes().get(formulaNode).toString().substring(0,3).equals("(<>") && !dependencyGraph.getFormulaNodes().get(formulaNode).toString().contains("X")){
                formulaNode++;
            }
            if(dependencyGraph.getFormulaNodes().get(formulaNode).toString().contains("X")){
                formulaNode = 0;
            }
            switch ( ((ProceduralModalEdgePropertyImpl)edge.getProperty()).getProceduralType() ) {
                case INTERNAL:
                    if(queueElement.apMain.getAtomicPropositions(edge.getTarget()).contains(formula.getVarNumber()) ) {
                        SearchStateNode toAdd = new SearchStateNode(
                                queueElement.procedures,
                                queueElement.contexts,
                                queueElement.expansionDepth,
                                edge.getTarget(),
                                graphSize+1,
                                queueElement.resultGraphSize,
                                dependencyGraph.getFormulaNodes().get(formulaNode),
                                queueElement.returnAddress,
                                queueElement.apMain,
                                edge.getLabel().toString()
                        );
                        graphSize++;
                        result.add(toAdd);
                    }
                    break;
                case PROCESS:
                    logger.debug("target is a procedural edge!");
                    Stack newProcedures = new Stack();
                    newProcedures.addAll(queueElement.procedures);
                    newProcedures.push(edge.getLabel().toString());
                    Stack newContext = new Stack();
                    newContext.addAll(queueElement.contexts);
                    newContext.push(queueElement.apMain.getAtomicPropositions(edge.getTarget()));
                    Stack newReturnAddresses = new Stack();
                    newReturnAddresses.addAll(queueElement.returnAddress);
                    newReturnAddresses.push(edge.getTarget());
                    SearchStateNode toAdd = new SearchStateNode(
                            newProcedures,
                            newContext,
                            queueElement.expansionDepth+1,
                            0,
                            graphSize+1,
                            queueElement.resultGraphSize,
                            dependencyGraph.getFormulaNodes().get(formulaNode),
                            newReturnAddresses,
                            new Wrapper<>(super.workUnits.get(edge.getLabel().toString()), Sets.newHashSet(queueElement.apMain.getAtomicPropositions(edge.getTarget()))),
                            edge.getLabel().toString()
                    );
                    graphSize++;
                    result.add(toAdd);
                break;
            }
        }
        if(result.size() == 0) { logger.debug("found nothing in findDiaMoveWithEmpty");}
        return result;
    }
    private ArrayList<SearchStateNode> findDiaMoveEndNodeReturn(SearchStateNode queueElement){
        ArrayList<SearchStateNode> result = new ArrayList<>();
        //wir sind im endzustand mit rücksprungadressen
        Set<Integer> nextContext = queueElement.contexts.pop();
        queueElement.procedures.pop();
        String procedureName = queueElement.procedures.peek();
        SearchStateNode toAdd = new SearchStateNode(
                                        queueElement.procedures,
                                        queueElement.contexts,
                                        queueElement.expansionDepth-1,
                                        queueElement.returnAddress.pop(),
                                        graphSize+1,
                                        queueElement.resultGraphSize,
                                        queueElement.subformula,
                                        queueElement.returnAddress,
                                        new Wrapper<>(super.workUnits.get(procedureName), Sets.newHashSet(nextContext)),
                                        "return"
        );
        graphSize++;
        result.add(toAdd);
        return result;
    }
    private ArrayList<SearchStateNode> findDiaMoveRegularStep(SearchStateNode queueElement, FormulaNode formula){
        ArrayList<SearchStateNode> result = new ArrayList<>();
        String moveLabel = findDiaMoveLabel(formula);
        Iterator edgeIter = queueElement.apMain.getOutgoingEdges(queueElement.state).iterator();
        while(edgeIter.hasNext()){
            CompactPMPGEdge edge = (CompactPMPGEdge) edgeIter.next();
            if( edge.getLabel().equals(moveLabel) ){
                Stack newProcedures = queueElement.procedures;
                Stack newContext = queueElement.contexts;
                Stack newReturnAddresses = queueElement.returnAddress;
                SearchStateNode toAdd = new SearchStateNode(
                        newProcedures,
                        newContext,
                                                queueElement.expansionDepth,
                                                edge.getTarget(),
                        graphSize+1,
                                                queueElement.resultGraphSize,
                                                dependencyGraph.getFormulaNodes().get(queueElement.subformula.getVarNumber()+1),
                        newReturnAddresses,
                                                queueElement.apMain,
                                                edge.getLabel().toString()
                );
                graphSize++;
                result.add(toAdd);
            } else if(Character.isUpperCase( edge.getLabel().toString().charAt(0) ) ){
                Stack newProcedures = new Stack();
                newProcedures.addAll(queueElement.procedures);
                newProcedures.push(edge.getLabel().toString());
                Stack newContext = new Stack();
                newContext.addAll(queueElement.contexts);
                newContext.push(queueElement.apMain.getAtomicPropositions(queueElement.state));
                Stack newReturnAddresses = new Stack();
                newReturnAddresses.addAll(queueElement.returnAddress);
                newReturnAddresses.push(edge.getTarget());
                SearchStateNode toAdd = new SearchStateNode(
                        newProcedures,
                        newContext,
                        queueElement.expansionDepth+1,
                        0,
                        graphSize+1,
                        queueElement.resultGraphSize,
                        dependencyGraph.getFormulaNodes().get(queueElement.subformula.getVarNumber()),
                        newReturnAddresses,
                        new Wrapper<>(super.workUnits.get(edge.getLabel().toString()), Sets.newHashSet(queueElement.apMain.getAtomicPropositions(edge.getTarget()))),
                        edge.getLabel().toString()
                );
                graphSize++;
                logger.debug("adding this from within findDiaMoveRegularStep:\n" + toAdd.printSearchStateWrapper());
                result.add(toAdd);
            }
        }
        if(result.size() == 0) {logger.debug("found nothing in findDiaMoveRegularStep");}
        return result;
    }



    //---------------------------------helper methods---------------------------------
    private String findGlobalVarNumber(FormulaNode<L, AP> toCheck) {
        int result = -1;
        for(FormulaNode<L, AP> node : dependencyGraph.getFormulaNodes()){
            if(node.toString().equals(toCheck.toString())) {
                result = node.getVarNumber();
            }
        }
        return String.valueOf(result);
    }
    private String findDiaMoveLabel(FormulaNode<L, AP> formula){
        String tmp = formula.toString();
        int index = tmp.indexOf('<');
        return Character.toString(tmp.charAt(index+1));
    }
    private SearchStateNode fillInitialSearchStateWrapper(Wrapper apMain, FormulaNode formula) {
        Stack<Wrapper<Integer, CompactBidiEdge<String>>> atomicPropositions = new Stack<>();
        atomicPropositions.add(apMain);
        List<String> procedures = new ArrayList<>();
        procedures.add(atomicPropositions.get(0).unit.label.toString());
        List<Set<Integer>> context = new ArrayList<>();
        context.add(atomicPropositions.get(0).subformulasInFinalState);
        List<Integer> returnAddresses = new ArrayList<>();

        return new SearchStateNode(procedures, context, 0, 0, 0, 0, formula, returnAddresses, apMain, "nullString");
    }


    public class Wrapper<N, E> extends AbstractPrintable implements IModel<N, E> {

        public final WorkUnit<N, E> unit;
        public final ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg;
        public final Set<Integer> subformulasInFinalState;

        public final Mapping<N, Collection<E>> incomingEdges;
        public final NodeIDs<N> nodeIDs;
        public final Set<E> edges;

        public final List<String> atomicPropositions;


        Wrapper(WorkUnit<N, E> unit) {
            this(unit, getAllAPDeadlockedNode());
        }

        Wrapper(WorkUnit<N, E> unit, Set<Integer> subformulasInFinalState) {
            this.unit = unit;
            this.pmpg = unit.pmpg;
            this.subformulasInFinalState = subformulasInFinalState;

            this.incomingEdges = Graphs.incomingEdges(pmpg);
            this.nodeIDs = pmpg.nodeIDs();
            this.edges = new HashSet<>();
            this.atomicPropositions = new ArrayList<>();

            for (N n : this.pmpg) {
                this.edges.addAll(this.pmpg.getOutgoingEdges(n));
                this.atomicPropositions.add(getAtomicPropositions(n).toString());
            }
        }

        @Override
        public String getIdentifier(N node) {
            return String.valueOf(nodeIDs.getNodeId(node));
        }

        @Override
        public Set<N> getNodes() {
            return new HashSet<>(pmpg.getNodes());
        }

        @Override
        public Set<E> getOutgoingEdges(N node) {
            return new HashSet<>(pmpg.getOutgoingEdges(node));
        }

        @Override
        public Set<E> getIncomingEdges(N node) {
            return new HashSet<>(this.incomingEdges.get(node));
        }

        @Override
        public Set<E> getEdges() {
            return this.edges;
        }

        @Override
        public EdgeType getEdgeType(E edge) {
            return EdgeType.MUST;
        }

        @Override
        public N getSource(E edge) {
            for (N n : this.pmpg) {
                for (E e : this.pmpg.getOutgoingEdges(n)) {
                    if (Objects.equals(e, edge)) {
                        return n;
                    }
                }
            }

            throw new IllegalStateException();
        }

        @Override
        public N getTarget(E edge) {
            return this.pmpg.getTarget(edge);
        }

        @Override
        public Set<String> getEdgeLabels(E edge) {
            return Collections.singleton(String.valueOf(this.pmpg.getEdgeLabel(edge)));
        }

        @Override
        public Set<Integer> getAtomicPropositions(N node) {

            final Set<Integer> output =
                    unit.propTransformers.get(node).evaluate(toBoolArray(subformulasInFinalState));
            final Set<Integer> satisfiedSubFormulas = new HashSet<>();
            for (FormulaNode<L, AP> n : dependencyGraph.getFormulaNodes()) {
                if (output.contains(n.getVarNumber())) {
                    satisfiedSubFormulas.add(n.getVarNumber());
                }
            }

            return satisfiedSubFormulas;
        }

        @Override
        public Set<N> getInitialNodes() {
            return Collections.singleton(this.pmpg.getInitialNode());
        }

        @Override
        public void print(Appendable a) throws IOException {
            for (N n : this.pmpg) {
                a.append(this.getIdentifier(n));
                a.append(' ');
                a.append(getAtomicPropositions(n).toString());
                a.append('\n');
            }

        }
    }

}
