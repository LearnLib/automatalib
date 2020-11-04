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

public class Transition<S, I, TP> implements Serializable {

    protected S source;
    protected S target;
    protected I label;
    protected TP property;

    Transition(S target, TP property) {
        this.target = target;
        this.property = property;
    }

    public Transition(S source, I label, S target, TP property) {
        this.source = source;
        this.label = label;
        this.target = target;
        this.property = property;
    }

    public S getSource() {
        return source;
    }

    void setSource(S source) {
        this.source = source;
    }

    public S getTarget() {
        return target;
    }

    void setTarget(S target) {
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

    @Override
    public String toString() {
        return "Transition{" + "source=" + source + ", target=" + target + ", label=" + label + ", property=" +
               property + '}';
    }
}
