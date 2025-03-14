/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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

import java.io.StreamTokenizer;

import net.automatalib.exception.FormatException;

/**
 * Exception that may be thrown whenever an FSM is illegal.
 */
class FSMFormatException extends FormatException {

    public static final String MESSAGE = "Unable to parse FSM: %s at line %d";

    FSMFormatException(String message, StreamTokenizer streamTokenizer) {
        super(String.format(MESSAGE, message, streamTokenizer.lineno()));
    }

    FSMFormatException(Exception e, StreamTokenizer streamTokenizer) {
        super(String.format(MESSAGE, e.getMessage(), streamTokenizer.lineno()));
    }
}
