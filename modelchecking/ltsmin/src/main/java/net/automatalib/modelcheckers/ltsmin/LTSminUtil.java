/* Copyright (C) 2013-2019 TU Dortmund
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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;

import net.automatalib.AutomataLibProperty;
import net.automatalib.AutomataLibSettings;
import net.automatalib.commons.util.process.ProcessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class that encapsulates certain technical aspects of LTSmin (e.g. accessibility of the binary, etc.)
 *
 * @author Jeroen Meijer
 * @author frohme
 */
public final class LTSminUtil {

    /**
     * Path to the "etf2lts-mc" binary.
     */
    public static final String ETF2LTS_MC;

    /**
     * Path to the "ltsmin-convert" binary.
     */
    public static final String LTSMIN_CONVERT;

    private static final Logger LOGGER = LoggerFactory.getLogger(LTSminUtil.class);

    private static final String CHECK = "An exception occurred while checking if LTSmin is installed. " +
                                        "Could not run binary '%s', the following exception occurred: %s. " +
                                        "LTSmin can be obtained at https://ltsmin.utwente.nl. If you installed LTSmin " +
                                        "in a non standard location you can set the property: '" +
                                        AutomataLibProperty.LTSMIN_PATH.getPropertyKey() +
                                        "'. Setting the $PATH variable works too.";

    /**
     * The exit code for running an LTSmin binary with --version.
     */
    private static final int VERSION_EXIT = 255;

    /**
     * A flag for triggering verbose LTSmin output.
     */
    private static boolean verbose;

    private static LTSminVersion detectedVersion;

    static {
        AutomataLibSettings settings = AutomataLibSettings.getInstance();

        final String ltsMinPath = settings.getProperty(AutomataLibProperty.LTSMIN_PATH, "");

        ETF2LTS_MC = Paths.get(ltsMinPath, "etf2lts-mc").toString();
        LTSMIN_CONVERT = Paths.get(ltsMinPath, "ltsmin-convert").toString();

        verbose = !settings.getProperty(AutomataLibProperty.LTSMIN_VERBOSE, Boolean.toString(LOGGER.isDebugEnabled()))
                           .equalsIgnoreCase("false");

        detectLTSmin();
    }

    private LTSminUtil() {
        throw new AssertionError();
    }

    /**
     * Returns whether or not an LTSmin installation was detected.
     *
     * @return {@code true} if an LTSmin installation was detected, {@code false} otherwise.
     */
    public static boolean isInstalled() {
        return detectedVersion != null;
    }

    /**
     * Returns the detected version of the LTSmin installation. {@code null} if LTSmin is not installed.
     *
     * @return the detected version of the LTSmin installation. {@code null} if LTSmin is not installed.
     *
     * @see #isInstalled()
     */
    public static LTSminVersion getVersion() {
        return detectedVersion;
    }

    /**
     * Checks whether the currently detected LTSmin installation supports the queried version.
     *
     * @param requiredVersion
     *         the required version
     *
     * @return {@code true} if LTSmin is installed in the proper version, {@code false} otherwise.
     */
    public static boolean supports(LTSminVersion requiredVersion) {
        return isInstalled() && detectedVersion.supports(requiredVersion);
    }

    /**
     * Returns whether to make LTSmin's output more verbose.
     */
    public static boolean isVerbose() {
        return verbose;
    }

    /**
     * Programmitically set, whether to make LTSmin's output more verbose.
     */
    public static void setVerbose(boolean verbose) {
        LTSminUtil.verbose = verbose;
    }

    /**
     * (Re-)Checks whether the required binaries for the {@link AbstractLTSmin LTSmin modelchecker} can be executed, by
     * performing a version check. The results to these checks can be accessed by {@link #isInstalled()} and {@link
     * #getVersion()}.
     *
     * @see #ETF2LTS_MC
     * @see #LTSMIN_CONVERT
     */
    public static void detectLTSmin() {
        final LTSminVersion etf2ltsVersion = detectLTSmin(ETF2LTS_MC);
        final LTSminVersion ltsminConvertVersion = detectLTSmin(LTSMIN_CONVERT);

        if (etf2ltsVersion != null && ltsminConvertVersion != null) {
            if (!etf2ltsVersion.equals(ltsminConvertVersion)) {
                LOGGER.warn("Found differing etf2lts version '{}' and lstminConver version '{}'. Choosing the former",
                            etf2ltsVersion,
                            ltsminConvertVersion);
            }
            detectedVersion = etf2ltsVersion;
        }
    }

    private static LTSminVersion detectLTSmin(String bin) {

        // the command lines for the ProcessBuilder
        final String[] commandLine = new String[] {// add the binary
                                                   bin,
                                                   // just run a version check
                                                   "--version"};

        final StringWriter stringWriter = new StringWriter();

        try {
            final int exitValue = ProcessUtil.invokeProcess(commandLine, stringWriter::append);

            if (exitValue != VERSION_EXIT) {
                LOGGER.debug(String.format(CHECK,
                                           bin,
                                           String.format("Command '%s --version' did not exit with 255", bin)));
                return null;
            } else {
                return LTSminVersion.parse(stringWriter.toString());
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error(String.format(CHECK, bin, e.toString()), e);
            return null;
        }
    }
}
