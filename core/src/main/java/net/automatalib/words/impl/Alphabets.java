/* Copyright (C) 2013-2017 TU Dortmund
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;

public final class Alphabets {

    private Alphabets() {
        // prevent instantiation
    }

    @SafeVarargs
    public static <T> Alphabet<T> fromArray(T... symbols) {
        return new ArrayAlphabet<>(symbols);
    }

    public static <E extends Enum<E>> Alphabet<E> fromEnum(Class<E> enumClazz) {
        return fromEnum(enumClazz, false);
    }

    public static <E extends Enum<E>> Alphabet<E> fromEnum(Class<E> enumClazz, boolean withNull) {
        return new EnumAlphabet<>(enumClazz, withNull);
    }

    public static Alphabet<Integer> integers(int startInclusive, int endInclusive) {
        List<Integer> lst = CollectionsUtil.intRange(startInclusive, endInclusive + 1);
        return fromList(lst);
    }

    public static <T> Alphabet<T> fromList(List<? extends T> list) {
        return new ListAlphabet<>(list);
    }

    public static Alphabet<Character> characters(char startInclusive, char endInclusive) {
        List<Character> lst = CollectionsUtil.charRange(startInclusive, (char) (endInclusive + 1));
        return fromList(lst);
    }

    public static Alphabet<String> closedCharStringRange(char startInclusive, char endInclusive) {
        List<String> lst = CollectionsUtil.charStringRange(startInclusive, (char) (endInclusive + 1));
        return fromList(lst);
    }

    public static <T> Alphabet<T> singleton(T symbol) {
        return fromList(Collections.singletonList(symbol));
    }

    @SuppressWarnings("unchecked")
    public static <I> Alphabet<I> fromCollection(Collection<? extends I> coll) {
        if (coll instanceof Alphabet) {
            return (Alphabet<I>) coll;
        }
        return new SimpleAlphabet<>(coll);
    }

}
