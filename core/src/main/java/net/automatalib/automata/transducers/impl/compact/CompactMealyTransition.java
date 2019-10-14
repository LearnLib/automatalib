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
package net.automatalib.automata.transducers.impl.compact;

import java.io.Serializable;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class CompactMealyTransition<O> implements Serializable {

    private int memoryIdx;
    private final int succId;
    private O output;

    CompactMealyTransition(int succId, O output) {
        this(-1, succId, output);
    }

    CompactMealyTransition(int memoryIdx, int succId, O output) {
        this.memoryIdx = memoryIdx;
        this.succId = succId;
        this.output = output;
    }

    public int getSuccId() {
        return succId;
    }

    public O getOutput() {
        return output;
    }

    void setOutput(O output) {
        this.output = output;
    }

    int getMemoryIdx() {
        return memoryIdx;
    }

    void setMemoryIdx(int memoryIdx) {
        this.memoryIdx = memoryIdx;
    }

    boolean isAutomatonTransition() {
        return memoryIdx >= 0;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompactMealyTransition)) {
            return false;
        }

        final CompactMealyTransition<?> that = (CompactMealyTransition<?>) o;
        return memoryIdx == that.memoryIdx && succId == that.succId && Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Integer.hashCode(memoryIdx);
        result = 31 * result + Integer.hashCode(succId);
        result = 31 * result + Objects.hashCode(output);
        return result;
    }
}
