/* Copyright (C) 2013-2020 TU Dortmund
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
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;

import com.google.common.collect.Sets;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModalRefinement {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModalRefinement.class);

    private ModalRefinement() {
        // do not instantiate
    }

    private static <BS, I, BT, BTP extends ModalEdgeProperty> Set<BT> partnerTransitions(ModalTransitionSystem<BS, I, BT, BTP> b,
                                                                                         BS source,
                                                                                         I input,
                                                                                         Set<ModalEdgeProperty.ModalType> acceptableValues) {

        Set<BT> coTransitions = Sets.newHashSetWithExpectedSize(b.getInputAlphabet().size());

        for (BT candidateTransition : b.getTransitions(source, input)) {
            BTP property = b.getTransitionProperty(candidateTransition);

            if (property != null && acceptableValues.contains(property.getType())) {
                coTransitions.add(candidateTransition);
            }
        }

        return coTransitions;
    }

    private static <AS, I, AT, ATP extends ModalEdgeProperty, BS, BT, BTP extends ModalEdgeProperty> boolean eligiblePartner(
            ModalTransitionSystem<AS, I, AT, ATP> a,
            ModalTransitionSystem<BS, I, BT, BTP> b,
            Collection<I> inputs,
            BiFunction<AS, BS, Boolean> inRefinementRelation,
            AS source,
            BS coSource,
            Set<ModalEdgeProperty.ModalType> acceptableValues) {

        for (I label : inputs) {
            for (AT transition : a.getTransitions(source, label)) {
                if (!acceptableValues.contains(a.getTransitionProperty(transition).getType())) {
                    continue;
                }
                LOGGER.debug("Searching corresponding transition for {}", transition);

                Set<BT> partnerTransitions = partnerTransitions(b, coSource, label, acceptableValues);

                AS target = a.getTarget(transition);
                final boolean eligablePartner = partnerTransitions.stream()
                                                                  .map(b::getSuccessor)
                                                                  .anyMatch(s -> inRefinementRelation.apply(target, s));

                if (!eligablePartner) {
                    return false;
                } else {
                    LOGGER.debug("Found transitions {}", partnerTransitions);
                }
            }
        }

        return true;
    }

    public static <AS, I, AT, ATP extends ModalEdgeProperty, BS, BT, BTP extends ModalEdgeProperty> Set<Pair<AS, BS>> refinementRelation(
            ModalTransitionSystem<AS, I, AT, ATP> implementation,
            ModalTransitionSystem<BS, I, BT, BTP> specification,
            Collection<I> inputs) {

        Set<Pair<AS, BS>> refinement = Sets.newHashSetWithExpectedSize(implementation.size() * specification.size());

        // lower approximation only correct if automaton is finite (image-finite)
        for (AS p : implementation.getStates()) {
            for (BS q : specification.getStates()) {
                refinement.add(Pair.of(p, q));
            }
        }

        Set<ModalEdgeProperty.ModalType> may = Sets.newHashSetWithExpectedSize(2);
        may.add(ModalEdgeProperty.ModalType.MAY);
        may.add(ModalEdgeProperty.ModalType.MUST);

        Set<ModalEdgeProperty.ModalType> must = Sets.newHashSetWithExpectedSize(1);
        must.add(ModalEdgeProperty.ModalType.MUST);

        boolean update = true;
        while (update) {
            update = false;
            LOGGER.debug("Pairs {}", refinement);

            Iterator<Pair<AS, BS>> iterator = refinement.iterator();
            while (iterator.hasNext()) {
                Pair<AS, BS> pair = iterator.next();
                LOGGER.debug("Checking {}", pair);

                boolean eligiblePartner = eligiblePartner(implementation,
                                                          specification,
                                                          inputs,
                                                          (s, t) -> refinement.contains(Pair.of(s, t)),
                                                          pair.getFirst(),
                                                          pair.getSecond(),
                                                          may);

                eligiblePartner &= eligiblePartner(specification,
                                                   implementation,
                                                   inputs,
                                                   (s, t) -> refinement.contains(Pair.of(t, s)),
                                                   pair.getSecond(),
                                                   pair.getFirst(),
                                                   must);

                if (!eligiblePartner) {
                    LOGGER.debug("Pair {} has no partner transitions", pair);
                    update = true;
                    iterator.remove();
                }
            }

        }
        LOGGER.info("Refinement relation {}", refinement);

        return refinement;

    }

}
