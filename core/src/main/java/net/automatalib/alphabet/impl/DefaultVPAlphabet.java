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
package net.automatalib.alphabet.impl;

import java.util.Arrays;
import java.util.Collection;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.VPAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An alphabet-based, fixed size implementation of a {@link VPAlphabet}.
 */
public class DefaultVPAlphabet<I> extends AbstractVPAlphabet<I> implements VPAlphabet<I> {

    private final @Nullable Object[] globalSymbolCache;

    public DefaultVPAlphabet(Collection<I> internalSymbols, Collection<I> callSymbols, Collection<I> returnSymbols) {
        this(Alphabets.fromCollection(internalSymbols),
             Alphabets.fromCollection(callSymbols),
             Alphabets.fromCollection(returnSymbols));
    }

    // False positive, because our intended semantic is currently not supported by CF (https://github.com/typetools/checker-framework/issues/3760)
    @SuppressWarnings({"method.invocation", "methodref.receiver.bound"})
    public DefaultVPAlphabet(Alphabet<I> internalAlphabet, Alphabet<I> callAlphabet, Alphabet<I> returnAlphabet) {
        super(internalAlphabet, callAlphabet, returnAlphabet);
        this.globalSymbolCache = new Object[super.size()];
        Arrays.setAll(this.globalSymbolCache, super::getSymbol);
    }

    @Override
    @SuppressWarnings("unchecked")
    public I getSymbol(int index) {
        if (index < globalSymbolCache.length) {
            return (I) globalSymbolCache[index];
        }
        throw new IllegalArgumentException();
    }
}
