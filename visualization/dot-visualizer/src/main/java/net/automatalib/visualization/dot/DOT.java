/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.visualization.dot;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import com.google.common.io.CharStreams;
import net.automatalib.AutomataLibProperty;
import net.automatalib.AutomataLibSettings;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.process.ProcessUtil;
import net.automatalib.visualization.dot.DOTMultiDialog.ThrowableExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to simplify operating the GraphVIZ "dot" utility. Please note that all the provided methods require
 * GraphVIZ to be installed on the system, and that the "dot" binary resides in the execution path.
 *
 * @author Malte Isberner
 * @author frohme
 */
public final class DOT {

    private static final Logger LOGGER = LoggerFactory.getLogger(DOT.class);

    private static String dotExe;

    static {
        final AutomataLibSettings settings = AutomataLibSettings.getInstance();

        final String dotExePath = settings.getProperty(AutomataLibProperty.DOT_EXE_DIR);
        final String dotExeName = settings.getProperty(AutomataLibProperty.DOT_EXE_NAME, "dot");

        String dotExe = dotExeName;
        if (dotExePath != null) {
            Path dotBasePath = FileSystems.getDefault().getPath(dotExePath);
            Path resolvedDotPath = dotBasePath.resolve(dotExeName);
            dotExe = resolvedDotPath.toString();
        }

        DOT.dotExe = dotExe;
    }

    private DOT() {}

    /**
     * Explicitly sets the path to the DOT utility executable.
     *
     * @param dotExe
     *         the path to the DOT utility executable
     */
    public static void setDotExe(String dotExe) {
        DOT.dotExe = dotExe;
    }

    /**
     * Checks whether the DOT utility can be successfully invoked.
     */
    public static boolean checkUsable() {
        try {
            final String[] dotCheck = buildRawDOTCommand("-V");
            return ProcessUtil.invokeProcess(dotCheck) == 0;
        } catch (IOException | InterruptedException ex) {
            LOGGER.error("Error executing dot", ex);
        }
        return false;
    }

    /**
     * Invokes the DOT utility on a file. Convenience method, see {@link #runDOT(Reader, String, String...)}.
     *
     * @throws IOException
     *         if reading from the file or the call to the DOT utility fails.
     */
    public static InputStream runDOT(File dotFile, String format, String... additionalOpts) throws IOException {
        return runDOT(IOUtil.asBufferedUTF8Reader(dotFile), format, additionalOpts);
    }

    /**
     * Invokes the GraphVIZ DOT utility for rendering graphs.
     *
     * @param r
     *         the reader from which the GraphVIZ description is obtained.
     * @param format
     *         the output format, as understood by the dot utility, e.g., png, ps, ...
     *
     * @return an input stream from which the image data can be read.
     *
     * @throws IOException
     *         if reading from the reader or the call to the DOT utility fails.
     */
    public static InputStream runDOT(Reader r, String format, String... additionalOpts) throws IOException {
        final String[] dotCommand = buildDOTCommand(format, additionalOpts);

        final Process p = ProcessUtil.buildProcess(dotCommand, r, null, LOGGER::warn);

        return p.getInputStream();
    }

    /**
     * Invokes the DOT utility on a string. Convenience method, see {@link #runDOT(Reader, String, String...)}
     *
     * @throws IOException
     *         if the call to the DOT utility fails.
     */
    public static InputStream runDOT(String dotText, String format, String... additionalOpts) throws IOException {
        try (StringReader sr = new StringReader(dotText)) {
            return runDOT(sr, format, additionalOpts);
        }
    }

    /**
     * Invokes the DOT utility on a file, producing an output file. Convenience method, see
     * {@link #runDOT(Reader, String, File)}.
     *
     * @throws IOException
     *         if reading from the file, the call to the DOT utility, or writing to the file fails.
     */
    public static void runDOT(File dotFile, String format, File out) throws IOException {
        runDOT(IOUtil.asBufferedUTF8Reader(dotFile), format, out);
    }

    /**
     * Invokes the GraphVIZ DOT utility for rendering graphs, writing output to the specified file.
     *
     * @param r
     *         the reader from which the GraphVIZ description is read.
     * @param format
     *         the output format to produce.
     * @param out
     *         the file to which the output is written.
     *
     * @throws IOException
     *         if reading from the reader, the call to the DOT utility, or writing to the file fails.
     */
    public static void runDOT(Reader r, String format, File out) throws IOException {
        final String[] dotCommand = buildDOTCommand(format, "-o" + out.getAbsolutePath());

        try {
            ProcessUtil.invokeProcess(dotCommand, r, LOGGER::warn);
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for 'dot' process to exit.", ex);
        }
    }

    /**
     * Invokes the DOT utility on a string, producing an output file. Convenience method, see
     * {@link #runDOT(Reader, String, File)}.
     *
     * @throws IOException
     *         if the call to the DOT utility or writing to the file fails.
     */
    public static void runDOT(String dotText, String format, File out) throws IOException {
        runDOT(new StringReader(dotText), format, out);
    }

    /**
     * Renders a GraphVIZ description from a file, using an external program for displaying. Convenience method, see
     * {@link #renderDOTExternal(Reader, String)}.
     *
     * @throws IOException
     *         if reading from the file or the call to the DOT utility fails.
     */
    public static void renderDOTExternal(File dotFile, String format) throws IOException {
        renderDOTExternal(IOUtil.asBufferedUTF8Reader(dotFile), format);
    }

    /**
     * Renders a GraphVIZ description, using an external program for displaying. The program is determined by the
     * system's file type associations, using the {@link Desktop#open(File)} method.
     *
     * @param r
     *         the reader from which the GraphVIZ description is read.
     * @param format
     *         the output format, as understood by the dot utility, e.g., png, ps, ...
     *
     * @throws IOException
     *         if reading from the reader or the call to the DOT utility fails.
     */
    public static void renderDOTExternal(Reader r, String format) throws IOException {
        final File image = File.createTempFile("dot", format);
        runDOT(r, format, image);
        Desktop.getDesktop().open(image);
    }

    /**
     * Renders a GraphVIZ description from a string, using an external program for displaying. Convenience method, see
     * {@link #renderDOTExternal(Reader, String)}.
     *
     * @throws IOException
     *         if the call to the DOT utility fails.
     */
    public static void renderDOTExternal(String dotText, String format) throws IOException {
        renderDOTExternal(new StringReader(dotText), format);
    }

    /**
     * Renders a GraphVIZ description from a {@link File} and displays it in a Swing window. Convenience method, see
     * {@link #renderDOT(Reader, boolean)}.
     *
     * @throws IOException
     *         if reading from the file or the call to the DOT utility fails.
     */
    public static void renderDOT(File dotFile, boolean modal) throws IOException {
        renderDOT(IOUtil.asBufferedUTF8Reader(dotFile), modal);
    }

    /**
     * Renders a GraphVIZ description from a {@link Reader} and displays it in a Swing window. Convenience method, see
     * {@link #renderDOT(String, boolean)}.
     *
     * @throws IOException
     *         if reading from the reader or the call to the DOT utility fails.
     */
    public static void renderDOT(Reader r, boolean modal) throws IOException {
        renderDOT(CharStreams.toString(r), modal);
    }

    /**
     * Renders a GraphVIZ description and displays it in a Swing window.
     *
     * @param dotText
     *         the {@link String} from which the description is obtained.
     * @param modal
     *         whether the dialog should be modal.
     *
     * @throws IOException
     *         if the call to the DOT utility fails.
     */
    public static void renderDOT(String dotText, boolean modal) throws IOException {
        new DOTDialog(dotText, modal);
    }

    /**
     * Renders multiple (named) GraphVIZ descriptions from {@link File}s and displays them in a Swing window.
     * Convenience method, see {@link #renderDOTStrings(List, boolean)}.
     *
     * @throws IOException
     *         if reading from the files or the calls to the DOT utility fail.
     */
    public static void renderDOTFiles(List<Pair<String, File>> files, boolean modal) throws IOException {
        renderDOTInternal(files, modal, f -> CharStreams.toString(IOUtil.asBufferedUTF8Reader(f)));
    }

    /**
     * Renders multiple (named) GraphVIZ descriptions from {@link Reader}s and displays them in a Swing window.
     * Convenience method, see {@link #renderDOTStrings(List, boolean)}.
     *
     * @throws IOException
     *         if reading from the readers or the calls to the DOT utility fail.
     */
    public static void renderDOTReaders(List<Pair<String, Reader>> readers, boolean modal) throws IOException {
        renderDOTInternal(readers, modal, CharStreams::toString);
    }

    /**
     * Renders multiple (named) GraphVIZ descriptions and displays them in a Swing window.
     *
     * @param dotTexts
     *         the {@link String}s from which the description is obtained. The first element of the {@link Pair} should
     *         contain the name, the second element should contain the DOT code.
     * @param modal
     *         whether the dialog should be modal.
     *
     * @throws IOException
     *         if the calls to the DOT utility fail.
     */
    public static void renderDOTStrings(List<Pair<String, String>> dotTexts, boolean modal) throws IOException {
        renderDOTInternal(dotTexts, modal, s -> s);
    }

    private static <I> void renderDOTInternal(List<Pair<String, I>> dots,
                                              boolean modal,
                                              ThrowableExtractor<I, String> extractor) throws IOException {
        new DOTMultiDialog<>(dots, modal, extractor);
    }

    /**
     * Reads a DOT description from a file and returns the PNG rendering result as a {@link BufferedImage}.
     *
     * @param dotFile
     *         the file containing the DOT description
     *
     * @return the rendering result
     *
     * @throws IOException
     *         if reading from the file or the call to the DOT utility fails.
     */
    public static BufferedImage renderDOTImage(File dotFile) throws IOException {
        return renderDOTImage(IOUtil.asBufferedUTF8Reader(dotFile));
    }

    /**
     * Reads a DOT description from a reader and returns the PNG rendering result as a {@link BufferedImage}.
     *
     * @param dotReader
     *         the reader from which to read the description
     *
     * @return the rendering result
     *
     * @throws IOException
     *         if reading from the reader or the call to the DOT utility fails.
     */
    public static BufferedImage renderDOTImage(Reader dotReader) throws IOException {
        try (InputStream pngIs = runDOT(dotReader, "png")) {
            return ImageIO.read(pngIs);
        }
    }

    /**
     * Reads a DOT description from a string and returns the PNG rendering result as a {@link BufferedImage}.
     *
     * @param dotText
     *         the DOT description
     *
     * @return the rendering result
     *
     * @throws IOException
     *         if the call to the DOT utility fails.
     */
    public static BufferedImage renderDOTImage(String dotText) throws IOException {
        return renderDOTImage(new StringReader(dotText));
    }

    private static String[] buildRawDOTCommand(String... opts) {
        String[] dotArgs = new String[1 + opts.length];
        dotArgs[0] = dotExe;
        System.arraycopy(opts, 0, dotArgs, 1, opts.length);

        return dotArgs;
    }

    private static String[] buildDOTCommand(String format, String... additionalOpts) {
        String[] dotArgs = new String[1 + additionalOpts.length];
        dotArgs[0] = "-T" + format;
        System.arraycopy(additionalOpts, 0, dotArgs, 1, additionalOpts.length);

        return buildRawDOTCommand(dotArgs);
    }

}
