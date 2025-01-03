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

open module net.automatalib.example {

    requires java.desktop;

    requires net.automatalib.api;
    requires net.automatalib.brics;
    requires net.automatalib.common.setting;
    requires net.automatalib.common.util;
    requires net.automatalib.core;
    requires net.automatalib.incremental;
    requires net.automatalib.modelchecker.ltsmin;
    requires net.automatalib.modelchecker.m3c;
    requires net.automatalib.serialization.dot;
    requires net.automatalib.util;
    requires net.automatalib.visualization.dot;
    requires org.slf4j;

    // provided dependency
    requires static automaton;
    // annotations are 'provided'-scoped and need not to be loaded at runtime
    requires static org.checkerframework.checker.qual;
}
