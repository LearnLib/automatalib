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
package net.automatalib.serialization;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import net.automatalib.common.util.IOUtil;
import net.automatalib.exception.FormatException;

/**
 * A generic interface for formalizing an arbitrary deserializer for a given model type.
 *
 * @param <M>
 *         the type of objects implementing classes can deserialize
 */
public interface ModelDeserializer<M> {

    /**
     * Reads the contents from the given input stream and de-serializes it into a model instance. If the format is a
     * textual one, the input stream is typically interpreted in {@link StandardCharsets#UTF_8 UTF-8}.
     * <p>
     * Note: the input stream will <b>not</b> be closed.
     *
     * @param is
     *         the input stream to read data from
     *
     * @return the de-serialized model
     *
     * @throws IOException
     *         if an error occurred while reading from the stream
     * @throws FormatException
     *         if the content of the stream was not in the expected format
     */
    M readModel(InputStream is) throws IOException, FormatException;

    /**
     * Reads the contents from the given URL and de-serializes it into a model instance. If the format is a textual one,
     * the input stream is typically interpreted in {@link StandardCharsets#UTF_8 UTF-8}.
     *
     * @param url
     *         the url to read data from
     *
     * @return the de-serialized model
     *
     * @throws IOException
     *         if an error occurred while reading from the stream
     * @throws FormatException
     *         if the content of the stream was not in the expected format
     */
    default M readModel(URL url) throws IOException, FormatException {
        try (InputStream is = IOUtil.asBufferedInputStream(url.openStream())) {
            return readModel(is);
        }
    }

    /**
     * Reads the contents from the given file and de-serializes it into a model instance. If the format is a textual
     * one, the input stream is typically interpreted in {@link StandardCharsets#UTF_8 UTF-8}.
     *
     * @param f
     *         the file to read data from
     *
     * @return the de-serialized model
     *
     * @throws IOException
     *         if an error occurred while reading from the stream
     * @throws FormatException
     *         if the content of the stream was not in the expected format
     */
    default M readModel(File f) throws IOException, FormatException {
        try (InputStream is = IOUtil.asBufferedInputStream(f)) {
            return readModel(is);
        }
    }

    /**
     * Reads the contents from the given byte buffer and de-serializes it into a model instance. If the format is a
     * textual one, the input stream is typically interpreted in {@link StandardCharsets#UTF_8 UTF-8}.
     *
     * @param buf
     *         the buffer to read data from
     *
     * @return the de-serialized model
     *
     * @throws IOException
     *         if an error occurred while reading from the stream
     * @throws FormatException
     *         if the content of the stream was not in the expected format
     */
    default M readModel(byte[] buf) throws IOException, FormatException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(buf)) {
            return readModel(is);
        }
    }

}
