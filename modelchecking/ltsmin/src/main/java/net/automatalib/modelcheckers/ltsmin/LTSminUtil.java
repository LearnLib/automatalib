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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.regex.Pattern;

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
     * @see #isCheckVersion()
     */
    private static boolean checkVersion;

    /**
     * @see #isVerbose()
     */
    private static boolean verbose;

    /**
     * The pattern for LTSmin versioning scheme: v[major].[minor].[patch].
     */
    public static final Pattern VERSION_PATTERN = Pattern.compile("v[0-9]+\\.[0-9]+\\.[0-9]+");

    static {
        AutomataLibSettings settings = AutomataLibSettings.getInstance();

        final String ltsMinPath = settings.getProperty(AutomataLibProperty.LTSMIN_PATH, "");

        ETF2LTS_MC = Paths.get(ltsMinPath, "etf2lts-mc").toString();
        LTSMIN_CONVERT = Paths.get(ltsMinPath, "ltsmin-convert").toString();

        checkVersion = !settings.getProperty(AutomataLibProperty.LTSMIN_CHECK_VERSION, "true").equalsIgnoreCase("false");

        verbose = !settings.getProperty(AutomataLibProperty.LTSMIN_VERBOSE, LOGGER.isDebugEnabled() ? "true" : "false")
                           .equalsIgnoreCase("false");
    }

    private LTSminUtil() {
        throw new AssertionError();
    }

    /**
     * Whether to perform an LTSmin version check.
     */
    public static boolean isCheckVersion() {
        return checkVersion;
    }

    /**
     * @see #isCheckVersion()
     */
    public static void setCheckVersion(boolean checkVersion) {
        LTSminUtil.checkVersion = checkVersion;
    }

    /**
     * Whether to make LTSmin's output more verbose.
     */
    public static boolean isVerbose() {
        return verbose;
    }

    /**
     * @see #isVerbose()
     */
    public static void setVerbose(boolean verbose) {
        LTSminUtil.verbose = verbose;
    }

    /**
     * Returns an array of length 3 from a version string.
     *
     * The version string should match the regex: "^v?[0-9]+\.[0-9]+\.[0-9]+.*".
     *
     * @param version the version string to split.
     *
     * @return the split version string.
     */
    private static int[] getVersion(String version) {
        final String afterV = version.replace("v", "");

        final String[] versionPart = afterV.split("-");

        assert versionPart.length > 0;

        final String beforeMin = versionPart[0];

        final String[] parts = beforeMin.split("\\.");

        assert parts.length == 3;

        return new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])};
    }

    /**
     * Returns whether the lhs version is greater or equal than the rhs version.
     *
     * @param lhs the left-hand side version.
     * @param rhs the right-hand side version.
     *
     * @return whether lhs >= rhs.
     */
    private static boolean isVersionGreaterOrEqualThan(int[] lhs, int[] rhs) {
        assert lhs.length == 3 && rhs.length == 3;

        boolean isEqual = true;
        boolean isGreater = false;
        for (int i = 0; i < 3 && isEqual; i++) {
            isEqual = lhs[i] == rhs[i];
            isGreater = lhs[i] > rhs[i];
        }

        return isGreater || isEqual;
    }

    /**
     * Checks whether the required binaries for the {@link AbstractLTSmin LTSmin modelchecker} can be executed, by
     * performing a version check.
     *
     * @param major
     *          the major version.
     * @param minor
     *          the minor version.
     * @param patch
     *          the patch version.
     *
     * @return {@code true} if the binary returned with the expected exit value, {@code false} otherwise.
     *
     * @see #ETF2LTS_MC
     * @see #LTSMIN_CONVERT
     */
    public static boolean checkUsable(int major, int minor, int patch) {
        if (checkVersion) {
            return checkUsable(ETF2LTS_MC, major, minor, patch) && checkUsable(LTSMIN_CONVERT, major, minor, patch);
        } else {
            LOGGER.warn("Skipping LTSmin version check!");
            return true;
        }
    }

    /**
     * Checks whether the given binary can be executed, by performing a version check.
     *
     * @param bin
     *         the binary to check.
     * @param major
     *          the major version.
     * @param minor
     *          the minor version.
     * @param patch
     *          the patch version.
     *
     * @return {@code true} if the binary returned with the expected exit value, {@code false} otherwise.
     */
    private static boolean checkUsable(String bin, int major, int minor, int patch) {

        // the command lines for the ProcessBuilder
        final String[] commandLine = new String[] {// add the binary
                                                   bin,
                                                   // just run a version check
                                                   "--version"};
        LOGGER.debug("Checking '{}' for version >= v{}.{}.{}", bin, major, minor, patch);

        final StringWriter stringWriter = new StringWriter();

        try {
            final int exitValue = ProcessUtil.invokeProcess(commandLine, s -> stringWriter.append(s));

            if (exitValue != VERSION_EXIT) {
                LOGGER.error(String.format(CHECK,
                                           bin,
                                           String.format("Command '%s --version' did not exit with 255", bin)));
                return false;
            } else {
                LOGGER.debug("Installed version is {}", stringWriter.toString());

                if (VERSION_PATTERN.matcher(stringWriter.toString()).matches()) {
                    final int[] actualVersion = getVersion(stringWriter.toString());

                    final int[] expectedVersion = new int[] {major, minor, patch};

                    final boolean result = isVersionGreaterOrEqualThan(actualVersion, expectedVersion);
                    if (!result) {
                        LOGGER.warn("Installed version of '{}' is too old: required v{}.{}.{}, but found {}.",
                                    bin,
                                    major,
                                    minor,
                                    patch,
                                    stringWriter.toString());
                    } else {
                        LOGGER.debug("Installed version is okay.");
                    }

                    return result;
                } else {
                    LOGGER.error("Installed LTSmin version does not match v<major>.<minor>.<patch>.");
                    return false;
                }
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error(String.format(CHECK, bin, e.toString()), e);
            return false;
        }
    }
}
