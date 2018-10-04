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

import net.automatalib.automata.concepts.Output;
import net.automatalib.modelcheckers.ltsmin.AbstractLTSminTest;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public abstract class AbstractLTSminMonitorTest<A, R extends Output<String, ?>> extends AbstractLTSminTest<A, R> {

    @Override
    protected Word<String> getInput() {
        return new WordBuilder<String>().repeatAppend(4, "a").toWord();
    }

    @Override
    public abstract AbstractLTSminMonitor<String, A, R> getModelChecker();

    @Override
    protected int getMinimumMajorVersion() {
        return AbstractLTSminMonitor.MAJOR;
    }

    @Override
    protected int getMinimumMinorVersion() {
        return AbstractLTSminMonitor.MINOR;
    }

    @Override
    protected int getMinimumPatchVersion() {
        return AbstractLTSminMonitor.PATCH;
    }
}
