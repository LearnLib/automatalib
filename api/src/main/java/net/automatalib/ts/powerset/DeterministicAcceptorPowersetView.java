/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.ts.powerset;

import net.automatalib.ts.AcceptorPowersetViewTS;
import net.automatalib.ts.acceptor.DeterministicAcceptorTS;

public class DeterministicAcceptorPowersetView<S, I> extends DeterministicPowersetView<S, I, S>
        implements AcceptorPowersetViewTS<S, I, S, S, S> {

    private final DeterministicAcceptorTS<S, I> ts;

    public DeterministicAcceptorPowersetView(DeterministicAcceptorTS<S, I> ts) {
        super(ts);
        this.ts = ts;
    }

    @Override
    public boolean isAccepting(S state) {
        return this.ts.isAccepting(state);
    }
}
