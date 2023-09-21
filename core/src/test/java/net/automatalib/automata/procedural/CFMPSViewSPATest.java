/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.automata.procedural;

import java.util.Collection;
import java.util.Map;

import net.automatalib.graphs.ProceduralModalProcessGraph;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class CFMPSViewSPATest {

    @Test
    public void testPartialView() {
        final SPA<?, Character> spa = new StackSPA<>(SPATest.ALPHABET, 'S', SPATest.SUB_MODELS);
        final CFMPSViewSPA<Character> view = new CFMPSViewSPA<>(spa);

        final Map<Character, ProceduralModalProcessGraph<?, Character, ?, Void, ?>> pmpgs = view.getPMPGs();

        Assert.assertEquals(pmpgs.size(), 2);

        testPMPG(pmpgs.get('S'));
    }

    private <N, E> void testPMPG(ProceduralModalProcessGraph<N, Character, E, Void, ?> pmpg) {

        Assert.assertEquals(pmpg.size(), 8);

        final N initialNode = pmpg.getInitialNode();
        Assert.assertNotNull(initialNode);

        final Collection<E> initialOutgoing = pmpg.getOutgoingEdges(initialNode);
        Assert.assertEquals(initialOutgoing.size(), 1);

        final E initialEdge = initialOutgoing.iterator().next();
        Assert.assertEquals(pmpg.getEdgeLabel(initialEdge).charValue(), 'S');

        final N initialTarget = pmpg.getTarget(initialEdge);
        final Collection<E> targetOutgoing = pmpg.getOutgoingEdges(initialTarget);
        Assert.assertEquals(targetOutgoing.size(), 4); // should contain 'T', 'a', 'b', 'R'
    }


}
