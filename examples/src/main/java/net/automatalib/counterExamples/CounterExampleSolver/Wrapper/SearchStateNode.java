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
        /*String subformulaString = subformula.toString();
        result += "Current formula: " + indent.substring(0, indent.length() - subformulaString.length()) + subformulaString + "\n";
        result += "Procedures: " + indent.substring(0, indent.length() - procedures.size()) + procedures + "\n";
        String contextString = contexts.toString();

        result += "Contexts: " + indent.substring(0, indent.length() - contexts.size()) + contextString + "\n";
        result += "ReturnAddress: " + indent.substring(0, indent.length() - returnAddress.size()) + returnAddress + "\n";
        result += "RecDepth: " + indent.substring(0, indent.length() - contexts.size()) + expansionDepth + "\n";
*/
        return result;
    }

    public String trimmedStringSearchStateWrapper(int trimLength){
        String result = "<HTML><TABLE BORDER=\"0px\">";
       // String indent = "                                        "; //20 spaces

        result += "<TR><TD>State: </TD><TD>" +   state + "</TD> </TR>";
        String contextString = contexts.toString();
        result += "<TR><TD>Contexts: </TD><TD>" + contextString + "</TD></TR>";
        //String subformulaString = subformula.toString();
        /*if(subformulaString.length() > trimLength){
            subformulaString = subformulaString.substring(0,trimLength) + "...";
        }*/
        //result += "<TR><TD>Current formula: </TD><TD> \"" + subformulaString + "\"</TD></TR>";

        /*String proceduresString = procedures.toString();
        if(proceduresString.length() > trimLength){
            proceduresString = proceduresString.substring(0,trimLength) + "...";
        }
        result += "Procedures: " + indent.substring(0, indent.length() - procedures.size()) + proceduresString + "\n";

        String contextString = contexts.toString();
        if(contextString.length() > trimLength){
            contextString = contextString.substring(0,trimLength) + "...";
        }
        result += "Contexts: " + indent.substring(0, indent.length() - contexts.size()) + contextString + "\n";

        String returnAddressString = returnAddress.toString();
        if(returnAddressString.length() > trimLength){
            returnAddressString = returnAddressString.substring(0,trimLength) + "...";
        }
        result += "ReturnAddress: " + indent.substring(0, indent.length() - returnAddress.size()) + returnAddressString + "\n";*/

        result += "</TABLE>";

        return result;
    }
}
