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

import net.automatalib.common.util.setting.SettingsSource;

/**
 * This module contains several useful classes to ease everyday programming.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.automatalib&lt;/groupId&gt;
 *   &lt;artifactId&gt;automata-commons-util&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module net.automatalib.common.util {

    requires java.management;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    exports net.automatalib.common.util;
    exports net.automatalib.common.util.array;
    exports net.automatalib.common.util.collection;
    exports net.automatalib.common.util.comparison;
    exports net.automatalib.common.util.concurrent;
    exports net.automatalib.common.util.exception;
    exports net.automatalib.common.util.fixpoint;
    exports net.automatalib.common.util.function;
    exports net.automatalib.common.util.io;
    exports net.automatalib.common.util.lib;
    exports net.automatalib.common.util.mapping;
    exports net.automatalib.common.util.math;
    exports net.automatalib.common.util.nid;
    exports net.automatalib.common.util.process;
    exports net.automatalib.common.util.random;
    exports net.automatalib.common.util.ref;
    exports net.automatalib.common.util.setting;
    exports net.automatalib.common.util.string;
    exports net.automatalib.common.util.system;

    uses SettingsSource;
}
