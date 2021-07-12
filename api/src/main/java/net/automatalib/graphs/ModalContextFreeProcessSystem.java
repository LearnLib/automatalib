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
package net.automatalib.graphs;

import java.util.Map;

import net.automatalib.automata.concepts.FiniteRepresentation;
import net.automatalib.graphs.concepts.GraphViewable;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ModalContextFreeProcessSystem<L, AP> extends FiniteRepresentation, GraphViewable {

    Map<L, ModalProcessGraph<?, L, ?, AP, ?>> getMPGs();

    @Nullable L getMainProcess();

    @Override
    default int size() {
        return getMPGs().values().stream().mapToInt(ModalProcessGraph::size).sum();
    }

    @Override
    default Graph<?, ?> graphView() {
        // explicit type specification is required by checker-framework
        return new MCFPSGraphView<@Nullable Object, L, @Nullable Object, AP>(getMPGs());
    }
}
