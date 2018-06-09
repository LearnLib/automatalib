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
package net.automatalib.serialization;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.automatalib.commons.util.IOUtil;

/**
 * A generic interface for formalizing an arbitrary deserializer for a given model type.
 *
 * @param <M>
 *         the type of objects implementing classes can deserialize
 *
 * @author frohme
 */
public interface ModelDeserializer<M> {

    M readModel(InputStream is) throws IOException;

    default M readModel(URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            return readModel(is);
        }
    }

    default M readModel(File f) throws IOException {
        try (InputStream is = IOUtil.asBufferedInputStream(f)) {
            return readModel(is);
        }
    }

    default M readModel(byte[] buf) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(buf)) {
            return readModel(is);
        }
    }

}
