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

/**
 * Default implementation of a {@link ProceduralInputAlphabet}.
 *
 * @param <I>
 *         input symbol type
 */
public class DefaultProceduralInputAlphabet<I> extends DefaultVPAlphabet<I> implements ProceduralInputAlphabet<I> {

    private final Alphabet<I> proceduralAlphabet;

    public DefaultProceduralInputAlphabet(Alphabet<I> internalAlphabet, Alphabet<I> callAlphabet, I returnSymbol) {
        super(internalAlphabet, callAlphabet, Alphabets.singleton(returnSymbol));
        this.proceduralAlphabet = Alphabets.fromCollections(internalAlphabet, callAlphabet);
    }

    @Override
    public Alphabet<I> getProceduralAlphabet() {
        return this.proceduralAlphabet;
    }
}
