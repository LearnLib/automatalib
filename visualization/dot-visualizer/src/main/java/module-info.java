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

import net.automatalib.visualization.VisualizationProvider;
import net.automatalib.visualization.dot.GraphVizBrowserVisualizationProvider;
import net.automatalib.visualization.dot.GraphVizSwingVisualizationProvider;

/**
 * This module contains {@link VisualizationProvider}s based on the <a href="http://www.graphviz.org/">GraphVIZ DOT</a>
 * tool.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.automatalib&lt;/groupId&gt;
 *   &lt;artifactId&gt;automata-dot-visualizer&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module net.automatalib.visualization.dot {

    requires java.desktop;

    requires net.automatalib.api;
    requires net.automatalib.common.setting;
    requires net.automatalib.common.util;
    requires net.automatalib.serialization.dot;
    requires org.slf4j;

    // annotations are 'provided'-scoped and need not to be loaded at runtime
    requires static org.checkerframework.checker.qual;
    requires static org.kohsuke.metainf_services;

    exports net.automatalib.visualization.dot;

    provides VisualizationProvider with GraphVizSwingVisualizationProvider, GraphVizBrowserVisualizationProvider;
}
