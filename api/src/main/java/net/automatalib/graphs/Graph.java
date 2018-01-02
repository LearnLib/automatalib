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
package net.automatalib.graphs;

import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;

/**
 * Graph interface. Like an {@link IndefiniteGraph}, but with the additional requirement that the set of nodes be
 * finite.
 *
 * @param <N>
 *         node type
 * @param <E>
 *         edge type
 *
 * @author Malte Isberner
 */
public interface Graph<N, E> extends IndefiniteGraph<N, E>, SimpleGraph<N> {

    @Override
    default VisualizationHelper<N, E> getVisualizationHelper() {
        return new DefaultVisualizationHelper<>();
    }

    @Override
    default Graph<N, E> asNormalGraph() {
        return this;
    }
}
