/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package net.automatalib.modelcheckers.ltsmin;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.modelchecking.MealyLassoImpl;
import net.automatalib.util.automata.builders.AutomatonBuilders;

/**
 * Tests whether LTSminLTLAlternating actually uses regular edge semantics.
 *
 * @author Jeroen Meijer
 * @see LTSminLTLAlternatingTest
 */
public class LTSminLTLIOTest extends AbstractLTSminLTLMealyTest<LTSminLTLIO<String, String>> {

    @Override
    protected LTSminLTLIO<String, String> createModelChecker() {
        return new LTSminLTLIOBuilder<String, String>().withString2Input(s -> s).withString2Output(s -> s).create();
    }

    @Override
    protected MealyLasso<?, String, ?, String> createLasso() {
        return new MealyLassoImpl<>(createAutomaton(), getAlphabet(), 4);
    }

    @Override
    protected MealyMachine<?, String, ?, String> createAutomaton() {
        return AutomatonBuilders.forMealy(new CompactMealy<String, String>(getAlphabet()))
                                .withInitial("q0")
                                .from("q0")
                                .on("a")
                                .withOutput("1")
                                .loop()
                                .create();
    }

    @Override
    protected String createFalseProperty() {
        return "input == \"b\"";
    }
}