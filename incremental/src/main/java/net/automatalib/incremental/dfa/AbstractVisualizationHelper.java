/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.incremental.dfa;

import java.util.Map;

import net.automatalib.api.automaton.Automaton;
import net.automatalib.api.automaton.visualization.AutomatonVisualizationHelper;

/**
 * Abstract visualization helper for {@link IncrementalDFABuilder}s.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <A>
 *         automaton tyep
 */
public abstract class AbstractVisualizationHelper<S, I, T, A extends Automaton<S, I, T>>
        extends AutomatonVisualizationHelper<S, I, T, A> {

    private int idx;

    public AbstractVisualizationHelper(A automaton) {
        super(automaton);
    }

    @Override
    public boolean getNodeProperties(S node, Map<String, String> properties) {
        super.getNodeProperties(node, properties);

        properties.put(NodeAttrs.LABEL, "n" + (idx++));
        switch (getAcceptance(node)) {
            case TRUE:
                properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
                break;
            case DONT_KNOW:
                properties.put(NodeAttrs.STYLE, NodeStyles.DASHED);
                break;
            default: // case FALSE: default style
        }

        return true;
    }

    protected abstract Acceptance getAcceptance(S s);
}
