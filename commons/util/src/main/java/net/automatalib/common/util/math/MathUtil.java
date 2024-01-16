/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.common.util.math;

/**
 * Utility class for mathematical computations.
 */
public final class MathUtil {

    private MathUtil() {
        // prevent instantiation
    }

    /**
     * Computes the binomial coefficient n over k using Pascal's triangle. If the binomial coefficient exceeds the
     * maximum number representable by a long, this methods returns {@link Long#MAX_VALUE} instead.
     *
     * @param n
     *         the value for n
     * @param k
     *         the value for k
     *
     * @return the binomial coefficient n over k
     */
    public static long binomial(int n, int k) {

        if (k < 0 || n < k) {
            throw new IllegalArgumentException("Illegal values for n and k");
        }

        // abuse symmetry
        final int effectiveK = Math.min(k, n - k);

        // pascal's triangle using a one dimensional storage
        final int dimN = n + 1;
        final int dimK = effectiveK + 1;
        final long[] tmp = new long[dimN * dimK];

        try {
            for (int i = 0; i <= n; i++) {
                final int min = Math.min(i, effectiveK);
                for (int j = 0; j <= min; j++) {
                    if (j == 0 || j == i) {
                        // tmp[i][j] = 1
                        tmp[i * dimK + j] = 1;
                    } else {
                        // tmp[i][j] = tmp[i-1][j-1] + tmp[i-1][k]
                        tmp[i * dimK + j] = Math.addExact(tmp[(i - 1) * dimK + j - 1], tmp[(i - 1) * dimK + j]);
                    }
                }
            }

            // tmp[n][k]
            return tmp[n * dimK + effectiveK];
        } catch (ArithmeticException ae) {
            return Long.MAX_VALUE;
        }
    }

}
