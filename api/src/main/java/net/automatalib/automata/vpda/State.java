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
package net.automatalib.automata.vpda;

/**
 * Utility class to combine an entity (e.g. a location) with stack information.
 *
 * @param <L>
 *         type of the object to connect with stack information
 *
 * @author Malte Isberner
 */
public class State<L> {

    private static final State<?> SINK = new State<>(null, null);
    private final L loc;
    private final StackContents stack;

    public State(final L loc, final StackContents stack) {
        this.loc = loc;
        this.stack = stack;
    }

    @SuppressWarnings("unchecked")
    public static <L> State<L> getSink() {
        return (State<L>) SINK;
    }

    public L getLocation() {
        return loc;
    }

    public StackContents getStackContents() {
        return stack;
    }

    public boolean isSink() {
        return loc == null;
    }
}
