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
package net.automatalib.modelcheckers.m3c.cfps;

import net.automatalib.modelcheckers.m3c.util.TestUtil;
import org.testng.annotations.Test;

class CFPSTest {

    @Test
    void testSimpleExample() {
        CFPS cfps = new CFPS();
        ProceduralProcessGraph ppg = new ProceduralProcessGraph();
        ppg.setProcessName("P");

        cfps.addPPG(ppg);
        cfps.setMainGraph(ppg);

        State startState = cfps.createAndAddState(ppg, StateClass.START).withName("start");
        State endState = cfps.createAndAddState(ppg, StateClass.END).withName("end");
        State s1 = cfps.createAndAddState(ppg, StateClass.NORMAL).withName("s1");
        State s2 = cfps.createAndAddState(ppg, StateClass.NORMAL).withName("s2");

        Edge startToS1 = new Edge(startState, s1, "a", EdgeType.MUST);
        Edge startToEnd = new Edge(startState, endState, "e", EdgeType.MUST);
        Edge s1ToS2 = new Edge(s1, s2, "P", EdgeType.MUST_PROCESS);
        Edge s2TOEnd = new Edge(s2, endState, "b", EdgeType.MUST);

        startState.addEdge(startToS1);
        startState.addEdge(startToEnd);
        s1.addEdge(s1ToS2);
        s2.addEdge(s2TOEnd);
        TestUtil.assertCorrectlyCreated(cfps);
    }

    @Test
    void testAddStatesFirst() {
        CFPS cfps = new CFPS();
        ProceduralProcessGraph ppg = new ProceduralProcessGraph();
        ppg.setProcessName("P");
        cfps.setMainGraph(ppg);
        addStates(ppg);
        cfps.addPPG(ppg);
        TestUtil.assertCorrectlyCreated(cfps);
    }

    private void addStates(ProceduralProcessGraph ppg) {
        State startState = new State(StateClass.START).withName("start");
        State endState = new State(StateClass.END).withName("end");
        State s1 = new State(StateClass.NORMAL).withName("s1");
        State s2 = new State(StateClass.NORMAL).withName("s2");

        Edge startToS1 = new Edge(startState, s1, "a", EdgeType.MUST);
        Edge startToEnd = new Edge(startState, endState, "e", EdgeType.MUST);
        Edge s1ToS2 = new Edge(s1, s2, "P", EdgeType.MUST_PROCESS);
        Edge s2TOEnd = new Edge(s2, endState, "b", EdgeType.MUST);

        startState.addEdge(startToS1);
        startState.addEdge(startToEnd);
        s1.addEdge(s1ToS2);
        s2.addEdge(s2TOEnd);

        ppg.addState(startState);
        ppg.addState(endState);
        ppg.addState(s1);
        ppg.addState(s2);
    }

    @Test
    void testAddPPGFirst() {
        CFPS cfps = new CFPS();
        ProceduralProcessGraph ppg = new ProceduralProcessGraph();
        ppg.setProcessName("P");
        cfps.setMainGraph(ppg);
        cfps.addPPG(ppg);
        addStates(ppg);
        TestUtil.assertCorrectlyCreated(cfps);
    }

}
