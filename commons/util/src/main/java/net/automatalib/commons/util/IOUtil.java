/* Copyright (C) 2013 TU Dortmund
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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Utility methods for operating with <tt>java.io.*</tt> classes.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public abstract class IOUtil {
	
	
	/**
	 * Skips the content of the stream as long as there is data available.
	 * Afterwards, the stream is closed.
	 * @param is the input stream.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void skip(InputStream is) throws IOException {
		while(is.available() > 0) 
			is.skip(Long.MAX_VALUE);
		is.close();
	}
	
	/**
	 * Copies all data from the given input stream to the given output stream.
	 * @param is the input stream.
	 * @param os the output stream.
	 * @param close <code>true</code> if both streams are closed afterwards,
	 * <code>false</code> otherwise.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void copy(InputStream is, OutputStream os, boolean close) throws IOException {
		byte[] buf = new byte[8192];
		int len;
		try {
			while((len = is.read(buf)) != -1)
				os.write(buf, 0, len);
		}
		finally {
			if(close) {
				try { is.close(); } catch(IOException e) {}
				try { os.close(); } catch(IOException e) {}
			}
		}
	}
	
	/**
	 * Copies all data from the given input stream to the given output stream
	 * and closes the streams.
	 * Convenience method, same as <code>copy(is, os, true)</code>.
	 * @param is the input stream.
	 * @param os the output stream.
	 * @throws IOException if an I/O error occurs.
	 * @see #copy(InputStream, OutputStream, boolean)
	 */
	public static void copy(InputStream is, OutputStream os) throws IOException {
		copy(is, os, true);
	}
	
	/**
	 * Copies all text from the given reader to the given writer.
	 * @param r the reader.
	 * @param w the writer.
	 * @param close <code>true</code> if both reader and writer are closed
	 * afterwards, <code>false</code> otherwise.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void copy(Reader r, Writer w, boolean close) throws IOException {
		char[] buf = new char[8192];
		int len;
		try {
			while((len = r.read(buf)) != -1)
				w.write(buf, 0, len);
		}
		finally {
			if(close) {
				try { r.close(); } catch(IOException e) {}
				try { w.close(); } catch(IOException e) {}
			}
		}
	}
	
	/**
	 * Copies all text from the given reader to the given writer and closes
	 * both afterwards.
	 * Convenience method, same as <code>copy(r, w, true)</code>.
	 * @param r the reader.
	 * @param w the writer.
	 * @throws IOException if an I/O error occurs.
	 * @see #copy(Reader, Writer, boolean)
	 */
	public static void copy(Reader r, Writer w) throws IOException {
		copy(r, w, true);
	}
}
