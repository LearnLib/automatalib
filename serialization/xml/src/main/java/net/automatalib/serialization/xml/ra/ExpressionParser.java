/*
 * Copyright (C) 2014-2015 The LearnLib Contributors
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.serialization.xml.ra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.automatalib.automaton.ra.guard.impl.AtomicGuardExpression;
import net.automatalib.automaton.ra.guard.impl.Conjunction;
import net.automatalib.automaton.ra.guard.impl.Disjunction;
import net.automatalib.automaton.ra.GuardExpression;
import net.automatalib.automaton.ra.guard.impl.Relation;
import net.automatalib.automaton.ra.guard.impl.TrueGuardExpression;
import net.automatalib.data.SymbolicDataValue;

/**
 *
 * @author fh
 */
public class ExpressionParser {


    private final String expLine;
    private final Map<String, SymbolicDataValue> pMap;

    private GuardExpression predicate;

    public ExpressionParser(String exp, Map<String, SymbolicDataValue> pMap) {
        expLine = exp.trim();
        this.pMap = pMap;

        buildExpression();
    }

    private void buildExpression()
    {
        this.predicate = buildDisjunction(expLine);
    }

    private GuardExpression buildDisjunction(String dis) {
        StringTokenizer tok = new StringTokenizer(dis, "||");
        if (tok.countTokens() < 2) {
            return buildConjunction(dis);
        }
        List<GuardExpression> disjuncts = new ArrayList<>();
        while (tok.hasMoreTokens()) {
            disjuncts.add(buildConjunction(tok.nextToken().trim()));
        }
        return new Disjunction(disjuncts.toArray(new GuardExpression[] {}));
    }

    private GuardExpression buildConjunction(String con) {
        StringTokenizer tok = new StringTokenizer(con, "&&");
        if (tok.countTokens() < 2) {
            return buildPredicate(con);
        }
        List<GuardExpression> conjuncts = new ArrayList<>();
        while (tok.hasMoreTokens()) {
            conjuncts.add(buildPredicate(tok.nextToken().trim()));
        }
        return new Conjunction(conjuncts.toArray(new GuardExpression[] {}));
    }

    private GuardExpression buildPredicate(String pred)
    {

        pred = pred.replace("!=", "!!");
        if (pred.trim().length() < 1) {
            return new TrueGuardExpression();
        }

        Relation relation = null;
        String[] related = null;

        if (pred.contains("==")) {
            related = pred.split("==");
            relation = Relation.EQUALS;
        }
        else if (pred.contains("!!")) {
            related = pred.split("!!");
            relation = Relation.NOT_EQUALS;
        }
        else if (pred.contains("<")) {
            related = pred.split("<");
            relation = Relation.SMALLER;
        }
        else if (pred.contains(">")) {
            related = pred.split(">");
            relation = Relation.BIGGER;
        }

        if (relation == null) {
            throw new IllegalStateException(
                    "this should not happen!!! " + pred + " in " + expLine);
        }

        SymbolicDataValue left = pMap.get(related[0].trim());
        SymbolicDataValue right = pMap.get(related[1].trim());
        return new AtomicGuardExpression(left, relation, right);
    }

    /**
     * @return the predicate
     */
    public GuardExpression getPredicate() {
        return predicate;
    }

}
