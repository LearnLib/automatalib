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
package net.automatalib.tools.taf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import net.automatalib.automata.FiniteAlphabetAutomaton;
import net.automatalib.serialization.taf.parser.PrintStreamDiagnosticListener;
import net.automatalib.serialization.taf.parser.TAFParser;
import net.automatalib.util.graphs.dot.GraphDOT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code taf2dot} - a tool for converting TAF files to GraphVIZ DOT.
 *
 * @author Malte Isberner
 */
public class TAF2DOT {

    private static final Logger LOGGER = LoggerFactory.getLogger(TAF2DOT.class);

    public static void main(String[] args) {
        TAF2DOT app = new TAF2DOT();
        try {
            app.run(args);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        final int erronousExitCode = 255;
        System.exit(erronousExitCode);
    }

    public int run(String[] args) throws IOException {
        if (args.length > 2) {
            LOGGER.error("Error: taf2dot needs at most two arguments");
            return 1;
        }
        InputStream in = System.in;
        OutputStream out = System.out;
        try {
            if (args.length > 0) {
                in = new BufferedInputStream(new FileInputStream(new File(args[0])));
            }
            if (args.length > 1) {
                out = new BufferedOutputStream(new FileOutputStream(new File(args[1])));
            }
            FiniteAlphabetAutomaton<?, ?, ?> automaton =
                    TAFParser.parseAny(in, PrintStreamDiagnosticListener.getStderrDiagnosticListener());
            GraphDOT.write(automaton, new OutputStreamWriter(out));

            return 0;
        } finally {
            if (in != System.in) {
                in.close();
            }
            if (out != System.out) {
                out.close();
            }
        }

    }

}
