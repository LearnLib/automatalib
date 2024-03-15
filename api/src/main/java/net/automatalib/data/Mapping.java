/*
 * Copyright (C) 2014-2015 The LearnLib Contributors
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package net.automatalib.data;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author falk
 * @param <K>
 * @param <V>
 */
public class Mapping<K, V> extends LinkedHashMap<K, V>
        implements Iterable<Map.Entry<K, V>> {

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return this.entrySet().iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Mapping<?, ?> other = (Mapping<?, ?>) obj;
        return other.entrySet().equals(entrySet());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash * this.entrySet().hashCode();
    }

//    @Override
//    public V get(Object key) {
//        V v = super.get(key);
//        if (v == null) {
//            throw new IllegalStateException();
//        }
//        return v;
//    }

    public String toString(String map) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Map.Entry<K,V> e : entrySet()) {
            sb.append(e.getKey()).append(map).append(e.getValue()).append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public Set<K> getAllKeys(V value) {
        Set<K> retKeySet = new HashSet<>();
        for (Map.Entry<K,V> entry : this.entrySet()) {
            //log.trace("key = " + K);
            //log.trace("value = " + entry.getKey().toString());
            if (entry.getValue().equals(value)){
                //log.trace(entry.getKey().toString() + " equals " + value.toString());
                retKeySet.add(entry.getKey());
            }
        }
        return retKeySet;
    }

    @Override
    public String toString() {
        return toString(">");
    }
}
