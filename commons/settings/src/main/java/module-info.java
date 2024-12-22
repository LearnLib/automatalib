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

import net.automatalib.common.setting.AutomataLibLocalPropertiesSource;
import net.automatalib.common.setting.AutomataLibPropertiesSource;
import net.automatalib.common.setting.AutomataLibSettingsSource;
import net.automatalib.common.setting.AutomataLibSystemPropertiesSource;

/**
 * This module contains a collection of utility methods to parse AutomataLib specific settings.
 * <p>
 * This module is provided by the following Maven dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;net.automatalib&lt;/groupId&gt;
 *   &lt;artifactId&gt;automata-commons-settings&lt;/artifactId&gt;
 *   &lt;version&gt;${version}&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
open module net.automatalib.common.setting {

    requires net.automatalib.common.util;

    // make non-static once https://github.com/typetools/checker-framework/issues/4559 is implemented
    requires static org.checkerframework.checker.qual;
    requires static org.kohsuke.metainf_services;

    exports net.automatalib.common.setting;

    uses AutomataLibSettingsSource;

    provides AutomataLibSettingsSource with AutomataLibLocalPropertiesSource, AutomataLibPropertiesSource, AutomataLibSystemPropertiesSource;
}
