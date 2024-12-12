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
package net.automatalib.util.automaton.minimizer;

/**
 * A utility interface to store automaton properties during an invasive minimization.
 *
 * @param <T>
 *         type parameter
 */
interface Storage<T> {

    /**
     * Initializes the storage.
     *
     * @param n
     *         the number of elements to store
     */
    void init(int n);

    /**
     * Sets the value of a given element.
     *
     * @param n
     *         the element id
     * @param val
     *         the value that should be associated with the id
     */
    void set(int n, T val);

    /**
     * Gets the value of a given element.
     *
     * @param i
     *         the element id
     *
     * @return the value previously associated with the id
     */
    T get(int i);

    /**
     * A {@link Storage} implementation that stores generics values in an object array.
     *
     * @param <T>
     *         type parameter
     */
    class GenericStorage<T> implements Storage<T> {

        private T[] array;

        @SuppressWarnings("unchecked")
        @Override
        public void init(int n) {
            this.array = (T[]) new Object[n];
        }

        @Override
        public void set(int n, T val) {
            this.array[n] = val;
        }

        @Override
        public T get(int i) {
            return this.array[i];
        }
    }

    /**
     * A {@link Storage} implementation that stores boolean values in a primitive array.
     */
    class BooleanStorage implements Storage<Boolean> {

        private boolean[] array;

        @Override
        public void init(int n) {
            this.array = new boolean[n];
        }

        @Override
        public void set(int n, Boolean val) {
            this.array[n] = val;
        }

        @Override
        public Boolean get(int i) {
            return this.array[i];
        }
    }

    /**
     * A {@link Storage} implementation that does not store any values.
     */
    class VoidStorage implements Storage<Void> {

        @Override
        public void init(int n) {}

        @Override
        public void set(int n, Void val) {}

        @Override
        public Void get(int i) {
            return null;
        }
    }
}
