/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.modelcheckers.ltsmin;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for the {@link LTSminVersion} class.
 *
 * @author frohme
 */
public class LTSminVersionCheck {

    @Test
    public void testParse() {

        final String v300 = "v3.0.0";
        final String v300beta = "v3.0.0-beta1";
        final String v300broken = "v3.0broken.0";

        Assert.assertEquals(LTSminVersion.parse(v300), LTSminVersion.of(3, 0, 0));
        Assert.assertEquals(LTSminVersion.parse(v300beta), LTSminVersion.of(3, 0, 0));
        Assert.assertEquals(LTSminVersion.parse(v300broken), LTSminVersion.of(0, 0, 0));
    }
}
