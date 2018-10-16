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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * This class provides methods to parse automata in FSM format.
 *
 * The FSM is parsed by means of a tokenizer (a grammar is not used).
 *
 * @see <a href="http://www.win.tue.nl/vis1/home/apretori/data/fsm.html">the FSM format</a>
 *
 * @author Jeroen Meijer
 */
@ParametersAreNonnullByDefault
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

    /**
     * The function that transforms strings in the FSM file to input.
     */
    private final Function<String, I> inputParser;

    /**
     * The current line that is being parsed in the current part.
     */
    private int partLineNumber;

    /**
     * The set that contains all inputs that end up in the input alphabet (read from the file).
     */
    private final Set<I> inputs = new HashSet<>();

    /**
     * The set that contains all inputs that end up in the input alphabet (provided by the user, may be {@code null}).
     */
    @Nullable
    protected final Collection<? extends I> targetInputs;

    /**
     * Constructs a new AbstractFSMParser and defines all possible tokens.
     *
     * @param targetInputs
     *         An collection containing the inputs which should constitute the input alphabet of the parsed automaton.
     *         If {@code null}, the inputs will be automatically gathered from the read FSM file.
     * @param inputParser the Function that parses strings in the FSM file to input.
     */
    protected AbstractFSMParser(@Nullable Collection<? extends I> targetInputs, Function<String, I> inputParser) {
        this.inputParser = inputParser;
        this.targetInputs = targetInputs;
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
     * @param reader
     *         the source of the FSM file
     *
     * @return the StreamTokenizer.
     */
    protected StreamTokenizer getStreamTokenizer(Reader reader) {
        final StreamTokenizer streamTokenizer = new StreamTokenizer(reader);
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
     * @param streamTokenizer
     *         tokenizer containing the input
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void parseDataDefinition(StreamTokenizer streamTokenizer) throws FSMParseException, IOException;

    /**
     * Perform some actions after all data definitions have been parsed.
     *
     * @param streamTokenizer
     *         tokenizer containing the input
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void checkDataDefinitions(StreamTokenizer streamTokenizer) throws FSMParseException, IOException;

    /**
     * Parse a state vector.
     *
     * @param streamTokenizer
     *         tokenizer containing the input
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void parseStateVector(StreamTokenizer streamTokenizer) throws FSMParseException, IOException;

    /**
     * Perform some actions after all state vectors have been parsed.
     *
     * @param streamTokenizer
     *         tokenizer containing the input
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void checkStateVectors(StreamTokenizer streamTokenizer) throws FSMParseException, IOException;

    /**
     * Parse a transition.
     *
     * @param streamTokenizer
     *         tokenizer containing the input
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void parseTransition(StreamTokenizer streamTokenizer) throws FSMParseException, IOException;

    /**
     * Perform some actions after all transitions have been parsed.
     *
     * @param streamTokenizer
     *         tokenizer containing the input
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected abstract void checkTransitions(StreamTokenizer streamTokenizer) throws FSMParseException, IOException;

    /**
     * Parsed the FSM file line-by-line.
     * At first this method expects to parse data definitions, and calls {@link #parseDataDefinition(StreamTokenizer)}
     * for each data definition. After "---" is encountered {@link #checkDataDefinitions(StreamTokenizer)} is called,
     * and this method expects to parse state vectors. The behavior is similar for state vectors and transitions.
     * For each line this method will increment {@link #partLineNumber}, and reset it when a new part in the FSM file
     * begins.
     *
     * Note that {@link StreamTokenizer} allows one to push back tokens. This is used whenever we have checked type
     * type of token we are going to read.
     *
     * @param reader
     *         the source of the FSM file
     *
     * @throws FSMParseException when the FSM source is invalid.
     * @throws IOException when FSM source could not be read.
     */
    protected void parse(Reader reader) throws FSMParseException, IOException {
        Part part = Part.DataDefinition;
        partLineNumber = 0;

        final StreamTokenizer streamTokenizer = getStreamTokenizer(reader);

        while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            streamTokenizer.pushBack();
            switch (part) {
                case DataDefinition: {
                    if (streamTokenizer.nextToken() == StreamTokenizer.TT_WORD && "---".equals(streamTokenizer.sval)) {
                        // we entered the part with the state vectors
                        part = Part.StateVectors;
                        partLineNumber = 0;
                        checkDataDefinitions(streamTokenizer);
                    } else {
                        streamTokenizer.pushBack();
                        parseDataDefinition(streamTokenizer);
                    }
                    break;
                }
                case StateVectors: {
                    if (streamTokenizer.nextToken() == StreamTokenizer.TT_WORD && "---".equals(streamTokenizer.sval)) {
                        // we entered the part with the transitions.
                        part = Part.Transitions;
                        partLineNumber = 0;
                        checkStateVectors(streamTokenizer);
                    } else {
                        streamTokenizer.pushBack();
                        parseStateVector(streamTokenizer);
                    }
                    break;
                }
                case Transitions: {
                    parseTransition(streamTokenizer);
                    break;
                }
                default: throw new AssertionError();
            }
            // consume all tokens until EOL is reached
            while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOL) {}
            partLineNumber++;
        }
        checkTransitions(streamTokenizer);
        reader.close();
    }
}
