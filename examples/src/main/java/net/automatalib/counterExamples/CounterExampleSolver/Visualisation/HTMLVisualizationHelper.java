package net.automatalib.counterExamples.CounterExampleSolver.Visualisation;

import net.automatalib.counterExamples.CounterExampleSolver.CounterExampleTree;

import java.util.Map;

public class HTMLVisualizationHelper extends AbstractVisualizationHelper{
    private int trimLength;
    private boolean showFormulaAsGrammar;

    public HTMLVisualizationHelper(CounterExampleTree resultTree, int trimLength, boolean showFormulaAsGrammar){
        super(resultTree);
        this.trimLength = trimLength;
        this.showFormulaAsGrammar = showFormulaAsGrammar;
    }


    @Override
    public boolean getNodeProperties(Integer node, Map<String, String> properties) {
        super.getNodeProperties(node, properties);

        String boxNodeLabel = "<HTML><TABLE BORDER=\"0px\">";

        boxNodeLabel += "<TR><TD>State:</TD><TD>" + resultTree.getNodeProperty(node).state + "</TD> </TR>";

        String contextString = resultTree.getNodeProperty(node).contexts.toString();

        boxNodeLabel += "<TR><TD>Contexts:</TD><TD>" + contextString + "</TD></TR>";

        boxNodeLabel += "<TR><TD>Procedures:</TD><TD>" + resultTree.getNodeProperty(node).procedures.toString() + "</TD> </TR>";

        boxNodeLabel += "<TR><TD>ReturnAddress:</TD><TD>" + resultTree.getNodeProperty(node).returnAddress.toString() + "</TD></TR>";

        boxNodeLabel += "<TR><TD>Formula:</TD><TD> " + transformFormulaForHTML(node) + " </TD></TR>";

        boxNodeLabel += "</TABLE>";

        properties.put(NodeAttrs.LABEL, boxNodeLabel);
        return true;
    }

    private String transformFormulaForHTML(Integer node){
        String formula = resultTree.getNodeProperty(node).subformula.toString();
        if(showFormulaAsGrammar){
            resultTree.getNodeProperty(node).subformula.getVarNumber();
        } else {
            formula = formula.replace("&", "&amp;");
            formula = formula.replace("<", "&lt;");
            formula = formula.replace(">", "&gt;");

        }
        return formula;
    }



}
