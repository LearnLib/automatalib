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

import java.util.Map;
import java.util.Set;

import net.automatalib.visualization.DelegateVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Alphabet;

public interface ModalContract<S, I, T, TP extends ModalContractEdgeProperty>
        extends ModalTransitionSystem<S, I, T, TP> {

    Set<T> getRedTransitions();

    Alphabet<I> getCommunicationAlphabet();

    default boolean isSymbolInCommunicationAlphabet(I symbol) {
        if (getCommunicationAlphabet() == null) {
            // TODO: maybe throw?
            return false;
        }
        return getCommunicationAlphabet().containsSymbol(symbol);
    }

    boolean checkRedTransitions();

    @Override
    default VisualizationHelper<S, T> getVisualizationHelper() {
        final VisualizationHelper<S, T> superHelper = ModalTransitionSystem.super.getVisualizationHelper();
        return new DelegateVisualizationHelper<S, T>(superHelper) {

            @Override
            public boolean getEdgeProperties(S src, T edge, S tgt, Map<String, String> properties) {
                if (!super.getEdgeProperties(src, edge, tgt, properties)) {
                    return false;
                }

                final TP transitionProperty = getTransitionProperty(edge);

                if (transitionProperty.isTau()) {
                    properties.put(EdgeAttrs.LABEL, "τ");
                }

                if (transitionProperty.isGreen()) {
                    properties.put(EdgeAttrs.COLOR, "green");
                } else if (transitionProperty.isRed()) {
                    properties.put(EdgeAttrs.COLOR, "red");

                }

                return true;
            }
        };
    }

}
