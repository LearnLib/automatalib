/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.ts.modal;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.graphs.FiniteLTS;
import net.automatalib.visualization.DelegateVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;

public interface ModalTransitionSystem<S, I, T, TP extends ModalEdgeProperty>
        extends UniversalAutomaton<S, I, T, Void, TP>, FiniteLTS<S, T, I>, InputAlphabetHolder<I> {

    @Nonnull
    @Override
    default Iterator<S> iterator() {
        return UniversalAutomaton.super.iterator();
    }

    @Override
    default int size() {
        return UniversalAutomaton.super.size();
    }

    @Override
    default VisualizationHelper<S, T> getVisualizationHelper() {
        final VisualizationHelper<S, T> superHelper = FiniteLTS.super.getVisualizationHelper();
        return new DelegateVisualizationHelper<S, T>(superHelper) {

            @Override
            public boolean getEdgeProperties(S src, T edge, S tgt, Map<String, String> properties) {
                if (!super.getEdgeProperties(src, edge, tgt, properties)) {
                    return false;
                }

                if (getTransitionProperty(edge).isMayOnly()) {
                    properties.put(EdgeAttrs.STYLE, EdgeStyles.DASHED);
                }

                return false;
            }
        };
    }

}
