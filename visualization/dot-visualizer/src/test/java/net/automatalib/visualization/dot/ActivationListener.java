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
package net.automatalib.visualization.dot;

import com.github.caciocavallosilano.cacio.ctc.junit.CacioExtension;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;

/**
 * This listener checks whether GUI tests can be executed and if so, sets up the necessary headless environments.
 */
public class ActivationListener implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (TestUtil.shouldRunGUITests()) {
            // hack: the static initializer of this class does the magic we want, so only invoke it on compatible JVMs
            new CacioExtension();
        } else {
            testResult.setThrowable(new SkipException(
                    "Either DOT is not available or the headless AWT environment is not supported"));
            testResult.setStatus(ITestResult.SKIP);
        }
    }
}
