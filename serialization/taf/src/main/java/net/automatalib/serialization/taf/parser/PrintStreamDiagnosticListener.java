/* Copyright (C) 2015 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.serialization.taf.parser;

import java.io.PrintStream;
import java.text.MessageFormat;

public class PrintStreamDiagnosticListener implements TAFParseDiagnosticListener {
	
	private final PrintStream ps;
	
	private static final PrintStreamDiagnosticListener STDERR_INSTANCE
		= new PrintStreamDiagnosticListener(System.err);
	
	public static TAFParseDiagnosticListener getStderrDiagnosticListener() {
		return STDERR_INSTANCE;
	}

	public PrintStreamDiagnosticListener(PrintStream ps) {
		this.ps = ps;
	}

	@Override
	public void error(int line, int col, String msgFmt, Object... args) {
		ps.printf("Error: at line %d, column %d: %s\n", line, col, MessageFormat.format(msgFmt, args));
		ps.flush();
	}

	@Override
	public void warning(int line, int col, String msgFmt, Object... args) {
		ps.printf("Warning: at line %d, column %d: %s\n", line, col, MessageFormat.format(msgFmt, args));
		ps.flush();
	}

}
