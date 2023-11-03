/* Copyright (C) 2013-2023 TU Dortmund
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

import net.automatalib.word.Word;

/**
 * Alphabet definition for visible push-down automata. Partitions the overall input alphabet into call-, internal-, and
 * return symbols.
 *
 * @param <I>
 *         input alphabet type
 */
public interface VPAlphabet<I> extends Alphabet<I> {

    /**
     * Returns the call symbols of {@code this} alphabet as a (sub-) alphabet.
     *
     * @return the call symbols of {@code this} alphabet
     */
    Alphabet<I> getCallAlphabet();

    /**
     * The {@link Alphabet#getSymbol(int)} variant for the call alphabet. Note that the index must be relative to the
     * {@link #getCallAlphabet() call alphabet} and not to {@code this} alphabet.
     *
     * @param index
     *         the index of the symbol
     *
     * @return the call symbol at the given index
     *
     * @throws IllegalArgumentException
     *         if there is no symbol with this index
     */
    I getCallSymbol(int index);

    /**
     * The {@link Alphabet#getSymbolIndex(Object)} variant for the call alphabet. Note that the index is relative to the
     * {@link #getCallAlphabet() call alphabet} and not to {@code this} alphabet.
     *
     * @param symbol
     *         the symbol whose index should be determined
     *
     * @return the index of the given call symbol
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the call alphabet.
     */
    int getCallSymbolIndex(I symbol);

    /**
     * The {@link Alphabet#size()} variant for the call alphabet.
     *
     * @return the number of call symbols
     */
    int getNumCalls();

    /**
     * Returns the internal symbols of {@code this} alphabet as a (sub-) alphabet.
     *
     * @return the internal symbols of {@code this} alphabet
     */
    Alphabet<I> getInternalAlphabet();

    /**
     * The {@link Alphabet#getSymbol(int)} variant for the internal alphabet. Note that the index must be relative to
     * the {@link #getInternalAlphabet() internal alphabet} and not to {@code this} alphabet.
     *
     * @param index
     *         the index of the symbol
     *
     * @return the internal symbol at the given index
     *
     * @throws IllegalArgumentException
     *         if there is no symbol with this index
     */
    I getInternalSymbol(int index);

    /**
     * The {@link Alphabet#getSymbolIndex(Object)} variant for the internal alphabet. Note that the index is relative to
     * the {@link #getInternalAlphabet() internal alphabet} and not to {@code this} alphabet.
     *
     * @param symbol
     *         the symbol whose index should be determined
     *
     * @return the index of the given internal symbol
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the internal alphabet.
     */
    int getInternalSymbolIndex(I symbol);

    /**
     * The {@link Alphabet#size()} variant for the internal alphabet.
     *
     * @return the number of internal symbols
     */
    int getNumInternals();

    /**
     * Returns the return symbols of {@code this} alphabet as a (sub-) alphabet.
     *
     * @return the return symbols of {@code this} alphabet
     */
    Alphabet<I> getReturnAlphabet();

    /**
     * The {@link Alphabet#getSymbol(int)} variant for the return alphabet. Note that the index must be relative to the
     * {@link #getReturnAlphabet() return alphabet} and not to {@code this} alphabet.
     *
     * @param index
     *         the index of the symbol
     *
     * @return the return symbol at the given index
     *
     * @throws IllegalArgumentException
     *         if there is no symbol with this index
     */
    I getReturnSymbol(int index);

    /**
     * The {@link Alphabet#getSymbolIndex(Object)} variant for the return alphabet. Note that the index is relative to
     * the {@link #getReturnAlphabet() return alphabet} and not to {@code this} alphabet.
     *
     * @param symbol
     *         the symbol whose index should be determined
     *
     * @return the index of the given return symbol
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the return alphabet.
     */
    int getReturnSymbolIndex(I symbol);

    /**
     * The {@link Alphabet#size()} variant for the return alphabet.
     *
     * @return the number of return symbols
     */
    int getNumReturns();

    /**
     * Returns the {@link SymbolType symbol type} of the given alphabet symbol.
     *
     * @param symbol
     *         the symbol whose type should be returned
     *
     * @return the {@link SymbolType symbol type} of the given alphabet symbol.
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the alphabet.
     */
    SymbolType getSymbolType(I symbol);

    /**
     * Returns whether the given symbol is a call symbol of {@code this} alphabet.
     *
     * @param symbol
     *         the symbol to analyze
     *
     * @return {@code true} if the given symbol is a call symbol of this alphabet, {@code false} otherwise
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the alphabet.
     */
    default boolean isCallSymbol(I symbol) {
        return getSymbolType(symbol) == SymbolType.CALL;
    }

    /**
     * Returns whether the given symbol is an internal symbol of {@code this} alphabet.
     *
     * @param symbol
     *         the symbol to analyze
     *
     * @return {@code true} if the given symbol is an internal symbol of this alphabet, {@code false} otherwise
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the alphabet.
     */
    default boolean isInternalSymbol(I symbol) {
        return getSymbolType(symbol) == SymbolType.INTERNAL;
    }

    /**
     * Returns whether the given symbol is a return symbol of {@code this} alphabet.
     *
     * @param symbol
     *         the symbol to analyze
     *
     * @return {@code true} if the given symbol is a return symbol of this alphabet, {@code false} otherwise
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the alphabet.
     */
    default boolean isReturnSymbol(I symbol) {
        return getSymbolType(symbol) == SymbolType.RETURN;
    }

    /**
     * Returns the call-return balance of the given word relative to this alphabet. The call-return balance is positive,
     * if the given word contains more call symbols than return symbols, negative if the given word contains more return
     * symbols than call symbols, and 0 if it contains an equal number of call symbols and return symbols.
     *
     * @param word
     *         the word to analyze
     *
     * @return the call-return balance
     */
    default int callReturnBalance(Word<I> word) {
        int crb = 0;
        for (I sym : word) {
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    break;
                case RETURN:
                    crb--;
                    break;
                default:
            }
        }
        return crb;
    }

    /**
     * Returns whether the given word is call-matched relative to {@code this} alphabet. A word is call-matched if every
     * call symbol is at one point succeeded by a matching return symbol and there exist no un-matched call symbols.
     * Note that a call-matched word may still contain un-matched return symbols.
     *
     * @param word
     *         the word to analyze
     *
     * @return {@code true} if the given word is call-matched, {@code false} otherwise
     */
    default boolean isCallMatched(Word<I> word) {
        int crb = 0;
        for (I sym : word) {
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    break;
                case RETURN:
                    if (crb > 0) {
                        crb--;
                    }
                    break;
                default:
            }
        }
        return crb == 0;
    }

    /**
     * Returns whether the given word is return-matched relative to {@code this} alphabet. A word is return-matched if
     * every return symbol is at one point preceded by a matching call symbol and there exist no un-matched return
     * symbols. Note that a return-matched word may still contain un-matched call symbols.
     *
     * @param word
     *         the word to analyze
     *
     * @return {@code true} if the given word is return-matched, {@code false} otherwise
     */
    default boolean isReturnMatched(Word<I> word) {
        int crb = 0;
        for (I sym : word) {
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    break;
                case RETURN:
                    crb--;
                    if (crb < 0) {
                        return false;
                    }
                    break;
                default:
            }
        }

        return true;
    }

    /**
     * Returns whether the given word is well-matched relative to {@code this} alphabet. A word is well-matched if every
     * call symbol is at one point succeeded by a matching return symbol and there exist no un-matched call symbols or
     * return symbols.
     *
     * @param word
     *         the word to analyze
     *
     * @return {@code true} if the given word is well-matched, {@code false} otherwise
     */
    default boolean isWellMatched(Word<I> word) {
        int crb = 0;
        for (I sym : word) {
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    break;
                case RETURN:
                    crb--;
                    if (crb < 0) {
                        return false;
                    }
                    break;
                default:
            }
        }

        return crb == 0;
    }

    /**
     * Return the longest {@link #isWellMatched(Word) well-matched} (relative to {@code this} alphabet) prefix of the
     * given word.
     *
     * @param word
     *         the word to analyze
     *
     * @return the longest well-matched prefix of the given word
     */
    default Word<I> longestWellMatchedPrefix(Word<I> word) {
        int idx = 0;
        int len = word.length();
        int crb = 0;
        int lastzero = 0;
        outer:
        while (idx < len) {
            final I sym = word.getSymbol(idx);
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    break;
                case RETURN:
                    crb--;
                    if (crb < 0) {
                        break outer;
                    }
                    break;
                default:
            }
            if (crb == 0) {
                lastzero = idx + 1;
            }
            idx++;
        }
        return word.prefix(lastzero);
    }

    /**
     * Return the longest {@link #isWellMatched(Word) well-matched} (relative to {@code this} alphabet) suffix of the
     * given word.
     *
     * @param word
     *         the word to analyze
     *
     * @return the longest well-matched suffix of the given word
     */
    default Word<I> longestWellMatchedSuffix(Word<I> word) {
        int idx = word.length();
        int crb = 0;
        int lastZero = idx;
        outer:
        while (idx > 0) {
            final I sym = word.getSymbol(--idx);
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    if (crb > 0) {
                        break outer;
                    }
                    break;
                case RETURN:
                    crb--;
                    break;
                default:
            }
            if (crb == 0) {
                lastZero = idx;
            }
        }
        return word.subWord(lastZero);
    }

    /**
     * Classifies an input symbol either as a call symbol, an internal symbol, or a return symbol.
     */
    enum SymbolType {
        CALL,
        INTERNAL,
        RETURN
    }

}
