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
package net.automatalib.example;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import net.automatalib.common.util.system.JVMUtil;
import net.automatalib.example.ads.ADSExample;
import net.automatalib.example.brics.SimpleBricsExample;
import net.automatalib.example.dot.DOTExample;
import net.automatalib.example.graph.DFSExample;
import net.automatalib.example.incremental.IncrementalDFAExample;
import net.automatalib.example.incremental.IncrementalMealyExample;
import net.automatalib.example.incremental.IncrementalPCDFAExample;
import net.automatalib.example.modelchecking.LTSminExample;
import net.automatalib.example.modelchecking.LTSminMonitorExample;
import net.automatalib.example.modelchecking.M3CSPAExample;
import net.automatalib.example.procedural.PalindromeExample;
import net.automatalib.example.vpa.OneSEVPAExample;
import net.automatalib.exception.FormatException;
import net.automatalib.modelchecker.ltsmin.LTSminUtil;
import net.automatalib.modelchecker.ltsmin.LTSminVersion;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Run the examples as part of (integration) testing.
 */
public class ExamplesTest {

    @BeforeClass
    public void setupAutoClose() {
        if (isJVMCompatible()) {
            // As soon as we observe an event that indicates a new window, close it to prevent blocking the tests.
            Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
                final WindowEvent windowEvent = (WindowEvent) event;
                final Window w = windowEvent.getWindow();
                w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
            }, AWTEvent.WINDOW_FOCUS_EVENT_MASK);
        }
    }

    @Test
    public void testADSExample() throws InvocationTargetException, InterruptedException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> ADSExample.main(new String[0]));
    }

    @Test
    public void testSimpleBricsExample() throws InvocationTargetException, InterruptedException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> SimpleBricsExample.main(new String[0]));
    }

    @Test
    public void testDOTExample() throws InvocationTargetException, InterruptedException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> {
            try {
                DOTExample.main(new String[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testDFSExample() throws InvocationTargetException, InterruptedException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> DFSExample.main(new String[0]));
    }

    @Test
    public void testIncrementalDFAExample() throws InvocationTargetException, InterruptedException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> IncrementalDFAExample.main(new String[0]));
    }

    @Test
    public void testIncrementalMealyExample() throws InvocationTargetException, InterruptedException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> IncrementalMealyExample.main(new String[0]));
    }

    @Test
    public void testIncrementalPCDFAExample() throws InvocationTargetException, InterruptedException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> IncrementalPCDFAExample.main(new String[0]));
    }

    @Test
    public void testLTSminExample() {
        if (!LTSminUtil.supports(LTSminVersion.of(3, 0, 0))) {
            throw new SkipException("LTSmin is not installed in the proper version");
        }

        LTSminExample.main(new String[0]);
    }

    @Test
    public void testLTSminMonitorExample() {
        if (!LTSminUtil.supports(LTSminVersion.of(3, 1, 0))) {
            throw new SkipException("LTSmin is not installed in the proper version");
        }

        LTSminMonitorExample.main(new String[0]);
    }

    @Test
    public void testM3CSPAExample() throws InterruptedException, InvocationTargetException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> {
            try {
                M3CSPAExample.main(new String[0]);
            } catch (FormatException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testPalindromeExample() throws InvocationTargetException, InterruptedException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> PalindromeExample.main(new String[0]));
    }

    @Test
    public void testOneSEVPAExample() throws InvocationTargetException, InterruptedException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> OneSEVPAExample.main(new String[0]));
    }

    private static boolean isJVMCompatible() {
        return JVMUtil.getCanonicalSpecVersion() == 11;
    }

    private static void requireJVMCompatibility() {
        if (!isJVMCompatible()) {
            throw new SkipException("The headless AWT environment currently only works with Java 11 or <=8");
        }
    }

}
