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
 * This module contains the implementation of the model checker presented in the paper <a
 * href="https://dx.doi.org/10.1007/978-3-030-00244-2_15">M3C: Modal Meta Model Checking</a> by Bernhard Steffen and
 * Alnis Murtovi. The paper is based on <a href="https://link.springer.com/chapter/10.1007/BFb0084787">Model Checking
 * for Context-Free Processes</a> by Olaf Burkart and Bernhard Steffen.
 * <p>
 * Note that this implementation requires a runtime dependency to a specific ADDLib backend (see
 * <a href="https://add-lib.scce.info/">https://add-lib.scce.info/</a>), which is not explicitly included in this
 * artifact due to packaging reasons.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.automatalib&lt;/groupId&gt;
 *   &lt;artifactId&gt;automata-modelchecking-m3c&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module net.automatalib.modelchecker.m3c {

    requires java.xml;

    requires addlib.core;
    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires org.checkerframework.checker.qual;

    exports net.automatalib.modelchecker.m3c.formula;
    exports net.automatalib.modelchecker.m3c.formula.ctl;
    exports net.automatalib.modelchecker.m3c.formula.modalmu;
    exports net.automatalib.modelchecker.m3c.formula.parser;
    exports net.automatalib.modelchecker.m3c.formula.visitor;
    exports net.automatalib.modelchecker.m3c.solver;
    exports net.automatalib.modelchecker.m3c.transformer;
    exports net.automatalib.modelchecker.m3c.visualization;
}
