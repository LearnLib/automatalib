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
package net.automatalib.automaton.ra.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.data.SymbolicDataValue.Register;
import net.automatalib.data.VarValuation;
import net.automatalib.symbol.PSymbolInstance;
import net.automatalib.word.Word;

/**
 *
 * @author falk
 */
public abstract class RegisterAutomaton implements net.automatalib.automaton.ra.RegisterAutomaton<RALocation, Transition> {

    private final VarValuation initialRegisters;

    public RegisterAutomaton(VarValuation initialRegisters) {
        this.initialRegisters = initialRegisters;
    }

    public RegisterAutomaton() {
        this(new VarValuation());
    }


    /**
     * Checks if a data word is accepted by an automaton.
     *
     * @param dw a data word
     * @return true if dw is accepted
     */
    public abstract boolean accepts(Word<PSymbolInstance> dw);

    public abstract RALocation getLocation(Word<PSymbolInstance> dw);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RALocation loc : getStates()) {
            sb.append(loc).append(loc.equals(getInitialState()) ? " (initial)" : "").append(":\n");
            for (Transition t : loc.getOut()) {
                sb.append("  ").append(t).append("\n");
            }
            sb.append("\n");
        }
        sb.append("Init:").append(initialRegisters).append("\n");
        return sb.toString();
    }

    /**
     * @return the initialRegisters
     */
    public VarValuation getInitialRegisters() {
        return initialRegisters;
    }

    public List<Transition> getTransitions() {
        List<Transition> tList = new ArrayList<>();
        for (RALocation loc : getStates()) {
            tList.addAll(loc.getOut());
        }
        return tList;
    }

    public List<Transition> getInputTransitions() {
        List<Transition> tList = new ArrayList<>();
        for (RALocation loc : getStates()) {
            for (Transition t : loc.getOut()) {
                if (!(t instanceof OutputTransition)) {
                    tList.add(t);
                }
            }
        }
        return tList;
    }

    public Collection<RALocation> getInputStates() {
        Set<RALocation> ret = new HashSet<>();
        for (Transition t : getInputTransitions()) {
            ret.add(t.getSource());
        }
        return ret;
    }

    public Collection<Register<?>> getRegisters() {
        Set<Register<?>> regs = new HashSet<>();
        for (Transition t : getTransitions()) {
            regs.addAll(t.getAssignment().getAssignment().keySet());
        }
        return regs;
    }
}
