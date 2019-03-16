/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.util.ts.modal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.modal.ModalEdgeProperty;
import net.automatalib.ts.modal.ModalTransitionSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author msc
 */
public final class PartialOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartialOrder.class);

    private PartialOrder() {
        // prevent instantiation
    }

    public static <AS, I, AT, ATP extends ModalEdgeProperty, BS, BT, BTP extends ModalEdgeProperty> Set<Pair<AS, BS>> refinementRelation(
            ModalTransitionSystem<AS, I, AT, ATP> a,
            ModalTransitionSystem<BS, I, BT, BTP> b,
            Collection<I> inputs) {

        Set<Pair<AS, BS>> refinement = Sets.newHashSetWithExpectedSize(a.size() * b.size());
        Set<Pair<AS, BS>> change = Sets.newHashSetWithExpectedSize(a.size() * b.size());

        for (AS p : a.getStates()) {
            for (BS q : b.getStates()) {

                for (I sym : inputs) {
                    if (a.getTransitions(p, sym).isEmpty() || !b.getTransitions(q, sym).isEmpty()) {
                        LOGGER.debug("Adding pair ({},{})", p, q);
                        change.add(Pair.of(p, q));
                        break;
                    }
                }
            }
        }

        Set<Pair<AS, BS>> swap;
        boolean forall, exists;
        while (!refinement.equals(change)) {
            swap = change;
            change = refinement;
            refinement = swap;
            change.clear();

            LOGGER.debug("Pairs {}", refinement);

            for (Pair<AS, BS> p : refinement) {
                LOGGER.debug("Checking {}", p);

                forall = true;
                for (I sym : inputs) {
                    for (AT t : a.getTransitions(p.getFirst(), sym)) {
                        LOGGER.debug("Searching corresponding transition for {}", t);

                        exists = false;
                        for (BT f : b.getTransitions(p.getSecond(), sym)) {
                            if (a.getTransitionProperty(t).isMust() && b.getTransitionProperty(f).isMayOnly()) {
                                continue;
                            }
                            assert (a.getTransitionProperty(t).isMayOnly() || a.getTransitionProperty(t).isMust()) &&
                                   (b.getTransitionProperty(f).isMayOnly() || b.getTransitionProperty(f).isMust()) :
                                    "Transitions have to be may-only or must. Null or other values are not allowed.";

                            for (Pair<AS, BS> s : refinement) {
                                if (a.getSuccessor(t).equals(s.getFirst()) && b.getSuccessor(f).equals(s.getSecond())) {
                                    LOGGER.debug("Found transition {}", f);
                                    exists = true;
                                    break;
                                }
                            }
                            if (exists) {
                                break;
                            }
                        }
                        if (!exists) {
                            LOGGER.info("Found no corresponding transition for {}", t);
                            forall = false;
                            break;
                        }
                    }
                }

                if (forall) {
                    LOGGER.debug("Keeping {}", p);
                    change.add(p);
                } else {
                    LOGGER.debug("Removing {}", p);
                }
            }

        }
        LOGGER.info("Refinement relation {}", change);

        return change;

    }

    public static <AS, I, AT, ATP extends ModalEdgeProperty, BS, BT, BTP extends ModalEdgeProperty> boolean refinementOf(
            ModalTransitionSystem<AS, I, AT, ATP> a,
            ModalTransitionSystem<BS, I, BT, BTP> b,
            Collection<I> input) {

        final Set<Pair<AS, BS>> refinement = refinementRelation(a, b, input);

        final Set<AS> statesA = new HashSet<>(a.getStates());
        final Set<BS> statesB = new HashSet<>(b.getStates());

        for (Pair<AS, BS> p : refinement) {
            statesA.remove(p.getFirst());
            statesB.remove(p.getSecond());
        }

        LOGGER.info("Counterexamples: {}, {}", statesA, statesB);

        return statesA.isEmpty() && statesB.isEmpty();
    }
}
