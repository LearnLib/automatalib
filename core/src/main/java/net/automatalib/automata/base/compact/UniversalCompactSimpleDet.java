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
package net.automatalib.automata.base.compact;

import java.util.Arrays;

import net.automatalib.words.Alphabet;

public class UniversalCompactSimpleDet<I, SP> extends AbstractCompactSimpleDeterministic<I, SP> {

    private Object[] stateProperties;

    public UniversalCompactSimpleDet(Alphabet<I> alphabet) {
        super(alphabet);
    }

    public UniversalCompactSimpleDet(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
    }

    @Override
    public void clear() {
        Arrays.fill(stateProperties, 0, size(), null);
        super.clear();
    }

    @Override
    public void setStateProperty(int stateId, SP property) {
        stateProperties[stateId] = property;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SP getStateProperty(int stateId) {
        return (SP) stateProperties[stateId];
    }



}
