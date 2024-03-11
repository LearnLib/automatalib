/*
 * Copyright (C) 2014-2015 The LearnLib Contributors
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package net.automatalib.automaton.ra;

import java.util.Map.Entry;

import net.automatalib.data.Constants;
import net.automatalib.data.ParValuation;
import net.automatalib.data.SymbolicDataValue;
import net.automatalib.data.SymbolicDataValue.Constant;
import net.automatalib.data.SymbolicDataValue.Parameter;
import net.automatalib.data.SymbolicDataValue.Register;
import net.automatalib.data.VarMapping;
import net.automatalib.data.VarValuation;

/**
 * A parallel assignment for registers.
 *
 * @author falk
 */
public class Assignment {

    private final VarMapping<Register, ? extends SymbolicDataValue> assignment;

    public Assignment(VarMapping<Register, ? extends SymbolicDataValue> assignment) {
        this.assignment = assignment;
    }

    public VarValuation compute(VarValuation registers, ParValuation parameters, Constants consts) {
        VarValuation val = new VarValuation(registers);
        for (Entry<Register, ? extends SymbolicDataValue> e : assignment) {
            SymbolicDataValue valp = e.getValue();
            if (valp.isRegister()) {
                val.put(e.getKey(), registers.get( (Register) valp));
            }
            else if (valp.isParameter()) {
                val.put(e.getKey(), parameters.get( (Parameter) valp));
            }
            //TODO: check if we want to copy constant values into vars
            else if (valp.isConstant()) {
                val.put(e.getKey(), consts.get( (Constant) valp));
            }
            else {
                throw new IllegalStateException("Illegal assignment: " +
                        e.getKey() + " := " + valp);
            }
        }
        return val;
    }

    @Override
    public String toString() {
        return assignment.toString(":=");
    }

    public VarMapping<Register, ? extends SymbolicDataValue> getAssignment() {
        return assignment;
    }

}
