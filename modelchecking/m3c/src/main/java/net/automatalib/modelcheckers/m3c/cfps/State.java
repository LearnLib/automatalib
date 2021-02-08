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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class State {

    private final List<Edge> incomingEdges;
    private final List<Edge> outgoingEdges;
    private Set<String> stateLabels;
    private int stateNumber;
    private StateClass stateClass;
    private ProceduralProcessGraph ppg;
    private String name;

    public State(StateClass stateClass) {
        this.stateClass = stateClass;
        this.incomingEdges = new ArrayList<>();
        this.outgoingEdges = new ArrayList<>();
        this.stateLabels = new HashSet<>();
    }

    public State withName(String name) {
        this.name = name;
        return this;
    }

    public State withStateLabels(String stateLabels) {
        String[] splitLabels = stateLabels.split(",");
        this.stateLabels = new HashSet<>(Arrays.asList(splitLabels));
        return this;
    }

    public boolean satisfiesAtomicProposition(String atomicProp) {
        for (String stateLabel : stateLabels) {
            if (stateLabel.equals(atomicProp)) {
                return true;
            }
        }
        return false;
    }

    public int numOutgoingEdgesLabeled(String label) {
        int numEdgedLabeledByArgLabel = 0;
        for (Edge edge : outgoingEdges) {
            if (edge.getLabel().equals(label)) {
                numEdgedLabeledByArgLabel++;
            }
        }
        return numEdgedLabeledByArgLabel;
    }

    public Set<State> getPredecessors() {
        Set<State> predecessors = new HashSet<>();
        for (Edge incomingEdge : incomingEdges) {
            predecessors.add(incomingEdge.getSource());
        }
        return predecessors;
    }

    public String getName() {
        return name;
    }

    public ProceduralProcessGraph getProceduralProcessGraph() {
        return ppg;
    }

    public void setProceduralProcessGraph(ProceduralProcessGraph ppg) {
        this.ppg = ppg;
    }

    public StateClass getStateClass() {
        return stateClass;
    }

    public void setStateClass(StateClass stateClass) {
        this.stateClass = stateClass;
    }

    public int getStateNumber() {
        return stateNumber;
    }

    public void setStateNumber(int stateNumber) {
        this.stateNumber = stateNumber;
    }

    public Set<String> getStateLabels() {
        return stateLabels;
    }

    public List<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public List<Edge> getIncomingEdges() {
        return incomingEdges;
    }

    public boolean isStartState() {
        return stateClass == StateClass.START;
    }

    public boolean isEndState() {
        return stateClass == StateClass.END;
    }

    public void addEdge(Edge edge) {
        this.addOutgoingEdge(edge);
        edge.getTarget().addIncomingEdge(edge);
    }

    private void addOutgoingEdge(Edge edge) {
        outgoingEdges.add(edge);
    }

    private void addIncomingEdge(Edge edge) {
        incomingEdges.add(edge);
    }

    public void addOutgoingEdge(State target, String edgeLabel, EdgeType edgeType) {
        outgoingEdges.add(new Edge(this, target, edgeLabel, edgeType));
    }

}
