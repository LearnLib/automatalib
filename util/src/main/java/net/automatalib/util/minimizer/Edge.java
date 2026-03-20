/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.util.minimizer;

/**
 * An edge in the internal automaton representation.
 *
 * @param <S>
 *         state class.
 * @param <T>
 *         transition label class.
 */
final class Edge<S, T> {

    // source state
    private final State<S, T> source;

    // transition label
    private final TransitionLabel<S, T> transitionLabel;

    /**
     * Constructor.
     *
     * @param source
     *         the source state.
     * @param transitionLabel
     *         the transition label.
     */
    Edge(State<S, T> source, TransitionLabel<S, T> transitionLabel) {
        this.source = source;
        this.transitionLabel = transitionLabel;
    }

    /**
     * Retrieves the source state.
     *
     * @return the source state.
     */
    State<S, T> getSource() {
        return source;
    }

    /**
     * Retrieves the transition label.
     *
     * @return the transition label.
     */
    TransitionLabel<S, T> getTransitionLabel() {
        return transitionLabel;
    }

}
