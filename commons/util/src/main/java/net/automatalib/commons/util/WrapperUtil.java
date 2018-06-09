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
package net.automatalib.commons.util;

public final class WrapperUtil {

    private WrapperUtil() {
        // prevent instantiation
    }

    public static boolean booleanValue(Boolean b) {
        return booleanValue(b, false);
    }

    public static boolean booleanValue(Boolean b, boolean def) {
        return (b != null) ? b.booleanValue() : def;
    }

    public static short shortValue(Short s, short def) {
        return (s != null) ? s.shortValue() : def;
    }

    public static int intValue(Integer i) {
        return intValue(i, 0);
    }

    public static int intValue(Integer i, int def) {
        return (i != null) ? i.intValue() : def;
    }

    public static long longValue(Long l) {
        return longValue(l, 0L);
    }

    public static long longValue(Long l, long def) {
        return (l != null) ? l.longValue() : def;
    }

    public static float floatValue(Float f) {
        return floatValue(f, 0.0f);
    }

    public static float floatValue(Float f, float def) {
        return (f != null) ? f.floatValue() : def;
    }

    public static double doubleValue(Double d) {
        return doubleValue(d, 0.0);
    }

    public static double doubleValue(Double d, double def) {
        return (d != null) ? d.doubleValue() : def;
    }

}
