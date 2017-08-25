/* Copyright (C) 2013-2017 TU Dortmund
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
package net.automatalib.visualization;

import java.util.Map;

import net.automatalib.graphs.Graph;
import net.automatalib.graphs.dot.GraphDOTHelper;

public interface VisualizationProvider {

    default String getName() {
        return getId();
    }

    String getId();

    default String getDescription() {
        return "";
    }

    default int getPriority() {
        return 0;
    }

    boolean checkUsable();

    <N, E> void visualize(Graph<N, E> graph,
                          GraphDOTHelper<N, ? super E> helper,
                          boolean modal,
                          Map<String, String> visOptions);
}
