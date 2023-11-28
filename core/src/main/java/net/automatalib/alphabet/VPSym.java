/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.alphabet;

import net.automatalib.api.alphabet.VPAlphabet;
import net.automatalib.api.alphabet.VPAlphabet.SymbolType;

/**
 * Utility class used to wrap input symbols of a {@link VPAlphabet}. Stores additional index information to allow fast
 * index-based access.
 *
 * @param <T>
 *         type of the payload
 */
public class VPSym<T> {

    private final T userObject;
    private final SymbolType type;
    private final int localIndex, globalIndex;

    VPSym(T userObject, SymbolType type, int localIndex, int globalIndex) {
        this.userObject = userObject;
        this.type = type;
        this.localIndex = localIndex;
        this.globalIndex = globalIndex;
    }

    public VPAlphabet.SymbolType getType() {
        return type;
    }

    public T getUserObject() {
        return userObject;
    }

    public int getLocalIndex() {
        return localIndex;
    }

    public int getGlobalIndex() {
        return globalIndex;
    }

    @Override
    public String toString() {
        return String.valueOf(userObject);
    }
}
