/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.graphs.base;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Preconditions;
import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.ModalProcessGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DefaultMCFPS<L, AP> implements ModalContextFreeProcessSystem<L, AP> {

    private final Map<L, ModalProcessGraph<?, L, ?, AP, ?>> mpgs;
    private final L mainProcess;

    public DefaultMCFPS(L mainProcess, Map<L, ? extends ModalProcessGraph<?, L, ?, AP, ?>> mpgs) {
        Preconditions.checkArgument(mpgs.containsKey(mainProcess),
                                    "There exists no process graph for the main process");

        this.mpgs = Collections.unmodifiableMap(mpgs);
        this.mainProcess = mainProcess;
    }

    @Override
    public Map<L, ModalProcessGraph<?, L, ?, AP, ?>> getMPGs() {
        return this.mpgs;
    }

    @Override
    public @Nullable L getMainProcess() {
        return this.mainProcess;
    }
}
