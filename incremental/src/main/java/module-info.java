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

import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;

/**
 * This module contains algorithms for incrementally constructing {@link DFA DFAs} (prefix-closed and
 * non-prefix-closed), {@link MealyMachine Mealy machines}, and {@link MooreMachine Moore machines} from a finite,
 * incrementally growing set of example inputs/outputs.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.automatalib&lt;/groupId&gt;
 *   &lt;artifactId&gt;automata-incremental&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module net.automatalib.incremental {

    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.util;
    requires org.checkerframework.checker.qual;

    exports net.automatalib.incremental;
    exports net.automatalib.incremental.dfa;
    exports net.automatalib.incremental.dfa.dag;
    exports net.automatalib.incremental.dfa.tree;
    exports net.automatalib.incremental.mealy;
    exports net.automatalib.incremental.mealy.dag;
    exports net.automatalib.incremental.mealy.tree;
    exports net.automatalib.incremental.moore;
    exports net.automatalib.incremental.moore.dag;
    exports net.automatalib.incremental.moore.tree;
}
