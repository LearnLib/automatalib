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
package net.automatalib.common.util;

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
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

import net.automatalib.common.util.io.NonClosingInputStream;
import net.automatalib.common.util.io.NonClosingOutputStream;

/**
 * Utility methods for operating with {@code java.io.*} classes.
 */
public final class IOUtil {

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    private IOUtil() {
        // prevent instantiation
    }

    /**
     * Ensures that the returned stream is an uncompressed version of the supplied input stream.
     * <p>
     * This method first tries to read the first two bytes from the stream, then resets the stream. If the first two
     * bytes equal the GZip magic number (see {@link GZIPInputStream#GZIP_MAGIC}), the supplied stream is wrapped in a
     * {@link GZIPInputStream}. Otherwise, the stream is returned as-is.
     * <p>
     * Note: this requires the input stream to {@link InputStream#markSupported() support marking}.
     *
     * @param is
     *         the input stream
     *
     * @return an uncompressed version of {@code is}
     *
     * @throws IOException
     *         if reading the magic number fails
     * @throws IllegalArgumentException
     *         if the stream does not support {@link InputStream#markSupported() marking}
     */
    public static InputStream asUncompressedInputStream(InputStream is) throws IOException {
        if (!is.markSupported()) {
            throw new IllegalArgumentException("input stream must support marking");
        }

        is.mark(2);
        byte[] buf = new byte[2];
        int bytesRead;
        try {
            bytesRead = is.read(buf);
        } finally {
            is.reset();
        }
        if (bytesRead == 2) {
            final int byteMask = 0xff;
            final int byteWidth = 8;
            int magic = (buf[1] & byteMask) << byteWidth | (buf[0] & byteMask);
            if (magic == GZIPInputStream.GZIP_MAGIC) {
                return new GZIPInputStream(is);
            }
        }
        return is;
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
        if (isBufferedInputStream(is)) {
            return is;
        }
        return new BufferedInputStream(is);
    }

    /**
     * Returns an input stream that reads the contents of the given file. Additionally, buffers the input stream to
     * improve performance.
     *
     * @param file
     *         the file to read
     *
     * @return a buffered input stream for the file contents
     *
     * @throws IOException
     *         if accessing the file results in an I/O error
     */
    public static InputStream asBufferedInputStream(File file) throws IOException {
        return asBufferedInputStream(Files.newInputStream(file.toPath()));
    }

    /**
     * Ensures that the returned stream is a buffered version of the supplied output stream. The result must not
     * necessarily be an instance of {@link BufferedOutputStream}, it can also be, e.g., a
     * {@link ByteArrayOutputStream}, depending on the type of the supplied output stream.
     *
     * @param os
     *         the output stream
     *
     * @return a buffered version of {@code os}
     */
    public static OutputStream asBufferedOutputStream(OutputStream os) {
        if (isBufferedOutputStream(os)) {
            return os;
        }
        return new BufferedOutputStream(os);
    }

    /**
     * Returns an output stream that writes the contents to the given file. Additionally, buffers the input stream to
     * improve performance.
     *
     * @param file
     *         the file to write to
     *
     * @return a buffered output stream for the file contents
     *
     * @throws IOException
     *         if accessing the file results in an I/O error
     */
    public static OutputStream asBufferedOutputStream(File file) throws IOException {
        return asBufferedOutputStream(Files.newOutputStream(file.toPath()));
    }

    /**
     * Returns a reader that parses the contents of the given file with {@link StandardCharsets#UTF_8} encoding.
     * Additionally, buffers the input stream to improve performance.
     *
     * @param file
     *         the file to read
     *
     * @return a buffered, UTF-8-decoding reader for the file contents
     *
     * @throws IOException
     *         if accessing the file results in an I/O error
     */
    public static Reader asBufferedUTF8Reader(File file) throws IOException {
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
     * @return a buffered, UTF-8-decoding reader for the input stream.
     */
    public static Reader asBufferedUTF8Reader(InputStream is) {
        return asUTF8Reader(asBufferedInputStream(is));
    }

    /**
     * Returns a reader that parses the contents of the given input stream with {@link StandardCharsets#UTF_8} encoding
     * and does not propagate calls to {@link InputStream#close()}.
     *
     * @param is
     *         the input stream to read
     *
     * @return a non-closing, UTF-8-decoding reader for the input stream.
     */
    public static Reader asNonClosingUTF8Reader(InputStream is) {
        return asUTF8Reader(new NonClosingInputStream(is));
    }

    /**
     * Returns a reader that parses the contents of the given input stream with {@link StandardCharsets#UTF_8} encoding.
     *
     * @param is
     *         the input stream to read
     *
     * @return a UTF-8-decoding reader for the input stream
     */
    public static Reader asUTF8Reader(InputStream is) {
        return new InputStreamReader(is, StandardCharsets.UTF_8);
    }

    /**
     * Returns a writer that writes contents to the given file with {@link StandardCharsets#UTF_8} encoding.
     * Additionally, buffers the input stream to improve performance.
     *
     * @param file
     *         the file to write to
     *
     * @return a buffered, UTF-8-encoding writer for the file contents
     *
     * @throws IOException
     *         if writing to the file results in I/O errors
     */
    public static Writer asBufferedUTF8Writer(File file) throws IOException {
        return Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Returns a writer that writes contents to the given output stream with {@link StandardCharsets#UTF_8} encoding. If
     * the given output stream is not already a buffering output stream, additionally buffers the output stream to
     * improve performance.
     * <p>
     * Implementation note: the output stream (byte-wise representation) will be buffered, not the writer (character-
     * wise representation).
     *
     * @param os
     *         the output stream to write to
     *
     * @return a buffered, UTF-8 encoding writer for the output stream
     */
    public static Writer asBufferedUTF8Writer(OutputStream os) {
        return asUTF8Writer(asBufferedOutputStream(os));
    }

    /**
     * Returns a writer that writes contents to the given output stream with {@link StandardCharsets#UTF_8} encoding and
     * propagates calls to {@link OutputStream#close()} to {@link OutputStream#flush()}.
     *
     * @param os
     *         the output stream to write to
     *
     * @return a non-closing, UTF-8 encoding writer for the output stream
     */
    public static Writer asNonClosingUTF8Writer(OutputStream os) {
        return asUTF8Writer(new NonClosingOutputStream(os));
    }

    /**
     * Returns a writer that writes contents to the given output stream with {@link StandardCharsets#UTF_8} encoding.
     *
     * @param os
     *         the output stream to write to
     *
     * @return a UTF-8-encoding writer for the output stream.
     */
    public static Writer asUTF8Writer(OutputStream os) {
        return new OutputStreamWriter(os, StandardCharsets.UTF_8);
    }

    /**
     * Returns a buffered input stream that de-compresses the contents of {@code is} (in case the given input stream
     * contains gzip'ed content).
     *
     * @param is
     *         the input stream to read
     *
     * @return a (potentially) de-compressed, buffered version of {@code is}
     *
     * @throws IOException
     *         if reading the stream (for detecting whether it contains compressed contents) fails
     * @see #asBufferedInputStream(InputStream)
     * @see #asUncompressedInputStream(InputStream)
     */
    public static InputStream asUncompressedBufferedInputStream(InputStream is) throws IOException {
        if (isBufferedInputStream(is)) {
            return asUncompressedInputStream(is);
        }

        return asUncompressedInputStream(asBufferedInputStream(is));
    }

    private static boolean isBufferedInputStream(InputStream is) {
        return is instanceof BufferedInputStream || is instanceof ByteArrayInputStream;
    }

    private static boolean isBufferedOutputStream(OutputStream os) {
        return os instanceof BufferedOutputStream || os instanceof ByteArrayOutputStream;
    }

    /**
     * Copies the contents of the given reader into the given writer.
     *
     * @param in
     *         the reader to read data from
     * @param out
     *         the writer to write data to
     *
     * @throws IOException
     *         if reading from or writing to the respective reader / writer throws this exception
     */
    public static void copy(Reader in, Writer out) throws IOException {
        final char[] buf = new char[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = in.read(buf)) >= 0) {
            out.write(buf, 0, read);
        }
    }

    /**
     * Reads the data from the given reader and returns its contents as a string.
     *
     * @param r
     *         the reader to read data from
     *
     * @return the contents of the reader as a string
     *
     * @throws IOException
     *         if reading from the reader throws this exception
     */
    public static String toString(Reader r) throws IOException {
        final StringWriter w = new StringWriter();
        copy(r, w);
        return w.toString();
    }
}
