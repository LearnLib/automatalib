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
package net.automatalib.serialization.xml.ra;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import jakarta.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.automatalib.automaton.ra.Assignment;
import net.automatalib.automaton.ra.impl.InputTransition;
import net.automatalib.automaton.ra.impl.MutableRegisterAutomaton;
import net.automatalib.automaton.ra.impl.RALocation;
import net.automatalib.automaton.ra.impl.TransitionGuard;
import net.automatalib.automaton.ra.impl.OutputMapping;
import net.automatalib.automaton.ra.impl.OutputTransition;
import net.automatalib.serialization.xml.ra.RegisterAutomaton.Transitions.Transition;
import net.automatalib.data.Constants;
import net.automatalib.data.DataType;
import net.automatalib.data.DataValue;
import net.automatalib.data.SymbolicDataValue;
import net.automatalib.data.SymbolicDataValue.Constant;
import net.automatalib.data.SymbolicDataValue.Parameter;
import net.automatalib.data.SymbolicDataValue.Register;
import net.automatalib.data.VarMapping;
import net.automatalib.data.VarValuation;
import net.automatalib.data.SymbolicDataValueGenerator.ConstantGenerator;
import net.automatalib.data.SymbolicDataValueGenerator.ParameterGenerator;
import net.automatalib.data.SymbolicDataValueGenerator.RegisterGenerator;
import net.automatalib.symbol.impl.InputSymbol;
import net.automatalib.symbol.impl.OutputSymbol;
import net.automatalib.symbol.ParameterizedSymbol;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;

/**
 *
 * @author falk
 */
public class RegisterAutomatonImporter {

    private final Map<String, ParameterizedSymbol> inputSigmaMap = new LinkedHashMap<>();
    private final Map<String, ParameterizedSymbol> outputSigmaMap = new LinkedHashMap<>();
    private final Map<ParameterizedSymbol, String[]> paramNames = new LinkedHashMap<>();
    private final Map<String, RALocation> stateMap = new LinkedHashMap<>();
    private final Map<String, Constant> constMap = new LinkedHashMap<>();
    private final Map<String, Register> regMap = new LinkedHashMap<>();
    private final Map<String, DataType> typeMap = new LinkedHashMap<>();
    // TRUE input locations, FALSE output locations
    private final Map<String, Boolean> locationTypeMap = new LinkedHashMap<>();

    private final VarValuation initialRegs = new VarValuation();
    private final Constants consts = new Constants();

    private MutableRegisterAutomaton iora;
    private Alphabet<InputSymbol> inputs;
    private Alphabet<ParameterizedSymbol> actions;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterAutomatonImporter.class);

    public Collection<DataType> getDataTypes() {
        return typeMap.values();
    }

    public RegisterAutomatonImporter(InputStream is) {
        loadModel(is);
    }

    public net.automatalib.automaton.ra.impl.RegisterAutomaton getRegisterAutomaton() {
        return this.iora;
    }

    private void loadModel(InputStream is) {

        RegisterAutomaton a = unmarschall(is);

        getAlphabet(a.getAlphabet());

        getConstants(a.getConstants());
        getRegisters(a.getGlobals());

        iora = new MutableRegisterAutomaton(consts, initialRegs);

        for (RegisterAutomaton.Locations.Location l : a.getLocations().getLocation()) {
            if (l.getInitial() != null && l.getInitial().equals("true")) {
                stateMap.put(l.getName(), iora.addInitialState());
                locationTypeMap.put(l.getName(), Boolean.TRUE);
            } else {
                stateMap.put(l.getName(), iora.addState());
            }
        }

        // determine input/output locations
        List<RegisterAutomaton.Transitions.Transition> transitions = new LinkedList<>(a.getTransitions().getTransition());
        while(!transitions.isEmpty()) {
            ListIterator<Transition> iter = transitions.listIterator();
            while (iter.hasNext()) {
                RegisterAutomaton.Transitions.Transition t = iter.next();
                if (locationTypeMap.containsKey(t.from)) {
                    locationTypeMap.put(t.to, !locationTypeMap.get(t.from));
                    iter.remove();
                } else {
                    if (locationTypeMap.containsKey(t.to)) {
                        locationTypeMap.put(t.from, !locationTypeMap.get(t.to));
                        iter.remove();
                    }
                }
            }
        }

        // transitions
        for (RegisterAutomaton.Transitions.Transition t : a.getTransitions().getTransition()) {

            // read basic data
            ParameterizedSymbol ps = locationTypeMap.get(t.getFrom()) ? inputSigmaMap.get(t.getSymbol()) : outputSigmaMap.get(t.getSymbol());
            RALocation from = stateMap.get(t.getFrom());
            RALocation to = stateMap.get(t.getTo());
            String[] pnames = paramNames.get(ps);
            if (t.getParams() != null) {
                pnames = t.getParams().split(",");
            }
            Map<String, Parameter> paramMap = paramMap(ps, pnames);

            // guard
            String gstring = t.getGuard();
            TransitionGuard p = new TransitionGuard();
            if (gstring != null) {
                Map<String, SymbolicDataValue> map = buildValueMap(
                        constMap, regMap, (ps instanceof OutputSymbol) ? new LinkedHashMap<String, Parameter>() : paramMap);
                ExpressionParser parser = new ExpressionParser(gstring, map);
                p = new TransitionGuard(parser.getPredicate());
            }

            // assignment
            Set<Register> freshRegs = new HashSet<>();
            VarMapping<Register, SymbolicDataValue> assignments = new VarMapping<>();
            if (t.getAssignments() != null) {
                for (RegisterAutomaton.Transitions.Transition.Assignments.Assign ass :
                        t.getAssignments().getAssign()) {
                    Register left = regMap.get(ass.to);
                    SymbolicDataValue right;
                    if ( ("__fresh__").equals(ass.value)) {
                        freshRegs.add(left);
                        continue;
                    }
                    else if (paramMap.containsKey(ass.value)) {
                        right = paramMap.get(ass.value);
                    }
                    else if (constMap.containsKey(ass.value)) {
                        right = constMap.get(ass.value);
                    }
                    else {
                        right = regMap.get(ass.value);
                    }
                    assignments.put(left, right);
                }
            }
            Assignment assign = new Assignment(assignments);

            // output
            if (ps instanceof OutputSymbol) {

                Parameter[] pList = paramList(ps);
                int idx = 0;
                VarMapping<Parameter, SymbolicDataValue> outputs = new VarMapping<>();
                for (String s : pnames) {
                    //Parameter param = paramMap.get(s);
                    Parameter param = pList[idx++];
                    SymbolicDataValue source = null;
                    if (regMap.containsKey(s)) {
                        Register r = regMap.get(s);
                        if (freshRegs.contains(r)) {
                            // add assignment to store fresh value
                            assignments.put(r, param);
                            continue;
                        }
                        else {
                            // check if there was an assignment,
                            // these seem to be meant to
                            // happen before the output
                            source = assignments.containsKey(r) ?
                                    assignments.get(r) : r;
                        }
                    } else if (constMap.containsKey(s)) {
                        source = constMap.get(s);
                    } else {
                        throw new IllegalStateException("No source for output parameter.");
                    }
                    outputs.put(param, source);
                }

                // all unassigned parameters have to be fresh by convention,
                // we do not allow "don't care" in outputs
                Set<Parameter> fresh = new LinkedHashSet<>(paramMap.values());
                fresh.removeAll(outputs.keySet());
                OutputMapping outMap = new OutputMapping(fresh, outputs);

                OutputTransition tOut = new OutputTransition(p, outMap,
                        (OutputSymbol) ps, from, to, assign);
                iora.addTransition(from, ps, tOut);
                LOGGER.trace("Loading: {}", tOut);
            } // input
            else {
                assert freshRegs.isEmpty();

                LOGGER.trace("Guard: {}", gstring);
                InputTransition tIn = new InputTransition(p, (InputSymbol) ps,
                        from, to, assign);
                LOGGER.trace("Loading: {}", tIn);
                iora.addTransition(from, ps, tIn);
            }
        }
    }

    private void getAlphabet(RegisterAutomaton.Alphabet a) {
        inputs = new GrowingMapAlphabet<>();
        actions = new GrowingMapAlphabet<>();
        for (RegisterAutomaton.Alphabet.Inputs.Symbol s : a.getInputs().getSymbol()) {
            int pcount = s.getParam().size();
            String[] pNames = new String[pcount];
            DataType[] pTypes = new DataType[pcount];
            int idx = 0;
            for (RegisterAutomaton.Alphabet.Inputs.Symbol.Param p : s.getParam()) {
                pNames[idx] = p.name;
                pTypes[idx] = getOrCreateType(p.type);
                idx++;
            }
            String sName = s.getName();
            InputSymbol ps = new InputSymbol(sName, pTypes);
            inputs.add(ps);
            actions.add(ps);
            inputSigmaMap.put(s.getName(), ps);
            paramNames.put(ps, pNames);
            LOGGER.trace("Loading: {}", ps);
        }
        for (RegisterAutomaton.Alphabet.Outputs.Symbol s : a.getOutputs().getSymbol()) {
            int pcount = s.getParam().size();
            String[] pNames = new String[pcount];
            DataType[] pTypes = new DataType[pcount];
            int idx = 0;
            for (RegisterAutomaton.Alphabet.Outputs.Symbol.Param p : s.getParam()) {
                pNames[idx] = p.name;
                pTypes[idx] = getOrCreateType(p.type);
                idx++;
            }
            String sName = s.getName();
            ParameterizedSymbol ps = new OutputSymbol(sName, pTypes);
            actions.add(ps);
            outputSigmaMap.put(s.getName(), ps);
            paramNames.put(ps, pNames);
            LOGGER.trace("Loading: {}", ps);
        }
    }

    private void getConstants(RegisterAutomaton.Constants xml) {
        ConstantGenerator cgen = new ConstantGenerator();
        for (RegisterAutomaton.Constants.Constant def : xml.getConstant()) {
            DataType type = getOrCreateType(def.type);
            Constant c = cgen.next(type);
            constMap.put(def.value, c);
            constMap.put(def.name, c);
            LOGGER.trace("{} ->{}", def.name, c);
            DataValue dv = new DataValue(type, Integer.parseInt(def.value));
            consts.put(c, dv);
        }
        LOGGER.trace("Loading: {}", consts);
    }

    private void getRegisters(RegisterAutomaton.Globals g) {
        RegisterGenerator rgen = new RegisterGenerator();
        for (RegisterAutomaton.Globals.Variable def : g.getVariable()) {
            DataType type = getOrCreateType(def.type);
            Register r = rgen.next(type);
            regMap.put(def.name, r);
            LOGGER.trace("{} ->{}", def.name, r);
            Object o = null;
            switch (type.getBase().getName()) {
                case "java.lang.Integer":
                    o = Integer.parseInt(def.value);
                    break;
                case "java.lang.Double":
                    o = BigDecimal.valueOf(Double.parseDouble(def.value));
                    break;
                default:
                    throw new IllegalStateException(
                            "Unsupported Register Type: " +
                                    type.getBase().getName());
            }
            DataValue dv = new DataValue(type, o);
            initialRegs.put(r, dv);
        }
        LOGGER.trace("Loading: {}", initialRegs);
    }

    private RegisterAutomaton unmarschall(InputStream is) {
        return JAXB.unmarshal(is, RegisterAutomaton.class);
    }

    private DataType getOrCreateType(String name) {
        DataType t = typeMap.get(name);
        if (t == null) {
            // TODO: there should be a proper way of specifying java types to be bound
            t = new DataType(name, isDoubleTempCheck(name) ? Double.class : Integer.class);
            typeMap.put(name, t);
        }
        return t;
    }

    private boolean isDoubleTempCheck(String name) {
        return name.equals("DOUBLE") || name.equals("double");
    }

    private Map<String, SymbolicDataValue> buildValueMap(
            Map<String, ? extends SymbolicDataValue> ... maps) {
        Map<String, SymbolicDataValue> ret = new LinkedHashMap<>();
        for (Map<String, ? extends SymbolicDataValue> m : maps) {
            ret.putAll(m);
        }
        return ret;
    }

    private Map<String, Parameter> paramMap(
            ParameterizedSymbol ps, String ... pNames) {

        if (pNames == null) {
            pNames = paramNames.get(ps);
        }

        Map<String, Parameter> ret = new LinkedHashMap<>();
        ParameterGenerator pgen = new ParameterGenerator();
        int idx = 0;
        for (String name : pNames) {
            ret.put(name, pgen.next(ps.getPtypes()[idx]));
            idx++;
        }
        return ret;
    }

    private Parameter[] paramList(ParameterizedSymbol ps) {


        Parameter[] ret = new Parameter[ps.getArity()];
        ParameterGenerator pgen = new ParameterGenerator();
        int idx = 0;
        for (DataType t : ps.getPtypes()) {
            ret[idx] = pgen.next(t);
            idx++;
        }
        return ret;
    }

    /**
     * @return the inputs
     */
    public Alphabet<InputSymbol> getInputs() {
        return inputs;
    }

    /**
     * @return the inputs
     */
    public Alphabet<ParameterizedSymbol> getActions() {
        return actions;
    }

    /**
     * @return the consts
     */
    public Constants getConstants() {
        return consts;
    }

}
