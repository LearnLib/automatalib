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
package net.automatalib.modelcheckers.m3c.util;

import net.automatalib.modelcheckers.m3c.cfps.CFPS;
import net.automatalib.modelcheckers.m3c.cfps.Edge;
import net.automatalib.modelcheckers.m3c.cfps.EdgeType;
import net.automatalib.modelcheckers.m3c.cfps.State;
import net.automatalib.modelcheckers.m3c.cfps.StateClass;
import org.testng.Assert;

public class TestUtil {

    public static void assertCorrectlyCreated(CFPS cfps) {
        Assert.assertEquals(1, cfps.getProcessList().size());
        Assert.assertEquals(4, cfps.getStateList().size());
        Assert.assertEquals(cfps.getMainGraph(), cfps.getProcessList().get(0));

        State start = cfps.getState("start");
        State end = cfps.getState("end");
        State s1 = cfps.getState("s1");
        State s2 = cfps.getState("s2");

        /* Assert that state start is created correctly */
        Assert.assertEquals(StateClass.START, start.getStateClass());
        Assert.assertEquals(2, start.getOutgoingEdges().size());
        if (start.getOutgoingEdges().get(0).getLabel().equals("e")) {
            Edge startToEnd = start.getOutgoingEdges().get(0);
            Assert.assertEquals(end, startToEnd.getTarget());
            Assert.assertEquals(EdgeType.MUST, startToEnd.getEdgeType());

            Edge startToS1 = start.getOutgoingEdges().get(1);
            Assert.assertEquals(s1, startToS1.getTarget());
            Assert.assertEquals(EdgeType.MUST, startToS1.getEdgeType());
            Assert.assertEquals("a", startToS1.getLabel());
        }

        /* Assert that state end is created correctly */
        Assert.assertEquals(StateClass.END, end.getStateClass());
        Assert.assertEquals(0, end.getOutgoingEdges().size());

        /* Assert that state s1 is created correctly */
        Assert.assertEquals(StateClass.NORMAL, s1.getStateClass());
        Assert.assertEquals(1, s1.getOutgoingEdges().size());
        Edge s1ToS2 = s1.getOutgoingEdges().get(0);
        Assert.assertEquals(s2, s1ToS2.getTarget());
        Assert.assertEquals("P", s1ToS2.getLabel());
        Assert.assertEquals(EdgeType.MUST_PROCESS, s1ToS2.getEdgeType());

        /* Assert that state s2 is created correctly */
        Assert.assertEquals(StateClass.NORMAL, s2.getStateClass());
        Assert.assertEquals(1, s2.getOutgoingEdges().size());
        Edge s2ToEnd = s2.getOutgoingEdges().get(0);
        Assert.assertEquals(end, s2ToEnd.getTarget());
        Assert.assertEquals("b", s2ToEnd.getLabel());
        Assert.assertEquals(EdgeType.MUST, s2ToEnd.getEdgeType());
    }
}
