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
package net.automatalib.util.automata.ads;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import net.automatalib.graphs.ads.ADSNode;

/**
 * Utility class that holds some information aggregated during the ADS computation of {@link LeeYannakakis}.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
public class LYResult<S, I, O> {

    private final Optional<ADSNode<S, I, O>> delegate;

    private final Set<S> indistinguishableStates;

    public LYResult(final ADSNode<S, I, O> result) {
        this.delegate = Optional.of(result);
        this.indistinguishableStates = Collections.emptySet();
    }

    public LYResult(final Set<S> indistinguishableStates) {
        this.delegate = Optional.empty();
        this.indistinguishableStates = indistinguishableStates;
    }

    public LYResult() {
        this.delegate = Optional.empty();
        this.indistinguishableStates = Collections.emptySet();
    }

    public boolean isPresent() {
        return this.delegate.isPresent();
    }

    public ADSNode<S, I, O> get() {
        return this.delegate.get();
    }

    public Set<S> getIndistinguishableStates() {
        return this.indistinguishableStates;
    }
}
