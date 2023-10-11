/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.util.automaton.procedural;

import net.automatalib.graph.ProceduralModalProcessGraph;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;

/**
 * Edge class for the {@link ProceduralModalProcessGraph} view of procedural automata.
 *
 * @param <I>
 *         input symbol type
 * @param <S>
 *         state type
 */
class PMPGEdge<I, S> implements ProceduralModalEdgeProperty {

    final I input;
    final S succ;
    final ProceduralType type;

    PMPGEdge(I input, S succ, ProceduralType type) {
        this.input = input;
        this.succ = succ;
        this.type = type;
    }

    @Override
    public ModalType getModalType() {
        return ModalType.MUST;
    }

    @Override
    public ProceduralType getProceduralType() {
        return this.type;
    }
}
