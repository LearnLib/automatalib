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

import net.automatalib.commons.util.Pair;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.ModalEdgePropertyImpl;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PartialOrderTest {

    @Test
    public void refinementOfTest01() {
        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'd');
        final CompactMTS<String> a = new CompactMTS<>(alphabet);
        final CompactMTS<String> b = new CompactMTS<>(alphabet);

        final Integer as0 = a.addInitialState();
        final Integer as1 = a.addState();
        final Integer as2 = a.addState();

        final Integer bs0 = b.addInitialState();
        final Integer bs1 = b.addState();

        a.addTransition(as0, "a", as1, null);
        a.addTransition(as0, "c", as1, new ModalEdgePropertyImpl(ModalType.MAY));
        a.addTransition(as1, "a", as2, null);
        a.addTransition(as2, "a", as2, null);

        b.addTransition(bs0, "a", bs0, null);
        b.addTransition(bs0, "c", bs1, null);
        b.addTransition(bs1, "a", bs1, null);
        b.addTransition(bs0, "b", bs0, null);

        Assert.assertTrue(PartialOrder.refinementOf(a, b, alphabet));
    }

    @Test
    void refinementOfTest02() {
        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'd');
        final CompactMTS<String> a = new CompactMTS<>(alphabet);
        final CompactMTS<String> b = new CompactMTS<>(alphabet);

        final Integer as0 = a.addInitialState();
        final Integer as1 = a.addState();
        final Integer as2 = a.addState();

        final Integer bs0 = b.addInitialState();
        final Integer bs1 = b.addState();

        a.addTransition(as0, "a", as1, null);
        a.addTransition(as0, "c", as1, new ModalEdgePropertyImpl(ModalType.MUST));

        b.addTransition(bs0, "a", bs0, null);
        b.addTransition(bs0, "c", bs1, new ModalEdgePropertyImpl(ModalType.MAY));

        Assert.assertFalse(PartialOrder.refinementOf(a, b, alphabet));
    }

    /*
    bisimulation implies refinement
     */
    @Test
    public void bisimTestLoop() {
        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'd');
        final CompactMTS<String> a = new CompactMTS<>(alphabet);
        final CompactMTS<String> b = new CompactMTS<>(alphabet);

        final Integer as0 = a.addInitialState();
        final Integer as1 = a.addState();
        final Integer as2 = a.addState();

        final Integer bs0 = b.addInitialState();

        a.addTransition(as0, "a", as1, null);
        a.addTransition(as1, "a", as2, null);
        a.addTransition(as2, "a", as2, null);

        b.addTransition(bs0, "a", bs0, null);

        Assert.assertTrue(PartialOrder.refinementOf(a, b, alphabet));
    }

    @Test
    public void bisimTestDiff() {
        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'd');
        final CompactMTS<String> a = new CompactMTS<>(alphabet);
        final CompactMTS<String> b = new CompactMTS<>(alphabet);

        final Integer as0 = a.addInitialState();
        final Integer as1 = a.addState();
        final Integer as2 = a.addState();

        final Integer bs0 = b.addInitialState();

        a.addTransition(as0, "a", as1, null);
        a.addTransition(as1, "a", as2, null);
        a.addTransition(as2, "a", as2, null);

        b.addTransition(bs0, "a", bs0, null);
        b.addTransition(bs0, "b", bs0, null);

        Assert.assertTrue(PartialOrder.refinementRelation(a, b, alphabet).contains(Pair.of(as0, bs0)));
        Assert.assertTrue(PartialOrder.refinementOf(a, b, alphabet));
    }
}