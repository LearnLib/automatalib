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

public class Edge {

    private final State source;
    private final State target;
    private final String label;
    private final EdgeType edgeType;

    public Edge(State source, State dest, String label, EdgeType edgeType) {
        this.source = source;
        this.target = dest;
        this.label = label;
        this.edgeType = edgeType;
    }

    public State getSource() {
        return source;
    }

    public State getTarget() {
        return target;
    }

    public String getLabel() {
        return label;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public boolean isProcessCall() {
        return edgeType == EdgeType.MUST_PROCESS || edgeType == EdgeType.MAY_PROCESS;
    }

    public boolean isMayProcessCall() {
        return edgeType == EdgeType.MAY_PROCESS;
    }

    public boolean isMust() {
        return edgeType == EdgeType.MUST || isMustProcessCall();
    }

    public boolean isMustProcessCall() {
        return edgeType == EdgeType.MUST_PROCESS;
    }

    public boolean isMay() {
        return edgeType == EdgeType.MAY;
    }

    public boolean labelMatches(String label) {
        return "".equals(label) || this.label.equals(label);
    }

}
