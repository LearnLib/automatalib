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
package net.automatalib.words.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;

/**
 * This alphabet wrapper allows to temporarily hide symbols, so that containment checks (such as
 * {@link Alphabet#containsSymbol(Object)}) return {@code false} even though the symbol is contained in the wrapped
 * alphabet. Adding the hidden symbol to this alphabet will return the correct return values for the corresponding
 * methods (e.g. its index for {@link GrowingAlphabet#addSymbol(Object)}) but not add the symbol to the backing alphabet
 * if the backing alphabet already contained the symbol.
 * <p>
 * <b>Note:</b> For now, this only holds for containment checks and add-methods. Other methods, such as
 * {@link Alphabet#iterator()} or {@link Alphabet#getSymbol(int)}) access the underlying alphabet directly and do not
 * hide symbols.
 * <p>
 * The purpose of this class is to help management of mutable (e.g. {@link GrowingAlphabet growing}) alphabets that are
 * shared across several components (automata, learning algorithms) that use the alphabet state to skip redundant work
 * (e.g. allocate memory only for every unique symbol). Since updates to the wrapped alphabet are directly present at
 * the components that hold a reference, additional update-steps of the components may be wrongfully skipped.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class SymbolHidingAlphabet<I> implements GrowingAlphabet<I>, Serializable {

    private final GrowingAlphabet<I> delegate;

    private I hiddenSymbol;
    private boolean hidden;

    public SymbolHidingAlphabet(GrowingAlphabet<I> alphabet) {
        this.delegate = alphabet;
    }

    /**
     * Hides the given symbols, i.e. after invoking this method, calls to containment methods (e.g.
     * {@link Alphabet#containsSymbol(Object)}) will return {@code false} even if the symbol is contained in the wrapped
     * alphabet.
     *
     * @param symbolToHide
     *         the symbol to hide
     */
    public void hideSymbol(I symbolToHide) {
        this.hiddenSymbol = symbolToHide;
        this.hidden = true;
    }

    /**
     * Unhides the previously hidden symbol, i.e. after invoking this methods all invocations on {@code this} object
     * directly delegate to the wrapped alphabet.
     */
    public void unhide() {
        this.hiddenSymbol = null;
        this.hidden = false;
    }

    private boolean isHidden(Object symbol) {
        return this.hidden && Objects.equals(this.hiddenSymbol, symbol);
    }

    /**
     * Checks if the given alphabet is a mutable (i.e. {@link GrowingAlphabet}) instance and wraps it in a
     * {@link SymbolHidingAlphabet} instance. Otherwise, the given alphabet is returned as-is.
     *
     * @param alphabet
     *         the alphabet to wrap
     * @param <I>
     *         input symbol type
     *
     * @return A {@link SymbolHidingAlphabet} wrapping the given alphabet if the given alphabet is a
     * {@link GrowingAlphabet}, the original alphabet otherwise
     */
    public static <I> Alphabet<I> wrapIfMutable(final Alphabet<I> alphabet) {
        if (alphabet instanceof GrowingAlphabet) {
            return new SymbolHidingAlphabet<>((GrowingAlphabet<I>) alphabet);
        } else {
            return alphabet;
        }
    }

    /**
     * If the given alphabet is a {@link SymbolHidingAlphabet}, the given code block is executed while hiding the given
     * symbol. Otherwise, the code block is run as-is.
     *
     * @param alphabet
     *         the alphabet
     * @param symbol
     *         the symbol to hide
     * @param block
     *         the block to execute
     * @param <I>
     *         input symbol type
     */
    public static <I> void runWhileHiding(final Alphabet<I> alphabet, final I symbol, final Runnable block) {
        if (alphabet instanceof SymbolHidingAlphabet) {
            final SymbolHidingAlphabet<I> asHidingAlphabet = (SymbolHidingAlphabet<I>) alphabet;
            asHidingAlphabet.hideSymbol(symbol);
            block.run();
            asHidingAlphabet.unhide();
        } else {
            block.run();
        }
    }

    /**
     * Returns the wrapped alphabet.
     *
     * @return the wrapped alphabet.
     */
    public GrowingAlphabet<I> getDelegate() {
        return delegate;
    }

    /**
     * Returns the hidden symbol.
     *
     * @return the hidden symbol
     */
    public I getHiddenSymbol() {
        return hiddenSymbol;
    }

    /**
     * Returns whether this wrapper is currently hiding the symbol returned by {@link #getHiddenSymbol()}.
     *
     * @return {@code true} if {@code this} alphabet is currently hiding the symbol returned by
     * {@link #getHiddenSymbol()}, {@code false} otherwise.
     */
    public boolean isHiding() {
        return hidden;
    }

    // Delegated methods

    @Override
    public int addSymbol(@Nullable I a) {
        if (isHidden(a) && this.delegate.containsSymbol(a)) {
            return getSymbolIndex(a);
        }
        return delegate.addSymbol(a);
    }

    @Override
    public I apply(int index) {
        return delegate.apply(index);
    }

    @Override
    @Nullable
    public I getSymbol(int index) throws IllegalArgumentException {
        return delegate.getSymbol(index);
    }

    @Override
    public int applyAsInt(I symbol) {
        return delegate.applyAsInt(symbol);
    }

    @Override
    public int getSymbolIndex(@Nullable I symbol) throws IllegalArgumentException {
        return delegate.getSymbolIndex(symbol);
    }

    @Override
    public int compare(I o1, I o2) {
        return delegate.compare(o1, o2);
    }

    @Override
    public void writeToArray(int offset, Object[] array, int tgtOfs, int num) {
        delegate.writeToArray(offset, array, tgtOfs, num);
    }

    @Override
    public <I2> Mapping<I2, I> translateFrom(Alphabet<I2> other) {
        return delegate.translateFrom(other);
    }

    @Override
    public boolean containsSymbol(I symbol) {
        return !isHidden(symbol) && delegate.containsSymbol(symbol);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return !isHidden(o) && delegate.contains(o);
    }

    @Override
    public Iterator<I> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(I i) {
        return (isHidden(i) && this.delegate.containsSymbol(i)) || delegate.add(i);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().noneMatch(this::isHidden) && delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends I> c) {
        final List<I> cache = new ArrayList<>(c.size());
        boolean foundHidden = false;

        for (final I i : c) {
            if (this.isHidden(i)) {
                foundHidden = true;
            } else {
                cache.add(i);
            }
        }

        return this.delegate.addAll(cache) || foundHidden;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super I> filter) {
        return delegate.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        this.delegate.clear();
    }
}
