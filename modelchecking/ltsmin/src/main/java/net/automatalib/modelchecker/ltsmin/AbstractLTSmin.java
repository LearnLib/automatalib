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
package net.automatalib.modelchecker.ltsmin;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import net.automatalib.common.setting.AutomataLibSettings;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.common.util.process.ProcessUtil;
import net.automatalib.exception.FormatException;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.modelchecking.ModelChecker;
import net.automatalib.serialization.etf.writer.AbstractETFWriter;
import net.automatalib.serialization.fsm.parser.AbstractFSMParser;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
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
 *         the type of counterexample
 *
 * @see <a href="http://ltsmin.utwente.nl">http://ltsmin.utwente.nl</a>
 * @see AbstractFSMParser
 * @see AbstractETFWriter
 * @see AutomataLibSettings
 */
public abstract class AbstractLTSmin<I, A, R> implements ModelChecker<I, A, String, R>, LTSmin<I, A, R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLTSmin.class);

    private final boolean keepFiles;
    private final Function<String, I> string2Input;

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
     *
     * @see AbstractLTSmin
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod") // it has no semantic impact
    protected AbstractLTSmin(boolean keepFiles, Function<String, I> string2Input) {
        this.keepFiles = keepFiles;
        this.string2Input = string2Input;

        if (!LTSminUtil.supports(getMinimumRequiredVersion())) {
            LOGGER.warn("LTSmin binary could not be detected in the correct version");
        }
    }

    /**
     * Returns the minimum required version of LTSmin.
     *
     * @return the major version.
     */
    protected abstract LTSminVersion getMinimumRequiredVersion(@UnknownInitialization(AbstractLTSmin.class) AbstractLTSmin<I, A, R> this);

    /**
     * Returns the extra command line options that should be given to the etf2lts-mc binary.
     *
     * @return the extra command line options.
     */
    protected abstract List<String> getExtraCommandLineOptions();

    /**
     * This method must verify that the given formula adheres to the expected syntax of the chosen serialization format
     * for hypotheses of {@code this} model-checker.
     *
     * @param formula
     *         the formula to verify
     *
     * @throws FormatException
     *         if the formula does not adhere to the expected syntax
     */
    protected abstract void verifyFormula(String formula) throws FormatException;

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
     * @param hypothesis
     *         the hypothesis to check
     * @param inputs
     *         the inputs which should be regarded for checking
     * @param formula
     *         the formula that should be checked
     *
     * @return a file containing the FSM representation for the found counterexample, or {@code null} if no such
     * counterexample could be found.
     *
     * @see AbstractLTSmin
     */
    protected final @Nullable File findCounterExampleFSM(A hypothesis, Collection<? extends I> inputs, String formula) {

        try {
            verifyFormula(formula);
        } catch (FormatException fe) {
            throw new ModelCheckingException(fe);
        }

        final File etf;

        try {
            // create the ETF that will contain the LTS of the hypothesis
            etf = File.createTempFile("automaton2etf", ".etf");

            try {
                // write to the ETF file
                automaton2ETF(hypothesis, inputs, etf);
            } catch (ModelCheckingException mce) {
                if (!keepFiles && !etf.delete()) {
                    logFileWarning(etf);
                }
                throw mce;
            }
        } catch (IOException ioe) {
            throw new ModelCheckingException(ioe);
        }

        final File ltlFile;

        try {
            // write LTL formula to a file because long formulae may cause problems as direct inputs to LTSmin
            ltlFile = File.createTempFile("formula", ".ltl");

            try (Writer w = IOUtil.asBufferedUTF8Writer(ltlFile)) {
                // write to the file
                w.write(formula);
            } catch (IOException ioe) {
                if (!keepFiles && !ltlFile.delete()) {
                    logFileWarning(ltlFile);
                }
                throw new ModelCheckingException(ioe);
            }
        } catch (IOException ioe) {
            throw new ModelCheckingException(ioe);
        }

        final File gcf;

        try {
            // create the GCF that will possibly contain the counterexample
            gcf = File.createTempFile("etf2gcf", ".gcf");
        } catch (IOException ioe) {
            if (!keepFiles && !etf.delete()) {
                logFileWarning(etf);
            }
            throw new ModelCheckingException(ioe);
        }

        // the command lines for the ProcessBuilder
        final List<String> ltsminCommandLine = CollectionUtil.list(// add the etf2lts-mc binary
                                                                   LTSminUtil.ETF2LTS_MC,
                                                                   // add the ETF file that contains the hypothesis
                                                                   etf.getAbsolutePath(),
                                                                   // add the LTL formula
                                                                   "--ltl=" + ltlFile,
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

        try {
            final int ltsminExitValue = runCommandLine(ltsminCommandLine);

            if (ltsminExitValue == 0) {
                // we have not found a counterexample
                return null;
            } else if (ltsminExitValue == 1) {
                // we have found a counterexample

                // create a file for the FSM
                final File fsm;
                try {
                    fsm = File.createTempFile("gcf2fsm", ".fsm");
                } catch (IOException ioe) {
                    throw new ModelCheckingException(ioe);
                }

                final List<String> convertCommandLine = CollectionUtil.list(// add the ltsmin-convert binary
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
                    throw new ModelCheckingException(
                            "Could not convert GCF to FSM. Enable debug logging to see LTSmin's debug information.");
                }

                return fsm;
            } else {
                throw new ModelCheckingException(
                        "Could not model check ETF. Enable debug logging to see LTSmin's debug information.");
            }
        } finally {
            if (!keepFiles) {
                if (!etf.delete()) {
                    logFileWarning(etf);
                }
                if (!ltlFile.delete()) {
                    logFileWarning(ltlFile);
                }
                if (!gcf.delete()) {
                    logFileWarning(gcf);
                }
            }
        }
    }

    private static int runCommandLine(List<String> commandLine) {
        try {
            LOGGER.debug("Invoking LTSmin binary as: {}", String.join(" ", commandLine));
            return ProcessUtil.invokeProcess(commandLine, LOGGER::debug);
        } catch (IOException | InterruptedException e) {
            throw new ModelCheckingException(e);
        }
    }

    private static void logFileWarning(File file) {
        LOGGER.warn("Could not delete file: '{}'", file.getAbsolutePath());
    }
}
