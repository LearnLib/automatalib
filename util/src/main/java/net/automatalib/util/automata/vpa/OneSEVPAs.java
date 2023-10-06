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
package net.automatalib.util.automata.vpa;

import net.automatalib.automata.vpa.DefaultOneSEVPA;
import net.automatalib.automata.vpa.OneSEVPA;
import net.automatalib.commons.util.Pair;
import net.automatalib.util.minimizer.OneSEVPAMinimizer;
import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.words.VPAlphabet;

/**
 * Operations on {@link OneSEVPA}s.
 */
public final class OneSEVPAs {

    private OneSEVPAs() {
        // prevent instantiation
    }

    /**
     * Returns a view on the conjunction ("and") of two {@link OneSEVPA}s.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     *
     * @return a view representing the conjunction of the specified {@link OneSEVPA}s
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> and(OneSEVPA<L1, I> sevpa1,
                                                            OneSEVPA<L2, I> sevpa2,
                                                            VPAlphabet<I> alphabet) {
        return combine(sevpa1, sevpa2, alphabet, AcceptanceCombiner.AND);
    }

    /**
     * Most general way of combining two {@link OneSEVPA}s. The {@link AcceptanceCombiner} specified via the
     * {@code combiner} parameter specifies how acceptance values of the {@link OneSEVPA}s will be combined to an
     * acceptance value in the resulting {@link ProductOneSEVPA}.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     * @param combiner
     *         combination method for acceptance values
     *
     * @return a view representing the given combination of the specified {@link OneSEVPA}s
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> combine(OneSEVPA<L1, I> sevpa1,
                                                                OneSEVPA<L2, I> sevpa2,
                                                                VPAlphabet<I> alphabet,
                                                                AcceptanceCombiner combiner) {
        return new ProductOneSEVPA<>(alphabet, sevpa1, sevpa2, combiner);
    }

    /**
     * Returns a view on the disjunction ("or") of two {@link OneSEVPA}s.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     *
     * @return a view representing the disjunction of the specified {@link OneSEVPA}s
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> or(OneSEVPA<L1, I> sevpa1,
                                                           OneSEVPA<L2, I> sevpa2,
                                                           VPAlphabet<I> alphabet) {
        return combine(sevpa1, sevpa2, alphabet, AcceptanceCombiner.OR);
    }

    /**
     * Returns a view on the exclusive-or ("xor") of two {@link OneSEVPA}s.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     *
     * @return a view representing the exclusive-or of the specified {@link OneSEVPA}s
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> xor(OneSEVPA<L1, I> sevpa1,
                                                            OneSEVPA<L2, I> sevpa2,
                                                            VPAlphabet<I> alphabet) {
        return combine(sevpa1, sevpa2, alphabet, AcceptanceCombiner.XOR);
    }

    /**
     * Returns a view on  the equivalence ("&lt;=&gt;") of two {@link OneSEVPA}s.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     *
     * @return a view representing the equivalence of the specified {@link OneSEVPA}s
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> equiv(OneSEVPA<L1, I> sevpa1,
                                                              OneSEVPA<L2, I> sevpa2,
                                                              VPAlphabet<I> alphabet) {
        return combine(sevpa1, sevpa2, alphabet, AcceptanceCombiner.EQUIV);
    }

    /**
     * Returns a view on  the implication ("=&gt;") of two {@link OneSEVPA}s.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     *
     * @return a view representing the implication of the specified {@link OneSEVPA}s
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> impl(OneSEVPA<L1, I> sevpa1,
                                                             OneSEVPA<L2, I> sevpa2,
                                                             VPAlphabet<I> alphabet) {
        return combine(sevpa1, sevpa2, alphabet, AcceptanceCombiner.IMPL);
    }

    /**
     * Minimizes the given {@link OneSEVPA} over the given alphabet. This method does not modify the given
     * {@link OneSEVPA}, but returns the minimized version as a new instance.
     * <p>
     * <b>Note:</b> the method currently does not support partial {@link OneSEVPA}s.
     *
     * @param sevpa
     *         the SEVPA to be minimized
     * @param alphabet
     *         the input alphabet to consider for minimization (this will also be the input alphabet of the resulting
     *         automaton)
     *
     * @return a minimized version of the specified {@link OneSEVPA}
     */
    public static <I> DefaultOneSEVPA<I> minimize(OneSEVPA<?, I> sevpa, VPAlphabet<I> alphabet) {
        return OneSEVPAMinimizer.minimize(sevpa, alphabet);
    }

}
