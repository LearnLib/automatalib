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

/**
 * This module contains the integration of the model checker <a href="https://ltsmin.utwente.nl/">LTSmin</a> as
 * described in the paper <a href="https://doi.org/10.1007/s11334-019-00342-6">Sound Black-Box Checking in the
 * LearnLib</a> by Jeroen Meijer and Jaco van de Pol.
 * <p>
 * Note that this implementation requires a local installation of the <a href="https://ltsmin.utwente.nl/">LTSmin
 * binaries</a> which are not explicitly included in this module due to packaging reasons.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.automatalib&lt;/groupId&gt;
 *   &lt;artifactId&gt;automata-modelchecking-ltsmin&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module net.automatalib.modelchecker.ltsmin {

    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.serialization.fsm;
    requires net.automatalib.serialization.etf;
    requires net.automatalib.util;
    requires org.slf4j;

    requires static de.learnlib.tooling.annotation;
    // make non-static once https://github.com/typetools/checker-framework/issues/4559 is implemented
    requires static org.checkerframework.checker.qual;

    exports net.automatalib.modelchecker.ltsmin;
    exports net.automatalib.modelchecker.ltsmin.ltl;
    exports net.automatalib.modelchecker.ltsmin.monitor;
}
