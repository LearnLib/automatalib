/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.modelcheckers.m3c.transformer;

import java.util.List;
import java.util.Set;

import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;

/**
 * @author murtovi
 */
public abstract class AbstractPropertyTransformer<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final boolean isMust;

    AbstractPropertyTransformer() {
        this(true);
    }

    AbstractPropertyTransformer(boolean isMust) {
        this.isMust = isMust;
    }

    public abstract Set<Integer> evaluate(boolean[] input);

    public abstract T compose(T other, boolean isMust);

    public abstract T createUpdate(Set<AP> atomicPropositions,
                                   List<T> compositions,
                                   EquationalBlock<L, AP> currentBlock);

    public abstract List<String> serialize();

    public boolean isMust() {
        return isMust;
    }

}