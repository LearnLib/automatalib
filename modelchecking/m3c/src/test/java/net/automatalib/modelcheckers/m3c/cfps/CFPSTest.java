package net.automatalib.modelcheckers.m3c.cfps;

import org.testng.annotations.Test;

import static net.automatalib.modelcheckers.m3c.util.TestUtil.assertCorrectlyCreated;

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
        assertCorrectlyCreated(cfps);
    }

    @Test
    void testAddStatesFirst() {
        CFPS cfps = new CFPS();
        ProceduralProcessGraph ppg = new ProceduralProcessGraph();
        ppg.setProcessName("P");
        cfps.setMainGraph(ppg);
        addStates(ppg);
        cfps.addPPG(ppg);
        assertCorrectlyCreated(cfps);
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
        assertCorrectlyCreated(cfps);
    }

}
