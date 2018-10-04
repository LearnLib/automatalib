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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import net.automatalib.AutomataLibSettings;
import net.automatalib.commons.util.process.ProcessUtil;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.modelchecking.ModelChecker;
import net.automatalib.serialization.etf.writer.AbstractETFWriter;
import net.automatalib.serialization.fsm.parser.AbstractFSMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An LTL model checker using LTSmin.
 * <p>
 * The user must install LTSmin in order for {@link AbstractLTSmin} to run without exceptions. Once LTSmin is
 * installed the user may specify the path to the installed LTSmin binaries with the property
 * <b>automatalib.ltsmin.path</b>. If this property is not set the binaries will be run as usual (e.g. simply
 * by invoking etf2lts-mc, and ltsmin-convert), which means the user can also specify the location of the binaries in
 * the PATH environment variable.
 * <p>
 * This model checker is implemented as follows. The hypothesis automaton is first written to an LTS in ETF {@link
 * AbstractETFWriter} file, which serves as input for the etf2lts-mc binary. Then the etf2lts-mc binary is run, which
 * will write an LTS in GCF format. This LTS will be a subset of the language of the given hypothesis. Next, the GCF is
 * converted to FSM using the ltsmin-convert binary. Lastly, the FSM is read back into an automaton using an {@link
 * AbstractFSMParser}.
 *
 * @param <I>
 *         the input type.
 * @param <A>
 *         the automaton type.
 * @param <R>
 *         the type of a counterexample
 *
 * @author Jeroen Meijer
 * @see <a href="http://ltsmin.utwente.nl">http://ltsmin.utwente.nl</a>
 * @see AbstractFSMParser
 * @see AbstractETFWriter
 * @see AutomataLibSettings
 */
public abstract class AbstractLTSmin<I, A, R> implements ModelChecker<I, A, String, R>, LTSmin<I, A, R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLTSmin.class);

    /**
     * @see #isKeepFiles()
     */
    private final boolean keepFiles;

    /**
     * @see #getString2Input()
     */
    private final Function<String, I> string2Input;

    /**
     * @see #isAvailable()
     */
    private static Boolean isAvailable;

    /**
     * The version for which we last checked availability.
     */
    private static int[] availableVersion;

    /**
     * Constructs a new AbstractLTSmin.
     *
     * @param keepFiles
     *         whether to keep intermediate files, (e.g. etfs, gcfs etc.).
     * @param string2Input
     *         a function that transforms edges in FSM files to actual input.
     *
     * @throws ModelCheckingException
     *         when the LTSmin binaries can not be run successfully.
     */
    protected AbstractLTSmin(boolean keepFiles,
                             Function<String, I> string2Input) throws ModelCheckingException {
        this.keepFiles = keepFiles;
        this.string2Input = string2Input;

        if (!isAvailable()) {
            throw new ModelCheckingException(
                    String.format(
                            "LTSmin binary could not be executed correctly, minimum version required is v%d.%d.%d",
                            getMinimumMajorVersion(), getMinimumMinorVersion(), getMinimumPatchVersion()));
        }
    }

    private static void setAvailableVersion(int[] availableVersion) {
        AbstractLTSmin.availableVersion = availableVersion;
    }

    private static void setIsAvailable(Boolean isAvailable) {
        AbstractLTSmin.isAvailable = isAvailable;
    }

    /**
     * Returns the minimum required major version of LTSmin.
     *
     * @return the major version.
     */
    protected abstract int getMinimumMajorVersion();

    /**
     * Returns the minimum required minor version of LTSmin.
     *
     * @return the minor version.
     */
    protected abstract int getMinimumMinorVersion();

    /**
     * Returns the minimum required patch version of LTSmin.
     *
     * @return the patch version.
     */
    protected abstract int getMinimumPatchVersion();

    /**
     * Returns the extra command line options that should be given to the etf2lts-mc binary.
     *
     * @return the extra command line options.
     */
    protected abstract List<String> getExtraCommandLineOptions();

    /**
     * Returns whether the LTSmin binaries are available, and match the minimum required version.
     *
     * @see #getMinimumMajorVersion()
     * @see #getMinimumMinorVersion()
     * @see #getMinimumPatchVersion()
     *
     * @return whether the binaries are available.
     */
    public boolean isAvailable() {
        if (isAvailable == null || availableVersion == null || !Arrays.equals(availableVersion, new int[] {
                getMinimumMajorVersion(), getMinimumMinorVersion(), getMinimumPatchVersion()})) {

            setIsAvailable(LTSminUtil.checkUsable(getMinimumMajorVersion(), getMinimumMinorVersion(),
                                                 getMinimumPatchVersion()));

            if (isAvailable) {
                setAvailableVersion(new int[] {getMinimumMajorVersion(), getMinimumMinorVersion(), getMinimumPatchVersion()});
            } else {
                setAvailableVersion(null);
            }
        }

        return isAvailable;
    }

    @Override
    public boolean isKeepFiles() {
        return keepFiles;
    }

    @Override
    public Function<String, I> getString2Input() {
        return string2Input;
    }

    /**
     * Finds a counterexample for the given {@code formula}, and given {@code hypothesis} in FSM format.
     *
     * @see AbstractLTSmin
     */
    protected final File findCounterExampleFSM(A hypothesis, Collection<? extends I> inputs, String formula)
            throws ModelCheckingException {

        final File etf, gcf;
        try {
            // create the ETF that will contain the LTS of the hypothesis
            etf = File.createTempFile("automaton2etf", ".etf");

            // create the GCF that will possibly contain the counterexample
            gcf = File.createTempFile("etf2gcf", ".gcf");

            // write to the ETF file
            automaton2ETF(hypothesis, inputs, etf);

        } catch (IOException ioe) {
            throw new ModelCheckingException(ioe);
        }

        // the command lines for the ProcessBuilder
        final List<String> ltsminCommandLine = Lists.newArrayList(// add the etf2lts-mc binary
                                                                  LTSminUtil.ETF2LTS_MC,
                                                                  // add the ETF file that contains the hypothesis
                                                                  etf.getAbsolutePath(),
                                                                  // add the LTL formula
                                                                  "--ltl=" + formula,
                                                                  // write the trace to this file
                                                                  "--trace=" + gcf.getAbsolutePath(),
                                                                  // use only one thread (hypotheses are always small)
                                                                  "--threads=1",
                                                                  // use LTSmin LTL semantics
                                                                  "--ltl-semantics=ltsmin",
                                                                  // do not abort on partial LTSs
                                                                  "--allow-undefined-edges");

        if (LTSminUtil.isVerbose()) {
            ltsminCommandLine.add("-v");
        }

        ltsminCommandLine.addAll(getExtraCommandLineOptions());

        final int ltsminExitValue = runCommandLine(ltsminCommandLine);

        // check if we need to delete the ETF
        if (!keepFiles && !etf.delete()) {
            throw new ModelCheckingException("Could not delete file: " + etf.getAbsolutePath());
        }

        final File fsm;

        if (ltsminExitValue == 1) {
            // we have found a counterexample

            try {
                // create a file for the FSM
                fsm = File.createTempFile("gcf2fsm", ".fsm");
            } catch (IOException ioe) {
                throw new ModelCheckingException(ioe);
            }

            final List<String> convertCommandLine = Lists.newArrayList(// add the ltsmin-convert binary
                                                                       LTSminUtil.LTSMIN_CONVERT,
                                                                       // use the GCF as input
                                                                       gcf.getAbsolutePath(),
                                                                       // use the FSM as output
                                                                       fsm.getAbsolutePath(),
                                                                       // required option
                                                                       "--rdwr");

            if (LTSminUtil.isVerbose()) {
                convertCommandLine.add("-v");
            }

            final int convertExitValue = runCommandLine(convertCommandLine);

            // check the conversion is successful
            if (convertExitValue != 0) {
                final String msg;
                if (LOGGER.isDebugEnabled()) {
                    msg = "Could not convert GCF to FSM, please check LTSmin's debug information to see why.";
                } else {
                    msg = "Could not convert GCF to FSM, to see why, enable debug logging.";
                }
                throw new ModelCheckingException(msg);
            }
        } else if (ltsminExitValue != 0) {
            final String msg;
            if (LOGGER.isDebugEnabled()) {
                msg = "Could not model check ETF, please check LTSmin's debug information to see why.";
            } else {
                msg = "Could not model check ETF, to see why, enable debug logging.";
            }
            throw new ModelCheckingException(msg);
        } else {
            fsm = null;
        }

        // check if we must keep the GCF
        if (!keepFiles && !gcf.delete()) {
            throw new ModelCheckingException("Could not delete file: " + gcf.getAbsolutePath());
        }

        return fsm;
    }

    static int runCommandLine(List<String> commandLine) throws ModelCheckingException {
        final String[] commands = new String[commandLine.size()];
        commandLine.toArray(commands);
        try {
            LOGGER.debug("Invoking LTSmin binary as: {}", String.join(" ", commands));
            return ProcessUtil.invokeProcess(commands, LOGGER::debug);
        } catch (IOException | InterruptedException e) {
            throw new ModelCheckingException(e);
        }
    }

    public static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        public static boolean keepFiles() {
            return false;
        }
    }
}
