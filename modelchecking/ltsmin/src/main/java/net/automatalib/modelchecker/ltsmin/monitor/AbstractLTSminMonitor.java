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
package net.automatalib.modelchecker.ltsmin.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.automatalib.modelchecker.ltsmin.AbstractLTSmin;
import net.automatalib.modelchecker.ltsmin.LTSminVersion;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;

/**
 * An LTSmin model checker for monitors.
 *
 * @see AbstractLTSmin
 * @see <a href="https://spot.lrde.epita.fr/tut11.html">https://spot.lrde.epita.fr/tut11.html</a>
 * @see <a href="http://ltsmin.utwente.nl/assets/man/etf2lts-mc.html">man etf2lts-mc</a>
 */
public abstract class AbstractLTSminMonitor<I, A, R> extends AbstractLTSmin<I, A, R> {

    public static final LTSminVersion REQUIRED_VERSION = LTSminVersion.of(3, 1, 0);

    /**
     * Constructs a new AbstractLTSminMonitor.
     *
     * @param keepFiles
     *         whether to keep the files generated during model checking
     * @param string2Input
     *         the input parsing function
     *
     * @see AbstractLTSmin#AbstractLTSmin(boolean, Function)
     */
    protected AbstractLTSminMonitor(boolean keepFiles, Function<String, I> string2Input) {
        super(keepFiles, string2Input);
    }

    @Override
    protected LTSminVersion getMinimumRequiredVersion(@UnknownInitialization(AbstractLTSmin.class) AbstractLTSminMonitor<I, A, R> this) {
        return REQUIRED_VERSION;
    }

    @Override
    protected List<String> getExtraCommandLineOptions() {
        return Collections.singletonList("--buchi-type=monitor"); // use a monitor created by spot
    }

    static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        static boolean keepFiles() {
            return false;
        }

        static <O> Collection<? super O> skipOutputs() {
            return Collections.emptyList();
        }
    }
}
