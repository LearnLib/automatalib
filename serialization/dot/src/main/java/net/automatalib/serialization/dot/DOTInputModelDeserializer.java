/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
package net.automatalib.serialization.dot;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.automatalib.common.util.IOUtil;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.ts.simple.SimpleTS;

/**
 * A specialization of the {@link InputModelDeserializer} that returns a {@link DOTInputModelData} object which holds
 * additional information about the state labels of the de-serialized automaton.
 *
 * @param <S>
 *         the state type of the de-serialized automaton
 * @param <I>
 *         the type of input symbols
 * @param <M>
 *         the type of objects implementing classes can deserialize
 */
public interface DOTInputModelDeserializer<S, I, M extends SimpleTS<S, I>> extends InputModelDeserializer<I, M> {

    @Override
    DOTInputModelData<S, I, M> readModel(InputStream is) throws IOException, FormatException;

    @Override
    default DOTInputModelData<S, I, M> readModel(URL url) throws IOException, FormatException {
        try (InputStream is = IOUtil.asBufferedInputStream(url.openStream())) {
            return readModel(is);
        }
    }

    @Override
    default DOTInputModelData<S, I, M> readModel(File f) throws IOException, FormatException {
        try (InputStream is = IOUtil.asBufferedInputStream(f)) {
            return readModel(is);
        }
    }

    @Override
    default DOTInputModelData<S, I, M> readModel(byte[] buf) throws IOException, FormatException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(buf)) {
            return readModel(is);
        }
    }
}
