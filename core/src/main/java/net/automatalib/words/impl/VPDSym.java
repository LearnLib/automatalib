/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.words.impl;

import java.io.Serializable;
import java.util.Objects;

import net.automatalib.words.VPDAlphabet;

/**
 * Utility class used to wrap input symbols of a {@link VPDAlphabet}. Stores additional index information to allow fast
 * index-based access.
 *
 * @param <T>
 *         type of the payload
 *
 * @author Malte Isberner
 */
public final class VPDSym<T> implements Serializable {

    private final VPDAlphabet.SymbolType type;
    private final T userObject;
    private final int localIndex, globalIndex;

    VPDSym(final T userObject, final VPDAlphabet.SymbolType type, final int localIndex, final int globalIndex) {
        this.userObject = userObject;
        this.type = type;
        this.localIndex = localIndex;
        this.globalIndex = globalIndex;
    }

    public VPDAlphabet.SymbolType getType() {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VPDSym)) {
            return false;
        }

        final VPDSym<?> that = (VPDSym<?>) o;
        return localIndex == that.localIndex && globalIndex == that.globalIndex && type == that.type &&
               Objects.equals(userObject, that.userObject);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(userObject);
        result = 31 * result + Objects.hashCode(userObject);
        result = 31 * result + Integer.hashCode(localIndex);
        result = 31 * result + Integer.hashCode(globalIndex);
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(userObject);
    }
}
