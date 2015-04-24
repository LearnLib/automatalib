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

import java.text.MessageFormat;

/**
 * Diagnostic listener for non-fatal errors and warnings during parsing
 * of a TAF file. The parser will usually recover from these errors and produce
 * a valid automaton anyway. Fatal errors are not reported to a diagnostic listener,
 * but instead a {@link TAFParseException} is thrown.
 * 
 * @author Malte Isberner
 */
public interface TAFParseDiagnosticListener {
	
	/**
	 * Called when a non-fatal error is encountered during parsing.
	 * <p>
	 * A non-fatal error could be, for example, the usage of an input symbol that
	 * was not declared in the alphabet. In this case, the respective transition
	 * is simply ignored.
	 * 
	 * @param line the line where the error occurred
	 * @param col the column where the error occurred
	 * @param msgFmt a format string of the message (see {@link MessageFormat})
	 * @param args the arguments of the message
	 */
	public void error(int line, int col, String msgFmt, Object... args);
	
	/**
	 * Called when a warning is raised during parsing.
	 * <p>
	 * A warning could be raised when, for example, an unrecognized option is used
	 * for a state.
	 * 
	 * @param line the line where the warning was raised
	 * @param col the column where the warning was raised
	 * @param msgFmt a format string of the message (see {@link MessageFormat})
	 * @param args the arguments of the message
	 */
	public void warning(int line, int col, String msgFmt, Object... args);
}
