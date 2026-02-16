/* Copyright (C) 2013-2026 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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

import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.graph.Graph;
import net.automatalib.ts.modal.ModalTransitionSystem;

/**
 * This module contains (de-) serializers for the DOT format. For further information about the DOT format, see <a
 * href="https://graphviz.org/doc/info/lang.html">https://graphviz.org/doc/info/lang.html</a>. Currently, this module
 * supports (de-)serialization of generic {@link UniversalAutomaton}s (such as {@link DFA}s, {@link MealyMachine}s, and
 * {@link MooreMachine}s), {@link Graph}s, {@link ModalTransitionSystem}s, {@link MMLT}s, and
 * {@link ContextFreeModalProcessSystem}s.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.automatalib&lt;/groupId&gt;
 *   &lt;artifactId&gt;automata-serialization-dot&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module net.automatalib.serialization.dot {

    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;

    // annotations are 'provided'-scoped and do not need to be loaded at runtime
    requires static org.checkerframework.checker.qual;

    exports net.automatalib.serialization.dot;
}
