package net.automatalib.modelcheckers.m3c.formula.parser;

import java.io.StringReader;
import java.util.HashSet;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;

public class ParserMuCalc {

    public static FormulaNode parse(String muCalcFormula) throws ParseException {
        InternalM3CParserMuCalc muCalcParser = new InternalM3CParserMuCalc(new StringReader(muCalcFormula));
        muCalcParser.setFixedPointVars(new HashSet<>());
        return muCalcParser.formula();
    }

}
