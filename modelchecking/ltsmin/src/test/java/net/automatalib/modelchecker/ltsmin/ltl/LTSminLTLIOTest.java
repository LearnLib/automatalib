/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
package net.automatalib.modelchecker.ltsmin.ltl;

import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.modelchecking.impl.MealyLassoImpl;

/**
 * Tests whether LTSminLTLAlternating actually uses regular edge semantics.
 *
 * @see LTSminLTLAlternatingTest
 */
public class LTSminLTLIOTest extends AbstractLTSminLTLMealyTest {

    private LTSminLTLIO<String, String> modelChecker;

    @Override
    public LTSminLTLIO<String, String> getModelChecker() {
        return modelChecker;
    }

    @Override
    public String getSkipFormula() {
        return "!(input == \"b\")";
    }

    @Override
    public void newModelChecker() {
        modelChecker = new LTSminLTLIOBuilder<String, String>().withString2Input(s -> s).withString2Output(s -> s).create();
    }

    @Override
    protected MealyLasso<String, String> createCounterExample() {
        return new MealyLassoImpl<>(createAutomaton(), getAlphabet(), 4);
    }

    @Override
    protected String createFalseProperty() {
        return "input == \"b\"";
    }
}

