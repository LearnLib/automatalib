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
package net.automatalib.visualization.dot;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import net.automatalib.common.util.Pair;
import net.automatalib.common.util.system.JVMUtil;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DOTMultiDialogTest {

    @BeforeClass
    public void checkDOT() {
        if (!DOT.checkUsable()) {
            // Do not fail on platforms, where DOT is not installed
            throw new SkipException("DOT is not installed");
        }
    }

    // Headless GUI testing is a pain. Therefore, just check that we don't throw any exceptions for now.
    @Test(timeOut = 30000)
    public void testFrame() throws InvocationTargetException, InterruptedException {

        if (!(JVMUtil.getCanonicalSpecVersion() == 11)) {
            throw new SkipException("The headless AWT environment currently only works with Java 11 or <=8");
        }

        final Random r = new Random(42);
        final List<Pair<String, String>> graphs =
                Arrays.asList(Pair.of("Automaton 1", TestUtil.generateRandomAutomatonDot(r)),
                              Pair.of("Automaton 2", TestUtil.generateRandomAutomatonDot(r)),
                              Pair.of("Automaton 3", TestUtil.generateRandomAutomatonDot(r)));

        // invokeAndWait so that TestNG doesn't kill our GUI thread that we want to check.
        SwingUtilities.invokeAndWait(() -> {
            try {
                DOT.renderDOTStrings(graphs, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Headless GUI testing is a pain. Therefore, just check that we don't throw any exceptions for now.
    @Test(timeOut = 30000)
    public void testEmptyFrame() throws InvocationTargetException, InterruptedException {

        if (!(JVMUtil.getCanonicalSpecVersion() == 11)) {
            throw new SkipException("The headless AWT environment currently only works with Java 11 or <=8");
        }

        // invokeAndWait so that TestNG doesn't kill our GUI thread that we want to check.
        SwingUtilities.invokeAndWait(() -> {
            try {
                DOT.renderDOTReaders(Collections.emptyList(), false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
