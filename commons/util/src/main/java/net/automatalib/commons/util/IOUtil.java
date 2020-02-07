/* Copyright (C) 2013-2020 TU Dortmund
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
package net.automatalib.commons.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

/**
 * Utility methods for operating with {@code java.io.*} classes.
 *
 * @author Malte Isberner
 */
public final class IOUtil {

    // Prevent instantiation
    private IOUtil() {}

    /**
     * Ensures that the returned stream is an uncompressed version of the supplied input stream.
     * <p>
     * This method first tries to read the first two bytes from the stream, then resets the stream. If the first two
     * bytes equal the GZip magic number (see {@link GZIPInputStream#GZIP_MAGIC}), the supplied stream is wrapped in a
     * {@link GZIPInputStream}. Otherwise, the stream is returned as-is, but possibly in a buffered version (see {@link
     * #asBufferedInputStream(InputStream)}).
     *
     * @param is
     *         the input stream
     *
     * @return an uncompressed version of {@code is}
     *
     * @throws IOException
     *         if reading the magic number fails
     */
    public static InputStream asUncompressedInputStream(InputStream is) throws IOException {
        final InputStream bufferedInputStream = asBufferedInputStream(is);
        assert bufferedInputStream.markSupported();

        bufferedInputStream.mark(2);
        byte[] buf = new byte[2];
        int bytesRead;
        try {
            bytesRead = bufferedInputStream.read(buf);
        } finally {
            bufferedInputStream.reset();
        }
        if (bytesRead == 2) {
            final int byteMask = 0xff;
            final int byteWidth = 8;
            int magic = (buf[1] & byteMask) << byteWidth | (buf[0] & byteMask);
            if (magic == GZIPInputStream.GZIP_MAGIC) {
                return new GZIPInputStream(bufferedInputStream);
            }
        }
        return bufferedInputStream;
    }

    /**
     * Ensures that the returned stream is a buffered version of the supplied input stream. The result must not
     * necessarily be an instance of {@link BufferedInputStream}, it can also be, e.g., a {@link ByteArrayInputStream},
     * depending on the type of the supplied input stream.
     *
     * @param is
     *         the input stream
     *
     * @return a buffered version of {@code is}
     */
    public static InputStream asBufferedInputStream(InputStream is) {
        if (is instanceof BufferedInputStream || is instanceof ByteArrayInputStream) {
            return is;
        }
        return new BufferedInputStream(is);
    }

    /**
     * Returns an input stream that reads the contents of the given file. Additionally buffers the input stream to
     * improve performance.
     *
     * @param file
     *         the file to read
     *
     * @return a (buffered) input stream for the file contents.
     *
     * @throws IOException
     *         if accessing the file results in an I/O error.
     */
    public static InputStream asBufferedInputStream(File file) throws IOException {
        return asBufferedInputStream(Files.newInputStream(file.toPath()));
    }

    /**
     * Ensures that the returned stream is a buffered version of the supplied output stream. The result must not
     * necessarily be an instance of {@link BufferedOutputStream}, it can also be, e.g., a {@link ByteArrayOutputStream},
     * depending on the type of the supplied output stream.
     *
     * @param os
     *         the output stream
     *
     * @return a buffered version of {@code os}
     */
    public static OutputStream asBufferedOutputStream(OutputStream os) {
        if (os instanceof BufferedOutputStream || os instanceof ByteArrayOutputStream) {
            return os;
        }
        return new BufferedOutputStream(os);
    }

    /**
     * Returns an output stream that writes the contents to the given file. Additionally buffers the input stream to
     * improve performance.
     *
     * @param file
     *         the file to write to
     *
     * @return a (buffered) output stream for the file contents.
     *
     * @throws IOException
     *         if accessing the file results in an I/O error.
     */
    public static OutputStream asBufferedOutputStream(File file) throws IOException {
        return asBufferedOutputStream(Files.newOutputStream(file.toPath()));
    }

    /**
     * Returns a reader that parses the contents of the given file with {@link StandardCharsets#UTF_8} encoding.
     * Additionally buffers the input stream to improve performance.
     *
     * @param file
     *         the file to read
     *
     * @return a (buffered) reader for the file contents.
     *
     * @throws IOException
     *         if accessing the file results in an I/O error.
     */
    public static Reader asBufferedUTF8Reader(final File file) throws IOException {
        return Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Returns a reader that parses the contents of the given input stream with {@link StandardCharsets#UTF_8} encoding.
     * If the given input stream is not already a buffering input stream, additionally buffers the input stream to
     * improve performance.
     * <p>
     * Implementation note: the input stream (byte-wise representation) will be buffered, not the reader (character-wise
     * representation).
     *
     * @param is
     *         the input stream to read
     *
     * @return a (buffered) reader for the file contents.
     */
    public static Reader asBufferedUTF8Reader(final InputStream is) {
        return asUTF8Reader(asBufferedInputStream(is));
    }

    /**
     * Returns a reader that parses the contents of the given input stream with {@link StandardCharsets#UTF_8} encoding.
     *
     * @param is
     *         the input stream to read
     *
     * @return a reader for the file contents.
     */
    public static Reader asUTF8Reader(final InputStream is) {
        return new InputStreamReader(is, StandardCharsets.UTF_8);
    }

    /**
     * Returns a writer that writes contents to the given file with {@link StandardCharsets#UTF_8} encoding.
     * Additionally buffers the input stream to improve performance.
     *
     * @param file
     *         the file to write to
     *
     * @return a (buffered) writer for the file contents.
     *
     * @throws IOException
     *         if writing to the file results in I/O errors.
     */
    public static Writer asBufferedUTF8Writer(final File file) throws IOException {
        return Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Returns a writer that writes contents to the given output stream with {@link StandardCharsets#UTF_8} encoding.
     * If the given output stream is not already a buffering output stream, additionally buffers the output stream to
     * improve performance.
     * <p>
     * Implementation note: the output stream (byte-wise representation) will be buffered, not the writer (character-
     * wise representation).
     *
     * @param os
     *         the output stream to write to
     *
     * @return a (buffered) writer for the file contents.
     */
    public static Writer asBufferedUTF8Writer(final OutputStream os) {
        return asUTF8Writer(asBufferedOutputStream(os));
    }

    /**
     * Returns a writer that writes contents to the given output stream with {@link StandardCharsets#UTF_8} encoding.
     *
     * @param os
     *         the output stream to write to
     *
     * @return a (buffered) writer for the file contents.
     */
    public static Writer asUTF8Writer(final OutputStream os) {
        return new OutputStreamWriter(os, StandardCharsets.UTF_8);
    }
}
