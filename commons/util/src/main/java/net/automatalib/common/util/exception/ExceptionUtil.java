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
package net.automatalib.common.util.exception;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility methods for {@link Exception}s.
 */
public final class ExceptionUtil {

    private ExceptionUtil() {
        // prevent instantiation
    }

    /**
     * Throws the given throwable if it is an unchecked exception.
     *
     * @param throwable
     *         the throwable to analyse
     */
    public static void throwIfUnchecked(@Nullable Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
    }
}
