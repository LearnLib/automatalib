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
package net.automatalib.common.setting;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class AutomataLibSettingsTest {

    @BeforeSuite
    public void setUp() {
        System.setProperty(AutomataLibProperty.WORD_EMPTY_REP.getPropertyKey(), "OVERRIDDEN");
    }

    @Test
    public void testProperties() {
        AutomataLibSettings settings = AutomataLibSettings.getInstance();

        for (AutomataLibProperty p : AutomataLibProperty.values()) {
            switch (p) {
                case DOT_EXE_DIR:
                    Assert.assertEquals(settings.getProperty(AutomataLibProperty.DOT_EXE_DIR), "dot");
                    break;
                case DOT_EXE_NAME:
                    Assert.assertEquals(settings.getProperty(AutomataLibProperty.DOT_EXE_NAME), "dot.exe");
                    break;
                case LTSMIN_PATH:
                    Assert.assertEquals(settings.getProperty(AutomataLibProperty.LTSMIN_PATH), "ltsmin");
                    break;
                case LTSMIN_VERBOSE:
                    Assert.assertEquals(settings.getProperty(AutomataLibProperty.LTSMIN_VERBOSE), "false");
                    break;
                case VISUALIZATION_PROVIDER:
                    Assert.assertEquals(settings.getProperty(AutomataLibProperty.VISUALIZATION_PROVIDER), "provider");
                    break;
                case WORD_DELIM_LEFT:
                    Assert.assertEquals(settings.getProperty(AutomataLibProperty.WORD_DELIM_LEFT), "delim_left");
                    break;
                case WORD_DELIM_RIGHT:
                    Assert.assertEquals(settings.getProperty(AutomataLibProperty.WORD_DELIM_RIGHT), "delim_right");
                    break;
                case WORD_EMPTY_REP:
                    Assert.assertEquals(settings.getProperty(AutomataLibProperty.WORD_EMPTY_REP), "OVERRIDDEN");
                    break;
                case WORD_SYMBOL_DELIM_LEFT:
                    Assert.assertEquals(settings.getProperty(AutomataLibProperty.WORD_SYMBOL_DELIM_LEFT),
                                        "symbol_delim_left");
                    break;
                case WORD_SYMBOL_DELIM_RIGHT:
                    Assert.assertEquals(settings.getProperty(AutomataLibProperty.WORD_SYMBOL_DELIM_RIGHT),
                                        "symbol_delim_right");
                    break;
                case WORD_SYMBOL_SEPARATOR:
                    Assert.assertEquals(settings.getProperty(AutomataLibProperty.WORD_SYMBOL_SEPARATOR), "symbol_sep");
                    break;
                default:
                    throw new IllegalStateException("Unhandled property " + p);

            }
        }

    }
}
