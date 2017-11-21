/* Copyright (C) 2013-2017 TU Dortmund
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
package net.automatalib.automata.transout.probabilistic;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class ProbabilisticOutput<O> {

    private final float probability;
    @Nullable
    private final O output;

    public ProbabilisticOutput(float probability, @Nullable O output) {
        this.probability = probability;
        this.output = output;
    }

    public float getProbability() {
        return probability;
    }

    @Nullable
    public O getOutput() {
        return output;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(output);
        result = prime * result + Float.floatToIntBits(probability);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != ProbabilisticOutput.class) {
            return false;
        }
        ProbabilisticOutput<?> other = (ProbabilisticOutput<?>) obj;

        return Objects.equals(output, other.output) && (probability == other.probability);
    }

}
