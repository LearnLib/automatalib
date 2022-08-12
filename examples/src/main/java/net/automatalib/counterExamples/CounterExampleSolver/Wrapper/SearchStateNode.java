package net.automatalib.counterExamples.CounterExampleSolver.Wrapper;


import net.automatalib.counterExamples.CounterExampleSolver.CounterExampleSolver;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;

import java.util.*;

@SuppressWarnings("rawtypes")
public class SearchStateNode {
    public Stack<String> procedures;
    public Stack<Set<Integer>> contexts;
    public int expansionDepth;
    public int state;
    public Integer parentNode;
    public Integer resultGraphSize;
    public FormulaNode subformula;
    public Stack<Integer> returnAddress;
    public CounterExampleSolver.Wrapper apMain;
    public String edgeLabel;
    public boolean isPartOfResult;

    public SearchStateNode(List<String> givenProcedures, List<Set<Integer>> givenContexts,
                              int depthOutside, int givenState, Integer resultGraphSizeOutside, Integer parentNode,
                              FormulaNode givenSubformula,
                              List<Integer> givenReturnAddresses,
                              CounterExampleSolver.Wrapper apOutside,
                              String edgeLabelOutside){
        procedures = new Stack<>();
        procedures.addAll(givenProcedures);
        contexts = new Stack<>();
        contexts.addAll(givenContexts);

        expansionDepth = depthOutside;
        state = givenState;
        if(parentNode == null || parentNode < 0){
            this.parentNode = -1;
        } else {
            this.parentNode = parentNode;
        }
        resultGraphSize = resultGraphSizeOutside;
        subformula = givenSubformula;
        returnAddress = new Stack<>();
        returnAddress.addAll(givenReturnAddresses);
        apMain = apOutside;
        edgeLabel = edgeLabelOutside;
        isPartOfResult = false;
    }

    public String printSearchStateWrapper(){
        String result = "";
        String indent = "                                        "; //20 spaces
        result += "State: " + indent.substring(0, indent.length() - String.valueOf(state).length()) + state + "\n";
        return result;
    }

    public String trimmedStringSearchStateWrapper(int trimLength){
        String result = "<HTML><TABLE BORDER=\"0px\">";
       // String indent = "                                        "; //20 spaces

        result += "<TR><TD>State: </TD><TD>" +   state + "</TD> </TR>";
        String contextString = contexts.toString();
        result += "<TR><TD>Contexts: </TD><TD>" + contextString + "</TD></TR>";

        result += "</TABLE>";

        return result;
    }
}
