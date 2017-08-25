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
package net.automatalib.commons.util.strings;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Misceallanous utility functions for {@link String}s.
 *
 * @author Malte Isberner
 */
public final class StringUtil {

    private static final String IDENTIFIER_REGEX = "[a-zA-Z_]\\w*";

    private static Pattern identifierPattern;

    private StringUtil() {
    }

    public static String enquote(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2);
        try {
            enquote(s, sb);
        } catch (IOException e) {
        }
        return sb.toString();
    }

    public static void enquote(String s, Appendable a) throws IOException {
        a.append('"');
        escapeQuotes(s, a);
        a.append('"');
    }

    public static Pattern getIdentifierPattern() {
        if (identifierPattern == null) {
            identifierPattern = Pattern.compile(IDENTIFIER_REGEX);
        }
        return identifierPattern;
    }

    public static String enquoteIfNecessary(String s) {
        StringBuilder sb = new StringBuilder();
        try {
            enquoteIfNecessary(s, sb);
            return sb.toString();
        } catch (IOException ex) {
            throw new AssertionError();
        }
    }

    public static void enquoteIfNecessary(String s, Appendable a) throws IOException {
        if (!getIdentifierPattern().matcher(s).matches()) {
            enquote(s, a);
        } else {
            a.append(s);
        }
    }

    public static String enquoteIfNecessary(String s, Pattern p) {
        StringBuilder sb = new StringBuilder();
        try {
            enquoteIfNecessary(s, sb, p);
            return sb.toString();
        } catch (IOException ex) {
            throw new AssertionError();
        }
    }

    public static void enquoteIfNecessary(String s, Appendable a, Pattern valid) throws IOException {
        if (!valid.matcher(s).matches()) {
            enquote(s, a);
        } else {
            a.append(s);
        }
    }

    public static void enquoteIfNecessary(String s, Appendable a, Pattern valid, Pattern exception) throws IOException {
        if (!valid.matcher(s).matches() || exception.matcher(s).matches()) {
            enquote(s, a);
        } else {
            a.append(s);
        }
    }

    public static String unquote(String s) {
        StringBuilder sb = new StringBuilder(s.length() - 2);
        try {
            unquote(s, sb);
        } catch (IOException e) {
        }
        return sb.toString();
    }

    public static void unquote(String s, Appendable a) throws IOException {
        if (s.charAt(0) != '"' || s.charAt(s.length() - 1) != '"') {
            throw new IllegalArgumentException(
                    "Argument to StringUtil.unquote() must begin and end with a double quote ('\"').");
        }
        unescapeQuotes(s.substring(1, s.length() - 1));
    }

    /**
     * Unescapes escaped double quotes in a string, i.e. replaces <code>\"</code> by <code>"</code> and <code>\\</code>
     * by <code>\</code>.
     *
     * @param s
     *         the string in which to unescape double quotes.
     *
     * @return the unescaped string.
     */
    public static String unescapeQuotes(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        try {
            unescapeQuotes(s, sb);
        } catch (IOException e) {
        }
        return sb.toString();
    }

    public static void unescapeQuotes(String s, Appendable a) throws IOException {
        int eos = s.length() - 1;
        for (int i = 0; i < eos; i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                c = s.charAt(++i);
                if (c != '"' && c != '\\') {
                    a.append('\\');
                }
            }
            a.append(c);
        }

        a.append(s.charAt(eos));
    }

    /**
     * Escapes double quotes in a string. Effectively, <code>"</code> is replaced by <code>\"</code> and <code>\</code>
     * is replaced by <code>\\</code>.
     *
     * @param s
     *         the string to escape.
     *
     * @return the escaped string.
     */
    public static String escapeQuotes(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        try {
            escapeQuotes(s, sb);
        } catch (IOException e) {
        }
        return sb.toString();
    }

    public static void escapeQuotes(String s, Appendable a) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '\\' || c == '"') {
                a.append('\\');
            }
            a.append(c);
        }
    }

    public static void appendArray(Appendable a, Object[] array, String sepString) throws IOException {
        boolean first = true;

        for (Object o : array) {
            if (first) {
                first = false;
            } else {
                a.append(sepString);
            }
            appendObject(a, o);
        }
    }

    public static void appendObject(Appendable a, Object obj) throws IOException {
        if (obj instanceof Printable) {
            ((Printable) obj).print(a);
        } else {
            a.append(String.valueOf(obj));
        }
    }

    public static void appendArrayEnquoted(Appendable a, Object[] array, String sepString) throws IOException {
        boolean first = true;

        for (Object o : array) {
            if (first) {
                first = false;
            } else {
                a.append(sepString);
            }
            enquote(String.valueOf(o), a);
        }
    }

    public static void appendIterable(Appendable a, Iterable<?> it, String sepString) throws IOException {
        boolean first = true;

        for (Object o : it) {
            if (first) {
                first = false;
            } else {
                a.append(sepString);
            }
            appendObject(a, o);
        }
    }

    public static void appendIterableEnquoted(Appendable a, Iterable<?> it, String sepString) throws IOException {
        boolean first = true;

        for (Object o : it) {
            if (first) {
                first = false;
            } else {
                a.append(sepString);
            }
            if (o == null) {
                a.append("null");
            } else {
                enquote(o.toString(), a);
            }
        }
    }
}
