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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author falk
 * @param <K>
 * @param <V>
 */
public class Valuation<K, V extends DataValue<?>> extends Mapping<K, V> {

    /**
     * returns the contained values of some type.
     *
     * @param <T>
     * @param type the type
     * @return
     */
    public <T> Collection<DataValue<T>> values(DataType<T> type) {
        List<DataValue<T>> list = new ArrayList<>();
        for (DataValue<?> v : values()) {
            if (v.type.equals(type)) {
                list.add((DataValue<T>) v);
            }
        }
        return list;
    }

}
