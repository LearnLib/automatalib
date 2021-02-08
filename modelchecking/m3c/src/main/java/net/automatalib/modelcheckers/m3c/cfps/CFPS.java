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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CFPS {

    private final List<ProceduralProcessGraph> processList;
    private final List<State> stateList;
    private int numberOfStates;
    private ProceduralProcessGraph mainGraph;

    public CFPS(Collection<ProceduralProcessGraph> ppgs, ProceduralProcessGraph mainPpg) {
        this();
        this.mainGraph = mainPpg;
        for (ProceduralProcessGraph ppg : ppgs) {
            addPPG(ppg);
        }
    }

    public CFPS() {
        numberOfStates = 0;
        stateList = new ArrayList<>();
        processList = new ArrayList<>();
    }

    public void addPPG(ProceduralProcessGraph ppg) {
        processList.add(ppg);
        for (State state : ppg.getStates()) {
            state.setStateNumber(numberOfStates++);
            stateList.add(state);
        }
        ppg.setCFPS(this);
    }

    public State createAndAddState(ProceduralProcessGraph ppg, StateClass stateClass) {
        State state = new State(stateClass);
        state.setProceduralProcessGraph(ppg);
        ppg.addState(state);
        return state;
    }

    public void addState(ProceduralProcessGraph ppg, State state) {
        ppg.addState(state);
        state.setProceduralProcessGraph(ppg);
    }

    void addState(State state) {
        stateList.add(state);
        state.setStateNumber(numberOfStates++);
    }

    public State getState(int stateNumber) {
        return stateList.get(stateNumber);
    }

    public int getStateNumberOfProcess(String processName) {
        for (ProceduralProcessGraph ppg : processList) {
            if (ppg.getProcessName().equals(processName)) {
                return ppg.getStartState().getStateNumber();
            }
        }
        return -1;
    }

    public State getState(String name) {
        for (State state : stateList) {
            if (state.getName().equals(name)) {
                return state;
            }
        }
        return null;
    }

    public List<State> getAllEndStates() {
        List<State> list = new ArrayList<>();
        for (State state : mainGraph.getStates()) {
            if (state.getStateClass() == StateClass.END) {
                list.add(state);
            }
        }
        return list;
    }

    public List<Integer> getAllStatesWithEdgeLabel(String label) {
        List<Integer> sourceList = new ArrayList<>();
        for (ProceduralProcessGraph ppg : processList) {
            for (State state : ppg.getStates()) {
                for (Edge edge : state.getOutgoingEdges()) {
                    if (edge.getLabel().equals(label)) {
                        sourceList.add(state.getStateNumber());
                    }
                }
            }
        }
        return sourceList;
    }

    public List<State> getStatesOutgoingEdgeLabeledBy(String label) {
        List<State> sourceList = new ArrayList<>();
        for (ProceduralProcessGraph ppg : processList) {
            for (State state : ppg.getStates()) {
                for (Edge edge : state.getOutgoingEdges()) {
                    if (edge.getLabel().equals(label)) {
                        sourceList.add(state);
                        break;
                    }
                }
            }
        }
        return sourceList;
    }

    public List<State> getStateList() {
        return stateList;
    }

    public List<ProceduralProcessGraph> getProcessList() {
        return processList;
    }

    public int getNumberOfStates() {
        return numberOfStates;
    }

    public ProceduralProcessGraph getMainGraph() {
        return mainGraph;
    }

    public void setMainGraph(ProceduralProcessGraph ppg) {
        this.mainGraph = ppg;
    }

}
