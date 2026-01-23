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
package net.automatalib.common.util.process;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.StringJoiner;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test
public class ProcessUtilTest {

    private final String program;
    private final String path;

    public ProcessUtilTest() throws URISyntaxException {
        this.program = "python";
        this.path = getPathToScript();
    }

    private static String getPathToScript() throws URISyntaxException {
        final URL resource = Objects.requireNonNull(ProcessUtilTest.class.getResource("/process.py"));
        return Paths.get(resource.toURI()).toFile().getAbsolutePath();
    }

    @BeforeTest
    public void setUp() {
        try {
            if (ProcessUtil.invokeProcess(new String[] {program, "--version"}) != 0) {
                throw new SkipException("python not supported");
            }
        } catch (IOException | InterruptedException e) {
            throw new SkipException("python not supported");
        }
    }

    @Test
    public void testReturnValue() throws IOException, InterruptedException {
        Assert.assertEquals(ProcessUtil.invokeProcess(new String[] {program, path, "abc"}), 0);
        Assert.assertEquals(ProcessUtil.invokeProcess(new String[] {program, path, "abc", "def"}), 1);

        Assert.assertEquals(ProcessUtil.invokeProcess(new String[] {program, path}, new StringReader("abc")), 0);
        Assert.assertEquals(ProcessUtil.invokeProcess(new String[] {program, path}, new StringReader("abc def")), 1);
    }

    @Test
    public void testProcessOutput() throws IOException, InterruptedException {
        StringJoiner stdOutJoiner = new StringJoiner("\n");
        StringJoiner stdErrBuilder = new StringJoiner("\n");

        int returnCode =
                ProcessUtil.invokeProcess(new String[] {program, path, "abc"}, stdOutJoiner::add, stdErrBuilder::add);
        Assert.assertEquals(returnCode, 0, stdErrBuilder.toString());
        Assert.assertEquals(stdOutJoiner.toString(), "294");
        Assert.assertEquals(stdErrBuilder.toString(), "abc");

        stdOutJoiner = new StringJoiner("\n");
        stdErrBuilder = new StringJoiner("\n");
        returnCode = ProcessUtil.invokeProcess(new String[] {program, path, "abc", "def"},
                                               stdOutJoiner::add,
                                               stdErrBuilder::add);
        Assert.assertEquals(returnCode, 1, stdErrBuilder.toString());
        Assert.assertEquals(stdOutJoiner.toString(), "294\n303");
        Assert.assertEquals(stdErrBuilder.toString(), "abc\ndef");

        stdOutJoiner = new StringJoiner("\n");
        stdErrBuilder = new StringJoiner("\n");
        returnCode = ProcessUtil.invokeProcess(new String[] {program, path},
                                               new StringReader("abc"),
                                               stdOutJoiner::add,
                                               stdErrBuilder::add);
        Assert.assertEquals(returnCode, 0, stdErrBuilder.toString());
        Assert.assertEquals(stdOutJoiner.toString(), "294");
        Assert.assertEquals(stdErrBuilder.toString(), "abc");

        stdOutJoiner = new StringJoiner("\n");
        stdErrBuilder = new StringJoiner("\n");
        returnCode = ProcessUtil.invokeProcess(new String[] {program, path},
                                               new StringReader("abc def"),
                                               stdOutJoiner::add,
                                               stdErrBuilder::add);
        Assert.assertEquals(returnCode, 1, stdErrBuilder.toString());
        Assert.assertEquals(stdOutJoiner.toString(), "294\n303");
        Assert.assertEquals(stdErrBuilder.toString(), "abc\ndef");

    }
}
