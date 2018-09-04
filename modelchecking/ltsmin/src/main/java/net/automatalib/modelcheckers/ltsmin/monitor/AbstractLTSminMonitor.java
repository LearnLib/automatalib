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
package net.automatalib.modelcheckers.ltsmin.monitor;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.modelcheckers.ltsmin.AbstractLTSmin;

/**
 * An LTSmin model checker for monitors.
 *
 * @see AbstractLTSmin
 * @see <a href="https://spot.lrde.epita.fr/tut11.html">https://spot.lrde.epita.fr/tut11.html</a>
 * @see <a href="http://ltsmin.utwente.nl/assets/man/etf2lts-mc.html">man etf2lts-mc</a>
 *
 * @author Jeroen Meijer
 */
public abstract class AbstractLTSminMonitor<I, A, R> extends AbstractLTSmin<I, A, R> {

    public static final int MAJOR = 3;

    public static final int MINOR = 1;

    public static final int PATCH = 0;

    /**
     * Constructs a new AbstractLTSminMonitor.
     *
     * @see AbstractLTSmin
     */
    protected AbstractLTSminMonitor(boolean keepFiles, Function<String, I> string2Input) throws ModelCheckingException {
        super(keepFiles, string2Input);
    }

    @Override
    protected int getMinimumMajorVersion() {
        return MAJOR;
    }

    @Override
    protected int getMinimumMinorVersion() {
        return MINOR;
    }

    @Override
    protected int getMinimumPatchVersion() {
        return PATCH;
    }

    @Override
    protected List<String> getExtraCommandLineOptions() {
        return Lists.newArrayList(// use a monitor created by spot
                                  "--buchi-type=monitor");
    }
}
