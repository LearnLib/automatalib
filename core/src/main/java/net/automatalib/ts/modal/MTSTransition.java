/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.ts.modal;

import java.io.Serializable;

public class MTSTransition<I, TP extends MutableModalEdgeProperty> implements Serializable {

    private int source;
    private int target;
    private I label;
    private TP property;

    MTSTransition(int target, TP property) {
        this.target = target;
        this.property = property;
    }

    public int getSource() {
        return source;
    }

    void setSource(int source) {
        this.source = source;
    }

    public int getTarget() {
        return target;
    }

    void setTarget(int target) {
        this.target = target;
    }

    public I getLabel() {
        return label;
    }

    void setLabel(I label) {
        this.label = label;
    }

    public TP getProperty() {
        return property;
    }

    void setProperty(TP property) {
        this.property = property;
    }
}
