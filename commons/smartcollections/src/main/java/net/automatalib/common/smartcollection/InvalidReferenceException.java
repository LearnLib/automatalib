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
package net.automatalib.common.smartcollection;

/**
 * Exception that is thrown if an invalid {@link ElementReference} is used. This can be the case if it refers to a
 * previously removed element, or to an element stored in a different collection.
 * <p>
 * This exception does not need to be caught explicitly.
 */
public class InvalidReferenceException extends IllegalArgumentException {

    /**
     * Constructor.
     *
     * @param message
     *         the error message
     *
     * @see RuntimeException#RuntimeException(String)
     */
    public InvalidReferenceException(String message) {
        super(message);
    }
}
