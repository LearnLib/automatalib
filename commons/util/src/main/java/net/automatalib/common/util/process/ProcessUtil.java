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
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.process.InputStreamConsumer.CopyConsumer;
import net.automatalib.common.util.process.InputStreamConsumer.DelegatingConsumer;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class for invoking system processes.
 */
public final class ProcessUtil {

    private ProcessUtil() {
        // prevent instantiation
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Discards any output of the process.
     *
     * @param commandLine
     *         the list of command line arguments to run
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(String[] commandLine) throws IOException, InterruptedException {
        return invokeProcess(Arrays.asList(commandLine));
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Discards any output of the process.
     *
     * @param commandLine
     *         the list of command line arguments to run
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(List<String> commandLine) throws IOException, InterruptedException {
        return invokeProcess(commandLine, null, OutputStream.nullOutputStream(), OutputStream.nullOutputStream());
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Additionally, allows one to supply an input stream to the invoked program. Discards any output of the
     * process.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param input
     *         the input passed to the program
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs, or writing the process' inputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(String[] commandLine, Reader input) throws IOException, InterruptedException {
        return invokeProcess(Arrays.asList(commandLine), input);
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Additionally, allows one to supply an input stream to the invoked program. Discards any output of the
     * process.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param input
     *         the input passed to the program
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs, or writing the process' inputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(List<String> commandLine, Reader input) throws IOException, InterruptedException {
        return invokeProcess(commandLine, input, OutputStream.nullOutputStream(), OutputStream.nullOutputStream());
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Outputs of the process (normal and error) are passed to the respective {@code consumer}s.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param stdOutConsumer
     *         the consumer for the program's standard output
     * @param stdErrConsumer
     *         the consumer for the program's error output
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(String[] commandLine,
                                    Consumer<String> stdOutConsumer,
                                    Consumer<String> stdErrConsumer) throws IOException, InterruptedException {
        return invokeProcess(Arrays.asList(commandLine), stdOutConsumer, stdErrConsumer);
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Outputs of the process (normal and error) are passed to the respective {@code consumer}s such that each
     * line results in a separate call to {@link Consumer#accept(Object)}.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param stdOutConsumer
     *         the consumer for the program's standard output
     * @param stdErrConsumer
     *         the consumer for the program's error output
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(List<String> commandLine,
                                    Consumer<String> stdOutConsumer,
                                    Consumer<String> stdErrConsumer) throws IOException, InterruptedException {
        return invokeProcess(commandLine, null, stdOutConsumer, stdErrConsumer);
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Additionally, allows one to supply an input stream to the invoked program. Outputs of the process
     * (normal and error) are passed to the respective {@code consumer}s such that each line results in a separate call
     * to {@link Consumer#accept(Object)}.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param input
     *         the input passed to the program
     * @param stdOutConsumer
     *         the consumer for the program's standard output
     * @param stdErrConsumer
     *         the consumer for the program's error output
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs, or writing the process' inputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(String[] commandLine,
                                    Reader input,
                                    Consumer<String> stdOutConsumer,
                                    Consumer<String> stdErrConsumer) throws IOException, InterruptedException {
        return invokeProcess(Arrays.asList(commandLine), input, stdOutConsumer, stdErrConsumer);
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Additionally, allows one to supply an input stream to the invoked program. Outputs of the process
     * (normal and error) are passed to the respective {@code consumer}s such that each line results in a separate call
     * to {@link Consumer#accept(Object)}.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param input
     *         the input passed to the program
     * @param stdOutConsumer
     *         the consumer for the program's standard output
     * @param stdErrConsumer
     *         the consumer for the program's error output
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs, or writing the process' inputs
     * @throws InterruptedException
     *         if the process is interrupted prior to finishing
     */
    public static int invokeProcess(List<String> commandLine,
                                    @Nullable Reader input,
                                    Consumer<String> stdOutConsumer,
                                    Consumer<String> stdErrConsumer) throws IOException, InterruptedException {
        return invokeProcess(commandLine,
                             input,
                             new DelegatingConsumer(stdOutConsumer),
                             new DelegatingConsumer(stdErrConsumer));
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Additionally, allows one to supply an input stream to the invoked program. Outputs of the process
     * (normal and error) are passed to the respective {@code consumer}s.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param input
     *         the input passed to the program
     * @param stdOutConsumer
     *         the consumer for the program's standard output
     * @param stdErrConsumer
     *         the consumer for the program's error output
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs, or writing the process' inputs
     * @throws InterruptedException
     *         if the process is interrupted prior to finishing
     */
    public static int invokeProcess(String[] commandLine,
                                    Reader input,
                                    OutputStream stdOutConsumer,
                                    OutputStream stdErrConsumer) throws IOException, InterruptedException {
        return invokeProcess(Arrays.asList(commandLine), input, stdOutConsumer, stdErrConsumer);
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Additionally, allows one to supply an input stream to the invoked program. Outputs of the process
     * (normal and error) are passed to the respective {@code consumer}s.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param input
     *         the input passed to the program
     * @param stdOutConsumer
     *         the consumer for the program's standard output
     * @param stdErrConsumer
     *         the consumer for the program's error output
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs, or writing the process' inputs
     * @throws InterruptedException
     *         if the process is interrupted prior to finishing
     */
    public static int invokeProcess(List<String> commandLine,
                                    @Nullable Reader input,
                                    OutputStream stdOutConsumer,
                                    OutputStream stdErrConsumer) throws IOException, InterruptedException {
        return invokeProcess(commandLine, input, new CopyConsumer(stdOutConsumer), new CopyConsumer(stdErrConsumer));
    }

    private static int invokeProcess(List<String> commandLine,
                                     @Nullable Reader input,
                                     InputStreamConsumer stdOutConsumer,
                                     InputStreamConsumer stdErrConsumer) throws IOException, InterruptedException {

        final ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        final Process process = processBuilder.start();

        final Thread stdOutThread = new StreamGobbler(process.getInputStream(), stdOutConsumer);
        final Thread stdErrThread = new StreamGobbler(process.getErrorStream(), stdErrConsumer);

        // consume process outputs to prevent blocking from full buffers
        stdOutThread.start();
        stdErrThread.start();

        writeProcessInput(process, input);

        try {
            final int exitVal = process.waitFor();

            // Handle situation where the process ends before the threads finish
            stdOutThread.join();
            stdErrThread.join();

            return exitVal;
        } finally {
            // cleanup
            process.destroy();
        }
    }

    private static void writeProcessInput(Process process, @Nullable Reader input) throws IOException {
        if (input != null) {
            try (OutputStream processInput = process.getOutputStream();
                 Writer writer = IOUtil.asBufferedUTF8Writer(processInput)) {
                input.transferTo(writer);
            }
        }
    }

}
