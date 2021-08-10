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
package net.automatalib.modelcheckers.m3c.solver;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.Mappings;
import net.automatalib.graphs.ModalProcessGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.modelcheckers.m3c.transformer.TransformerSerializer;

public final class SolverData<N, T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final ModalProcessGraph<N, L, ?, AP, ?> mpg;
    private final NodeIDs<N> nodeIDs;
    private final Mapping<N, List<String>> initialPropertyTransformers;
    private final Mapping<N, List<FormulaNode<L, AP>>> initialSatisfiedSubformulas;

    SolverData(ModalProcessGraph<N, L, ?, AP, ?> mpg,
               Mapping<N, List<String>> initialPropertyTransformers,
               Mapping<N, List<FormulaNode<L, AP>>> initialSatisfiedSubformulas) {
        this.mpg = mpg;
        this.nodeIDs = mpg.nodeIDs();
        this.initialPropertyTransformers = initialPropertyTransformers;
        this.initialSatisfiedSubformulas = initialSatisfiedSubformulas;
    }

    public ModalProcessGraph<N, L, ?, AP, ?> getMpg() {
        return mpg;
    }

    public NodeIDs<N> getNodeIDs() {
        return nodeIDs;
    }

    public Mapping<N, T> getInitialPropertyTransformers(TransformerSerializer<T, L, AP> serializer) {
        final Map<N, T> result = Maps.newHashMapWithExpectedSize(this.mpg.size());

        for (N n : this.mpg) {
            result.put(n, serializer.deserialize(this.initialPropertyTransformers.get(n)));
        }

        return Mappings.fromMap(result);
    }

    public Mapping<N, List<FormulaNode<L, AP>>> getInitialSatisfiedSubformulas() {
        return initialSatisfiedSubformulas;
    }
}
