package net.automatalib.modelcheckers.m3c.transformer;

import java.util.function.BinaryOperator;

import info.scce.addlib.dd.xdd.latticedd.example.BooleanVector;
import net.automatalib.modelcheckers.m3c.cfps.State;
import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BinaryFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;

public class DiamondOperation implements BinaryOperator<BooleanVector> {

    private final EquationalBlock block;
    private final State state;

    public DiamondOperation(State state, EquationalBlock block) {
        this.state = state;
        this.block = block;
    }

    @Override
    public BooleanVector apply(BooleanVector left, BooleanVector right) {
        boolean[] result = left.data().clone();
        for (FormulaNode node : block.getNodes()) {
            int currentVar = node.getVarNumber();
            if (node instanceof BoxNode) {
                result[currentVar] = result[currentVar] && right.data()[currentVar];
            } else if (node instanceof DiamondNode) {
                result[currentVar] = result[currentVar] || right.data()[currentVar];
            } else if (node instanceof BinaryFormulaNode) {
                int xj1 = node.getVarNumberLeft();
                int xj2 = node.getVarNumberRight();
                if (node instanceof AndNode) {
                    result[currentVar] = result[xj1] && result[xj2];
                } else {
                    result[currentVar] = result[xj1] || result[xj2];
                }
            } else if (node instanceof TrueNode) {
                result[currentVar] = true;
            } else if (node instanceof FalseNode) {
                result[currentVar] = false;
            } else if (node instanceof NotNode) {
                result[currentVar] = !result[node.getVarNumberLeft()];
            } else if (node instanceof AtomicNode) {
                String prop = ((AtomicNode) node).getProposition();
                result[currentVar] = state.satisfiesAtomicProposition(prop);
            }
        }
        return new BooleanVector(result);
    }

}
