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
package net.automatalib.graph.impl;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.graph.MutableGraph;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DynamicNodeMappingTest {

    @Test
    public void testGraph() {
        CompactGraph<Void, Void> graph = new CompactGraph<>();
        graph.addNode();
        graph.addNode();
        graph.addNode();

        testMutableGraph(graph);
    }

    private static <N> void testMutableGraph(MutableGraph<N, ?, ?, ?> graph) {

        final List<N> states = new ArrayList<>(graph.getNodes());

        final N n0 = states.get(0);
        final N n1 = states.get(1);
        final N n2 = states.get(2);

        final MutableMapping<N, Integer> mapping = graph.createDynamicNodeMapping();

        mapping.put(n0, 0);
        mapping.put(n1, 1);
        mapping.put(n2, 2);

        Assert.assertEquals(mapping.get(n0).intValue(), 0);
        Assert.assertEquals(mapping.get(n1).intValue(), 1);
        Assert.assertEquals(mapping.get(n2).intValue(), 2);

        final N n3 = graph.addNode();
        final N n4 = graph.addNode();
        mapping.put(n3, 3);
        mapping.put(n4, 4);

        Assert.assertEquals(mapping.get(n3).intValue(), 3);
        Assert.assertEquals(mapping.get(n4).intValue(), 4);
    }
}
