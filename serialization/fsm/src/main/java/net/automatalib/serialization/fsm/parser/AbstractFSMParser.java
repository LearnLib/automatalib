/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.serialization.fsm.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * This class provides methods to parse automata in FSM format.
 *
 * The FSM is parsed by means of a tokenizer (a grammar is not used).
 *
 * @see <a href="http://www.win.tue.nl/vis1/home/apretori/data/fsm.html">the FSM format</a>
 *
 * @author Jeroen Meijer
 */
public abstract class AbstractFSMParser<I> {

    /**
     * An enumeration for the three parts in the FSM file.
     */
    protected enum Part {DataDefinition, StateVectors, Transitions}

    // some messages for FSMParseExceptions.
    public static final String NO_SUCH_STATE = "state with number %d is undefined";
    public static final String NON_DETERMINISM_DETECTED = "non-determinism detected (previous value: %s)";
    public static final String EXPECT_CHAR = "expected char '%c' not found";
    public static final String EXPECT_NUMBER = "number expected";
    public static final String EXPECT_IDENTIFIER= "expecting identifier";
    public static final String EXPECT_STRING = "expecting string";

    private final Reader reader;

    /**
     * The function that transforms strings in the FSM file to input.
     */
    private final Function<String, I> inputParser;

    /**
     * The current line that is being parsed in the current part.
     */
    private int partLineNumber;

    /**
     * The StreamTokenizer, that tokenizes tokens in the FSM file.
     */
    private final StreamTokenizer streamTokenizer;

    /**
     * The set that contains all inputs that end up in the input alphabet.
     */
    private final Set<I> inputs = new HashSet<>();

    /**
     * Constructs a new AbstractFSMParser and defines all possible tokens.
     *
     * @param reader the Reader
     * @param inputParser the Function that parses strings in the FSM file to input.
     */
    protected AbstractFSMParser(Reader reader, Function<String, I> inputParser) {
        this.inputParser = inputParser;
        this.reader = reader;
        streamTokenizer = new StreamTokenizer(reader);
        streamTokenizer.resetSyntax();
        streamTokenizer.wordChars('a', 'z');
        streamTokenizer.wordChars('A', 'Z');
        streamTokenizer.wordChars('-', '-');
        streamTokenizer.wordChars('_', '_');
        streamTokenizer.wordChars('0', '9');
        streamTokenizer.wordChars('á', ' ');
        streamTokenizer.whitespaceChars(0, ' ');
        streamTokenizer.quoteChar('"');
        streamTokenizer.eolIsSignificant(true);
        streamTokenizer.ordinaryChar('(');
        streamTokenizer.ordinaryChar(')');
    }

    /**
     * Gets the function that transforms strings in the FSM file to input.
     *
     * @return the function.
     */
    protected Function<String, I> getInputParser() {
        return inputParser;
    }

    /**
     * Returns the line number that is being parsed in the current part.
     *
     * @return the line number.
     */
    protected int getPartLineNumber() {
        return partLineNumber;
    }

    /**
     * Gets the StreamTokenizer, that tokenizes tokens in the FSM file.
     *
     * @return the StreamTokenizer.
     */
    protected StreamTokenizer getStreamTokenizer() {
        return streamTokenizer;
    }

    /**
     * Gets the set that contains all inputs that end up in the input alphabet.
     *
     * @return the set of inputs.
     */
    protected Set<I> getInputs() {
        return inputs;
    }

    /**
     * Parse a data definition.
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void parseDataDefinition() throws FSMParseException, IOException;

    /**
     * Perform some actions after all data definitions have been parsed.
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void checkDataDefinitions() throws FSMParseException, IOException;

    /**
     * Parse a state vector.
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void parseStateVector() throws FSMParseException, IOException;

    /**
     * Perform some actions after all state vectors have been parsed.
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void checkStateVectors() throws FSMParseException, IOException;

    /**
     * Parse a transition.
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void parseTransition() throws FSMParseException, IOException;

    /**
     * Perform some actions after all transitions have been parsed.
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void checkTransitions() throws FSMParseException, IOException;

    /**
     * Parsed the FSM file line-by-line.
     * At first this method expects to parse data definitions, and calls {@link #parseDataDefinition()} for each data
     * definition. After "---" is encountered {@link #checkDataDefinitions()} is called, and this method expects to
     * parse state vectors. The behavior is similar for state vectors and transitions.
     * For each line this method will increment {@link #partLineNumber}, and reset it when a new part in the FSM file
     * begins.
     *
     * Note that {@link StreamTokenizer} allows one to push back tokens. This is used whenever we have checked type
     * type of token we are going to read.
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected void parse() throws FSMParseException, IOException {
        Part part = Part.DataDefinition;
        partLineNumber = 0;

        while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            streamTokenizer.pushBack();
            switch (part) {
                case DataDefinition: {
                    if (streamTokenizer.nextToken() == StreamTokenizer.TT_WORD && "---".equals(streamTokenizer.sval)) {
                        // we entered the part with the state vectors
                        part = Part.StateVectors;
                        partLineNumber = 0;
                        checkDataDefinitions();
                    } else {
                        streamTokenizer.pushBack();
                        parseDataDefinition();
                    }
                    break;
                }
                case StateVectors: {
                    if (streamTokenizer.nextToken() == StreamTokenizer.TT_WORD && "---".equals(streamTokenizer.sval)) {
                        // we entered the part with the transitions.
                        part = Part.Transitions;
                        partLineNumber = 0;
                        checkStateVectors();
                    } else {
                        streamTokenizer.pushBack();
                        parseStateVector();
                    }
                    break;
                }
                case Transitions: {
                    parseTransition();
                    break;
                }
                default: throw new AssertionError();
            }
            // consume all tokens until EOL is reached
            while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOL) {}
            partLineNumber++;
        }
        checkTransitions();
        reader.close();
    }
}
