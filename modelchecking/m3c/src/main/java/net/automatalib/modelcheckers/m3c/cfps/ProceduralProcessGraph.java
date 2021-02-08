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
import java.util.List;

public class ProceduralProcessGraph {

    private final List<State> stateList;
    private int numberOfStates;
    private State start;
    private State end;
    private String processName;
    private CFPS cfps;

    public ProceduralProcessGraph(String processName) {
        this();
        this.processName = processName;
    }

    public ProceduralProcessGraph() {
        stateList = new ArrayList<>();
        numberOfStates = 0;
    }

    public ProceduralProcessGraph withName(String processName) {
        this.processName = processName;
        return this;
    }

    public void addState(State state) {
        if (state.isStartState()) {
            if (start != null) {
                throw new IllegalArgumentException("The ppg already has a start state and there can only be one.");
            }
            start = state;
        } else if (state.isEndState()) {
            if (end != null) {
                throw new IllegalArgumentException("The ppg already has an end state and there can only be one");
            }
            end = state;
        }
        if (cfps != null) {
            cfps.addState(state);
        }
        stateList.add(state);
        numberOfStates++;
    }

    public State getState(String name) {
        for (State state : stateList) {
            if (state.getName().equals(name)) {
                return state;
            }
        }
        return null;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public State getStartState() {
        return start;
    }

    public void setStartState(State startState) {
        start = startState;
    }

    public State getEndState() {
        return end;
    }

    public void setEndState(State endState) {
        end = endState;
    }

    public List<State> getStates() {
        return stateList;
    }

    public int getNumberOfStates() {
        return numberOfStates;
    }

    public void setCFPS(CFPS cfps) {
        this.cfps = cfps;
    }

}
