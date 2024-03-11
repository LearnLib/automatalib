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

import jakarta.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the automata.xml package.
 * <p>
 * An ObjectFactory allows to programmatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: automata.xml
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RegisterAutomaton }
     *
     */
    public RegisterAutomaton createRegisterAutomaton() {
        return new RegisterAutomaton();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Transitions }
     *
     */
    public RegisterAutomaton.Transitions createRegisterAutomatonTransitions() {
        return new RegisterAutomaton.Transitions();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Transitions.Transition }
     *
     */
    public RegisterAutomaton.Transitions.Transition createRegisterAutomatonTransitionsTransition() {
        return new RegisterAutomaton.Transitions.Transition();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Transitions.Transition.Assignments
     * }
     *
     */
    public RegisterAutomaton.Transitions.Transition.Assignments createRegisterAutomatonTransitionsTransitionAssignments() {
        return new RegisterAutomaton.Transitions.Transition.Assignments();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Locations }
     *
     */
    public RegisterAutomaton.Locations createRegisterAutomatonLocations() {
        return new RegisterAutomaton.Locations();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Globals }
     *
     */
    public RegisterAutomaton.Globals createRegisterAutomatonGlobals() {
        return new RegisterAutomaton.Globals();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Constants }
     *
     */
    public RegisterAutomaton.Constants createRegisterAutomatonConstants() {
        return new RegisterAutomaton.Constants();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Alphabet }
     *
     */
    public RegisterAutomaton.Alphabet createRegisterAutomatonAlphabet() {
        return new RegisterAutomaton.Alphabet();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Alphabet.Outputs }
     *
     */
    public RegisterAutomaton.Alphabet.Outputs createRegisterAutomatonAlphabetOutputs() {
        return new RegisterAutomaton.Alphabet.Outputs();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Alphabet.Outputs.Symbol }
     *
     */
    public RegisterAutomaton.Alphabet.Outputs.Symbol createRegisterAutomatonAlphabetOutputsSymbol() {
        return new RegisterAutomaton.Alphabet.Outputs.Symbol();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Alphabet.Inputs }
     *
     */
    public RegisterAutomaton.Alphabet.Inputs createRegisterAutomatonAlphabetInputs() {
        return new RegisterAutomaton.Alphabet.Inputs();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Alphabet.Inputs.Symbol }
     *
     */
    public RegisterAutomaton.Alphabet.Inputs.Symbol createRegisterAutomatonAlphabetInputsSymbol() {
        return new RegisterAutomaton.Alphabet.Inputs.Symbol();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Transitions.Transition.Assignments.Assign
     * }
     *
     */
    public RegisterAutomaton.Transitions.Transition.Assignments.Assign createRegisterAutomatonTransitionsTransitionAssignmentsAssign() {
        return new RegisterAutomaton.Transitions.Transition.Assignments.Assign();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Locations.Location }
     *
     */
    public RegisterAutomaton.Locations.Location createRegisterAutomatonLocationsLocation() {
        return new RegisterAutomaton.Locations.Location();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Globals.Variable }
     *
     */
    public RegisterAutomaton.Globals.Variable createRegisterAutomatonGlobalsVariable() {
        return new RegisterAutomaton.Globals.Variable();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Constants.Constant }
     *
     */
    public RegisterAutomaton.Constants.Constant createRegisterAutomatonConstantsConstant() {
        return new RegisterAutomaton.Constants.Constant();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Alphabet.Outputs.Symbol.Param
     * }
     *
     */
    public RegisterAutomaton.Alphabet.Outputs.Symbol.Param createRegisterAutomatonAlphabetOutputsSymbolParam() {
        return new RegisterAutomaton.Alphabet.Outputs.Symbol.Param();
    }

    /**
     * Create an instance of {@link RegisterAutomaton.Alphabet.Inputs.Symbol.Param
     * }
     *
     */
    public RegisterAutomaton.Alphabet.Inputs.Symbol.Param createRegisterAutomatonAlphabetInputsSymbolParam() {
        return new RegisterAutomaton.Alphabet.Inputs.Symbol.Param();
    }

}
