package net.automatalib.modelcheckers.m3c.formula.parser;

import java.io.StringReader;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;

public class ParserCTL {

    public static FormulaNode parse(String ctlForumla) throws ParseException {
        return new InternalM3CParserCTL(new StringReader(ctlForumla)).formula();
    }

}
