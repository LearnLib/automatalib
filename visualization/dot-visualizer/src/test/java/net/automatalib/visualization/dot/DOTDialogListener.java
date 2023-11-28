/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.visualization.dot;

import net.automatalib.common.util.system.JVMUtil;
import org.assertj.swing.testng.testcase.AssertJSwingTestngTestCase;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;

/**
 * This listener fully skips GUI tests including the (otherwise un-skipable)
 * {@link AssertJSwingTestngTestCase#setUpOnce} method.
 */
public class DOTDialogListener implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!(DOT.checkUsable() && (JVMUtil.getCanonicalSpecVersion() == 11))) {
            testResult.setThrowable(new SkipException(
                    "Either DOT is not available or the headless AWT environment is not supported"));
            testResult.setStatus(ITestResult.SKIP);
        }
    }
}
