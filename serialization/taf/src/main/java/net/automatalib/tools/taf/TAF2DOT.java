package net.automatalib.tools.taf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import net.automatalib.automata.FiniteAlphabetAutomaton;
import net.automatalib.serialization.taf.parser.PrintStreamDiagnosticListener;
import net.automatalib.serialization.taf.parser.TAFParser;
import net.automatalib.util.graphs.dot.GraphDOT;

/**
 * {@code taf2dot} - a tool for converting TAF files to GraphVIZ DOT.
 * 
 * @author Malte Isberner
 */
public class TAF2DOT {
	
	public int run(String[] args) throws Exception {
		if (args.length > 2) {
			System.err.println("Error: taf2dot needs at most two arguments");
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
			FiniteAlphabetAutomaton<?, ?, ?> automaton = TAFParser.parseAny(in, PrintStreamDiagnosticListener.getStderrDiagnosticListener());
			GraphDOT.write(automaton, new OutputStreamWriter(out));
			
			return 0;
		}
		finally {
			if (in != System.in) {
				in.close();
			}
			if (out != System.out) {
				out.close();
			}
		}
		
	}
	
	public static void main(String[] args) {
		TAF2DOT app = new TAF2DOT();
		int retVal = 255;
		try {
			app.run(args);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		System.exit(retVal);
	}

}
