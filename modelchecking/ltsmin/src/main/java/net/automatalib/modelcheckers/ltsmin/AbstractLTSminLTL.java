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
import java.util.Collection;
import java.util.function.Function;

import net.automatalib.AutomataLibSettings;
import net.automatalib.automata.concepts.Output;
import net.automatalib.commons.util.process.ProcessUtil;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.modelchecker.AbstractUnfoldingModelChecker;
import net.automatalib.serialization.etf.writer.AbstractETFWriter;
import net.automatalib.serialization.fsm.parser.AbstractFSMParser;
import net.automatalib.ts.simple.SimpleDTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An LTL model checker using LTSmin.
 * <p>
 * The user must install LTSmin in order for {@link AbstractLTSminLTL} to run without exceptions. Once LTSmin is
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
 * @param <L>
 *         the Lasso type.
 *
 * @author Jeroen Meijer
 * @see <a href="http://ltsmin.utwente.nl">http://ltsmin.utwente.nl</a>
 * @see AbstractFSMParser
 * @see AbstractETFWriter
 * @see AutomataLibSettings
 */
public abstract class AbstractLTSminLTL<I, A extends SimpleDTS<?, I> & Output<I, ?>, L extends Lasso<I, ?>>
        extends AbstractUnfoldingModelChecker<I, A, String, L> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLTSminLTL.class);

    /**
     * @see #isKeepFiles()
     */
    private final boolean keepFiles;

    /**
     * @see #getString2Input()
     */
    private final Function<String, I> string2Input;

    /**
     * Constructs a new AbstractLTSminLTL.
     *
     * @param keepFiles
     *         whether to keep intermediate files, (e.g. etfs, gcfs etc.).
     * @param string2Input
     *         a function that transforms edges in FSM files to actual input.
     * @param minimumUnfolds
     *         the minimum number of unfolds.
     * @param multiplier
     *         the multiplier
     *
     * @throws ModelCheckingException
     *         when the LTSmin binaries can not be run successfully.
     */
    protected AbstractLTSminLTL(boolean keepFiles,
                                Function<String, I> string2Input,
                                int minimumUnfolds,
                                double multiplier) throws ModelCheckingException {
        super(minimumUnfolds, multiplier);
        this.keepFiles = keepFiles;
        this.string2Input = string2Input;

        if (!LazyBinaryChecker.AVAILABLE) {
            throw new ModelCheckingException("LTSmin binary could not be executed correctly");
        }
    }

    /**
     * Returns whether intermediate files should be kept, e.g. etfs, gcfs, etc.
     *
     * @return the boolean
     */
    protected boolean isKeepFiles() {
        return keepFiles;
    }

    /**
     * Returns the function that transforms edges in FSM files to actual input.
     *
     * @return the Function.
     */
    public Function<String, I> getString2Input() {
        return string2Input;
    }

    /**
     * Writes the given {@code automaton} to the given {@code etf} file.
     *
     * @param automaton
     *         the automaton to write.
     * @param inputs
     *         the alphabet.
     * @param etf
     *         the file to write to.
     *
     * @throws IOException
     *         when the given {@code automaton} can not be written to {@code etf}.
     */
    protected abstract void automaton2ETF(A automaton, Collection<? extends I> inputs, File etf) throws IOException;

    /**
     * Finds a counterexample for the given {@code formula}, and given {@code hypothesis} in FSM format.
     *
     * @see AbstractLTSminLTL
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
        final String[] ltsminCommandLine = new String[] {// add the etf2lts-mc binary
                                                         LTSminUtil.ETF2LTS_MC,
                                                         // add the ETF file that contains the LTS of the hypothesis
                                                         etf.getAbsolutePath(),
                                                         // add the LTL formula
                                                         "--ltl=" + formula,
                                                         // use Buchi automata created by spot
                                                         "--buchi-type=spotba",
                                                         // use the Union-Find strategy
                                                         "--strategy=ufscc",
                                                         // write the lasso to this file
                                                         "--trace=" + gcf.getAbsolutePath(),
                                                         // use only one thread (hypotheses are always small)
                                                         "--threads=1",
                                                         // use LTSmin LTL semantics
                                                         "--ltl-semantics=ltsmin",
                                                         // do not abort on partial LTSs
                                                         "--allow-undefined-edges"};

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

            final String[] convertCommandLine = new String[] {// add the ltsmin-convert binary
                                                              LTSminUtil.LTSMIN_CONVERT,
                                                              // use the GCF as input
                                                              gcf.getAbsolutePath(),
                                                              // use the FSM as output
                                                              fsm.getAbsolutePath(),
                                                              // required option
                                                              "--rdwr"};

            final int convertExitValue = runCommandLine(convertCommandLine);

            // check the conversion is successful
            if (convertExitValue != 0) {
                throw new ModelCheckingException("Could not convert gcf to fsm");
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

    private static int runCommandLine(String[] commandLine) throws ModelCheckingException {
        try {
            return ProcessUtil.invokeProcess(commandLine, LOGGER::debug);
        } catch (IOException | InterruptedException e) {
            throw new ModelCheckingException(e);
        }
    }

    /**
     * Lazy holder for checking availability of LTSMin binary. See
     * <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">
     * Initialization-on-demand holder idiom</a>
     */
    private static class LazyBinaryChecker {

        /**
         * Whether or not we made sure the LTSmin binaries can be run.
         */
        private static final boolean AVAILABLE = LTSminUtil.checkUsable();
    }

    public static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        public static boolean keepFiles() {
            return false;
        }

        public static int minimumUnfolds() {
            return 3; // super arbitrary number
        }

        public static double multiplier() {
            return 1.0; // quite arbitrary too
        }
    }
}
