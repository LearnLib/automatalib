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
package net.automatalib.words;

import java.util.AbstractList;
import java.util.List;

import net.automatalib.commons.util.array.ResizingObjectArray;

/**
 * A class for dynamically building {@link Word}s.
 * <p>
 * As {@link Word}s are - like strings - immutable objects, constructing them by subsequent invocations of {@link
 * Word#concat(Word...)} etc. is highly inefficient. This class provides an efficient means of construction by operating
 * on an internal storage during construction, only creating a {@link Word} (and thus requiring to ensure immutability)
 * when the method {@link #toWord()} (or {@link #toWord(int, int)}) is invoked.
 * <p>
 * Note that due to the specifics of the underlying word implementation, even after an invocation of {@link #toWord()}
 * the storage does not have to be duplicated unless it either is required due to capacity adjustment <i>or</i> a
 * non-appending change (such as {@link #setSymbol(int, Object)} or {@link #truncate(int)}) is made.
 * <p>
 * Nearly all modification methods of this class return a <tt>this</tt>-reference, allowing constructs such as {@code
 * builder.append(foo).append(bar).append(baz)}.
 *
 * @param <I>
 *         symbol class.
 *
 * @author Malte Isberner
 */
public final class WordBuilder<I> extends AbstractList<I> {

    private final ResizingObjectArray storage;
    private Object[] array;
    private int length;
    private boolean lock;

    /**
     * Constructor. Initializes the builder with a default capacity.
     */
    public WordBuilder() {
        this.storage = new ResizingObjectArray();
        this.array = this.storage.array;
    }

    /**
     * Constructor. Initializes the builder with the specified initial capacity.
     *
     * @param initialCapacity
     *         the initial capacity of the internal storage.
     */
    public WordBuilder(int initialCapacity) {
        this.storage = new ResizingObjectArray(initialCapacity);
        this.array = this.storage.array;
    }

    /**
     * Constructor. Initializes the builder with a sequence of <tt>count</tt> times the specified symbol. Note that this
     * constructor runs in constant time if <tt>initSym</tt> is <tt>null</tt>.
     *
     * @param initSym
     *         the initial symbol
     * @param count
     *         the initial symbol count
     */
    public WordBuilder(I initSym, int count) {
        this.storage = new ResizingObjectArray(count);
        this.array = this.storage.array;
        if (initSym != null) {
            for (int i = 0; i < count; i++) {
                array[i] = initSym;
            }
        }
        length = count;
    }

    /**
     * Constructor. Initializes the builder with a sequence of <tt>count</tt> times the specified symbol, while
     * allocating the specified initial capacity.
     *
     * @param capacity
     *         the initial capacity of the internal storage.
     * @param initSym
     *         the initial symbol
     * @param count
     *         the initial symbol count
     */
    public WordBuilder(int capacity, I initSym, int count) {
        this.storage = new ResizingObjectArray(Math.max(capacity, count));
        this.array = this.storage.array;
        if (initSym != null) {
            for (int i = 0; i < count; i++) {
                array[i] = initSym;
            }
        }
        length = count;
    }

    /**
     * Constructor. Initializes the builder with a given word.
     *
     * @param init
     *         the word to initialize the builder with.
     */
    public WordBuilder(Word<I> init) {
        int wLen = init.length();
        this.storage = new ResizingObjectArray(wLen);
        this.array = this.storage.array;
        init.writeToArray(0, array, 0, wLen);
        length = wLen;
    }

    /**
     * Constructor. Initializes the builder with a given word, while allocating the specified initial capacity.
     *
     * @param capacity
     *         the initial capacity to use.
     * @param init
     *         the initial word
     */
    public WordBuilder(int capacity, Word<I> init) {
        int wLen = init.length();
        this.storage = new ResizingObjectArray(Math.max(capacity, wLen));
        this.array = this.storage.array;
        init.writeToArray(0, array, 0, wLen);
        length = wLen;
    }

    /**
     * Appends a symbol to the contents of the internal storage.
     *
     * @param symbol
     *         the symbol to append
     *
     * @return <tt>this</tt>
     */
    public WordBuilder<I> append(I symbol) {
        ensureAdditionalCapacity(1);
        array[length++] = symbol;
        return this;
    }

    public WordBuilder<I> append(List<? extends I> symList) {
        int lLen = symList.size();
        ensureAdditionalCapacity(lLen);
        for (I sym : symList) {
            array[length++] = sym;
        }
        return this;
    }

    /**
     * Appends a word to the contents of the internal storage.
     *
     * @param word
     *         the word to append.
     *
     * @return <tt>this</tt>
     */
    public WordBuilder<I> append(Word<? extends I> word) {
        int wLen = word.length();
        ensureAdditionalCapacity(wLen);
        word.writeToArray(0, array, length, wLen);
        length += wLen;
        return this;
    }

    /**
     * Appends several words to the contents of the internal storage.
     *
     * @param words
     *         the words to append
     *
     * @return <tt>this</tt>
     */
    @SafeVarargs
    public final WordBuilder<I> append(Word<? extends I>... words) {
        if (words.length == 0) {
            return this;
        }

        int allLen = 0;
        for (Word<? extends I> w : words) {
            allLen += w.length();
        }

        ensureAdditionalCapacity(allLen);

        for (Word<? extends I> word : words) {
            int wLen = word.length();
            word.writeToArray(0, array, length, wLen);
            length += wLen;
        }

        return this;
    }

    /**
     * Appends several symbols to the contents of the internal storage.
     *
     * @param symbols
     *         the symbols to append
     *
     * @return <tt>this</tt>
     */
    @SafeVarargs
    public final WordBuilder<I> append(I... symbols) {
        if (symbols.length == 0) {
            return this;
        }
        ensureAdditionalCapacity(symbols.length);
        System.arraycopy(symbols, 0, array, length, symbols.length);
        length += symbols.length;
        return this;
    }

    /**
     * Ensures that the internal storage has <b>additionally</b> the given capacity.
     *
     * @param add
     *         the additional capacity to ensure
     */
    public void ensureAdditionalCapacity(int add) {
        ensureCapacity(length + add);
    }

    /**
     * Ensures that the internal storage has in total the given capacity.
     *
     * @param cap
     *         the minimum capacity to ensure
     */
    public void ensureCapacity(int cap) {
        if (storage.ensureCapacity(cap)) {
            lock = false;
            array = storage.array;
        }
    }

    /**
     * Appends <tt>num</tt> copies of the given word to the contents of the initial storage.
     *
     * @param num
     *         the number of copies
     * @param word
     *         the word
     *
     * @return <tt>this</tt>
     */
    public WordBuilder<I> repeatAppend(int num, Word<I> word) {
        if (num == 0) {
            return this;
        }

        int wLen = word.length();
        int allLen = wLen * num;

        ensureAdditionalCapacity(allLen);

        for (int i = num; i > 0; i--) {
            word.writeToArray(0, array, length, wLen);
            length += wLen;
        }

        return this;
    }

    /**
     * Appends <tt>num</tt> copies of a symbol to the contents of the internal storage.
     *
     * @param num
     *         the number of copies
     * @param symbol
     *         the symbol
     *
     * @return <tt>this</tt>
     */
    public WordBuilder<I> repeatAppend(int num, I symbol) {
        if (num == 0) {
            return this;
        }

        ensureAdditionalCapacity(num);
        if (symbol == null) {
            length += num;
        } else {
            for (int i = num; i > 0; i--) {
                array[length++] = symbol;
            }
        }
        return this;
    }

    /**
     * Truncates the contents of the initial storage to the given length.
     *
     * @param truncLen
     *         the length to truncate to
     *
     * @return <tt>this</tt>
     */
    public WordBuilder<I> truncate(int truncLen) {
        if (truncLen >= length) {
            return this;
        }

        ensureUnlocked();
        for (int i = truncLen; i < length; i++) {
            array[i] = null;
        }

        length = truncLen;

        return this;
    }

    /*
     * Ensure that non-appending modifications may be made
     */
    private void ensureUnlocked() {
        if (lock) {
            array = array.clone();
            storage.array = array;
            lock = false;
        }
    }

    /**
     * Creates a word from the given range of the contents of the internal storage. Note that the storage management
     * mechanisms of this class guarantee that the returned word will not change regardless of what further operations
     * are invoked on this {@link WordBuilder}.
     *
     * @param fromIndex
     *         the starting index, inclusive
     * @param toIndex
     *         the end index, exclusive
     *
     * @return the word for the specified subrange
     */
    public Word<I> toWord(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > length) {
            throw new IndexOutOfBoundsException();
        }
        int len = toIndex - fromIndex;

        lock = true;
        return new SharedWord<>(array, fromIndex, len);
    }

    /**
     * Creates a word from the contents of the internal storage. Note that the storage management mechanisms of this
     * class guarantee that the returned word will not change regardless of what further operations are performed on
     * this {@link WordBuilder}.
     *
     * @return the internal contents as a word
     */
    public Word<I> toWord() {
        lock = true;
        return new SharedWord<>(array, 0, length);
    }

    @Override
    public boolean add(I e) {
        append(e);
        return true;
    }

    @Override
    public I get(int index) {
        return getSymbol(index);
    }

    /**
     * Retrieves the symbol at the given index.
     *
     * @param index
     *         the index to retrieve
     *
     * @return the symbol at the given index
     */
    @SuppressWarnings("unchecked")
    public I getSymbol(int index) {
        return (I) array[index];
    }

    @Override
    public I set(int index, I element) {
        I old = getSymbol(index);
        setSymbol(index, element);
        return old;
    }

    /**
     * Sets the symbol at the given index. Note that this index must exist.
     *
     * @param index
     *         the index to manipulate
     * @param symbol
     *         the symbol to set
     *
     * @return <tt>this</tt>
     */
    public WordBuilder<I> setSymbol(int index, I symbol) {
        ensureUnlocked();
        array[index] = symbol;
        return this;
    }

    @Override
    public void clear() {
        ensureUnlocked();
        for (int i = 0; i < length; i++) {
            array[i] = null;
        }
        length = 0;
    }

    @Override
    public int size() {
        return length;
    }

    /**
     * Reverses the contents of the internal buffer.
     *
     * @return {@code this}
     */
    public WordBuilder<I> reverse() {
        ensureUnlocked();
        int lowIdx = 0, highIdx = length - 1;

        while (lowIdx < highIdx) {
            Object tmp = array[lowIdx];
            array[lowIdx++] = array[highIdx];
            array[highIdx--] = tmp;
        }

        return this;
    }

}
