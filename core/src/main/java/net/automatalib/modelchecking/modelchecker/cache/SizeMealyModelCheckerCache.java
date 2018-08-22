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
package net.automatalib.modelchecking.modelchecker.cache;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.modelchecking.ModelCheckerCache;

/**
 * @see SizeDFAModelCheckerCache
 */
public class SizeMealyModelCheckerCache<I, O, P>
        extends SizeModelCheckerCache<I, MealyMachine<?, I, ?, O>, P, MealyMachine<?, I, ?, O>>
        implements ModelCheckerCache.MealyModelCheckerCache<I, O, P> {

    private final MealyModelChecker<I, O, P> modelChecker;

    public SizeMealyModelCheckerCache(MealyModelChecker<I, O, P> modelChecker) {
        super(modelChecker::findCounterExample);
        this.modelChecker = modelChecker;
    }

    @Override
    public MealyModelChecker<I, O, P> getModelChecker() {
        return modelChecker;
    }
}
