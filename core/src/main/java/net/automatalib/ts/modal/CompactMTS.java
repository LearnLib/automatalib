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

import net.automatalib.ts.modal.ModalEdgeProperty.ModalType;
import net.automatalib.words.Alphabet;

public class CompactMTS<I> extends AbstractCompactMTS<I, MutableModalEdgeProperty> {

    public CompactMTS(Alphabet<I> alphabet) {
        super(alphabet);
    }

    public CompactMTS(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
    }

    @Override
    protected MutableModalEdgeProperty getDefaultTransitionProperty() {
        return new ModalEdgePropertyImpl(ModalType.MUST);
    }
}
