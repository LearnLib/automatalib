/* Copyright (C) 2013-2025 TU Dortmund University
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

/**
 * This module contains deserializers for the Finite State Machine (FSM) format as used by LTSmin. For further
 * information about the FSM format, see the <a
 * href="https://mcrl2.org/web/user_manual/tools/lts.html#language-fsm-lts">mcrl2 documentation</a>.
 * <p>
 * Note that this format differs from the identically named FSM format of <a
 * href="https://github.com/katef/libfsm">libfsm</a>.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.automatalib&lt;/groupId&gt;
 *   &lt;artifactId&gt;automata-serialization-fsm&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module net.automatalib.serialization.fsm {

    requires net.automatalib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.core;

    // annotations are 'provided'-scoped and do not need to be loaded at runtime
    requires static org.checkerframework.checker.qual;

    exports net.automatalib.serialization.fsm.parser;
}
