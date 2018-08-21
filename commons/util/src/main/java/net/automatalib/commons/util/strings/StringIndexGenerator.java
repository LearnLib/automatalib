/* Copyright (C) 2013-2018 TU Dortmund
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

/**
 * Class for transforming integer index values into string values (using latin characters, therefore effectively
 * realizing a radix-26 representation of numbers).
 *
 * @author Malte Isberner
 */
public class StringIndexGenerator {

    private static final int ALPHABET_SIZE = 26;

    private final char base;

    public StringIndexGenerator(Case charCase) {
        if (charCase == Case.LOWER) {
            this.base = 'a';
        } else {
            this.base = 'A';
        }
    }

    public static int getIntegerIndex(String sidx) {
        int idx = 0;
        int value = 1;
        for (int i = 0; i < sidx.length(); i++) {
            char c = sidx.charAt(i);
            idx = idx + value * getInteger(c);
            value *= ALPHABET_SIZE;
        }

        return idx;
    }

    private static int getInteger(char c) {
        return Character.toLowerCase(c) - 'a';
    }

    public void appendStringIndex(Appendable a, int idx) throws IOException {
        int idxIter = idx;
        do {
            a.append(getChar(idxIter % ALPHABET_SIZE));
            idxIter /= ALPHABET_SIZE;
        } while (idxIter > 0);
    }

    public void appendStringIndex(StringBuilder sb, int idx) {
        int idxIter = idx;
        do {
            sb.append(getChar(idxIter % ALPHABET_SIZE));
            idxIter /= ALPHABET_SIZE;
        } while (idxIter > 0);
    }

    private char getChar(int idx) {
        return (char) (base + idx);
    }

    public String getStringIndex(int idx) {
        StringBuilder sb = new StringBuilder();
        appendStringIndex(sb, idx);

        return sb.toString();
    }

    public enum Case {
        LOWER,
        UPPER
    }

}
