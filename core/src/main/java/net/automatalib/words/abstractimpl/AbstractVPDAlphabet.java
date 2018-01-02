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
package net.automatalib.words.abstractimpl;

import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;

/**
 * Abstract utility class that implements functionality shared across different subtypes.
 *
 * @author frohme
 */
public abstract class AbstractVPDAlphabet<I> extends AbstractAlphabet<I> implements VPDAlphabet<I> {

    @Override
    public Alphabet<I> getCallAlphabet() {
        return new AbstractAlphabet<I>() {

            @Override
            public I getSymbol(final int index) throws IllegalArgumentException {
                return getCallSymbol(index);
            }

            @Override
            public int size() {
                return getNumCalls();
            }

            @Override
            public int getSymbolIndex(final I symbol) throws IllegalArgumentException {
                return getCallSymbolIndex(symbol);
            }

        };
    }

    @Override
    public Alphabet<I> getInternalAlphabet() {
        return new AbstractAlphabet<I>() {

            @Override
            public I getSymbol(final int index) throws IllegalArgumentException {
                return getInternalSymbol(index);
            }

            @Override
            public int getSymbolIndex(final I symbol) throws IllegalArgumentException {
                return getInternalSymbolIndex(symbol);
            }

            @Override
            public int size() {
                return getNumInternals();
            }
        };
    }

    @Override
    public Alphabet<I> getReturnAlphabet() {
        return new AbstractAlphabet<I>() {

            @Override
            public I getSymbol(final int index) throws IllegalArgumentException {
                return getReturnSymbol(index);
            }

            @Override
            public int getSymbolIndex(final I symbol) throws IllegalArgumentException {
                return getReturnSymbolIndex(symbol);
            }

            @Override
            public int size() {
                return getNumReturns();
            }
        };
    }

}
