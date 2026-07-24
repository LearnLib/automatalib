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
package net.automatalib.serialization.dot;

import java.io.Writer;

import net.automatalib.automaton.Automaton;
import net.automatalib.common.util.IOUtil;
import net.automatalib.graph.Graph;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.serialization.ModelSerializer;

public final class DOTSerializationProvider {

    private DOTSerializationProvider() {
        // prevent instantiation
    }

    public static ModelSerializer<Graph<?, ?>> forGraph() {
        return (os, model) -> {
            try (Writer w = IOUtil.asNonClosingUTF8Writer(os)) {
                GraphDOT.write(model, w);
            }
        };
    }

    public static ModelSerializer<GraphViewable> forGraphViewable() {
        return (os, model) -> {
            try (Writer w = IOUtil.asNonClosingUTF8Writer(os)) {
                GraphDOT.write(model, w);
            }
        };
    }

    public static <I, M extends GraphViewable> InputModelSerializer<I, M> forGraphViewableInput() {
        return (os, model, alphabet) -> {
            try (Writer w = IOUtil.asNonClosingUTF8Writer(os)) {
                GraphDOT.write(model, w);
            }
        };
    }

    public static <I, M extends Automaton<?, I, ?>> InputModelSerializer<I, M> forAutomaton() {
        return (os, model, alphabet) -> {
            try (Writer w = IOUtil.asNonClosingUTF8Writer(os)) {
                GraphDOT.write(model.transitionGraphView(alphabet), w);
            }
        };
    }

}
