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

import static org.assertj.core.api.Assertions.assertThat;

public class RefinementTest {

    @Test(description = "Change may to must in implementation")
    void mayToMustTest() {
        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'c');
        final CompactMTS<String> a = new CompactMTS<>(alphabet);
        final CompactMTS<String> b = new CompactMTS<>(alphabet);

        final Integer as0 = a.addInitialState();
        final Integer as1 = a.addState();

        final Integer bs0 = b.addInitialState();
        final Integer bs1 = b.addState();

        a.addTransition(as0, "a", as1, null);
        a.addTransition(as0, "c", as1, new ModalEdgePropertyImpl(ModalType.MUST));

        b.addTransition(bs0, "a", bs0, null);
        b.addTransition(bs0, "c", bs1, new ModalEdgePropertyImpl(ModalType.MAY));

        Assert.assertFalse(MTSUtil.isRefinementOf(a, b, alphabet));
    }

    /*
    bisimulation implies refinement
     */
    @Test(description = "unroll loop")
    public void bisimTestDiff() {
        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'b');
        final CompactMTS<String> a = new CompactMTS<>(alphabet);
        final CompactMTS<String> b = new CompactMTS<>(alphabet);

        final Integer as0 = a.addInitialState();
        final Integer as1 = a.addState();

        final Integer bs0 = b.addInitialState();

        a.addTransition(as0, "a", as1, null);
        a.addTransition(as1, "a", as1, null);

        b.addTransition(bs0, "a", bs0, null);

        assertThat(ModalRefinement.refinementRelation(a, b, alphabet))
                .containsExactlyInAnyOrder(Pair.of(as0, bs0), Pair.of(as1, bs0));
        Assert.assertTrue(MTSUtil.isRefinementOf(a, b, alphabet));
    }

    @Test(description = "Example from Larsen in On Modal Refinement and Consistency")
    public void larsenTest() {
        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'b');
        final CompactMTS<String> s = new CompactMTS<>(alphabet);
        final CompactMTS<String> t = new CompactMTS<>(alphabet);

        final Integer as0 = s.addInitialState();
        final Integer as1 = s.addState();
        final Integer as2 = s.addState();

        final Integer bs0 = t.addInitialState();
        final Integer bs1 = t.addInitialState();
        final Integer bs2 = t.addInitialState();

        s.addTransition(as0, "a", as1, new ModalEdgePropertyImpl(ModalType.MAY));
        s.addTransition(as1, "b", as2, new ModalEdgePropertyImpl(ModalType.MAY));

        // TODO: consider writing a static factory method for properties and/or use an Enum type
        t.addTransition(bs0, "a", bs1, new ModalEdgePropertyImpl(ModalType.MAY));
        t.addTransition(bs0, "a", bs2, new ModalEdgePropertyImpl(ModalType.MAY));
        t.addTransition(bs1, "b", bs2, new ModalEdgePropertyImpl(ModalType.MUST));

        Assert.assertFalse(MTSUtil.isRefinementOf(s, t, alphabet));
    }

    @Test
    public void larsenTestMod1() {
        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'b');
        final CompactMTS<String> s = new CompactMTS<>(alphabet);
        final CompactMTS<String> t = new CompactMTS<>(alphabet);

        final Integer as0 = s.addInitialState();
        final Integer as1 = s.addState();
        final Integer as2 = s.addState();

        final Integer bs0 = t.addInitialState();
        final Integer bs1 = t.addInitialState();
        final Integer bs2 = t.addInitialState();

        s.addTransition(as0, "a", as1, new ModalEdgePropertyImpl(ModalType.MAY));
        s.addTransition(as1, "b", as2, new ModalEdgePropertyImpl(ModalType.MUST));

        t.addTransition(bs0, "a", bs1, new ModalEdgePropertyImpl(ModalType.MAY));
        t.addTransition(bs0, "a", bs2, new ModalEdgePropertyImpl(ModalType.MAY));
        t.addTransition(bs1, "b", bs2, new ModalEdgePropertyImpl(ModalType.MUST));

        Assert.assertTrue(MTSUtil.isRefinementOf(s, t, alphabet));
    }

    @Test(description = "Example from Jasper")
    public void jasperTest1() {
        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'b');
        final CompactMTS<String> s = new CompactMTS<>(alphabet);
        final CompactMTS<String> t = new CompactMTS<>(alphabet);

        final Integer as0 = s.addInitialState();
        final Integer as1 = s.addState();
        final Integer as2 = s.addState();

        final Integer bs0 = t.addInitialState();
        final Integer bs1 = t.addInitialState();
        final Integer bs2 = t.addInitialState();

        s.addTransition(as0, "a", as0, new ModalEdgePropertyImpl(ModalType.MUST));
        s.addTransition(as0, "b", as1, new ModalEdgePropertyImpl(ModalType.MUST));
        s.addTransition(as1, "a", as1, new ModalEdgePropertyImpl(ModalType.MUST));
        s.addTransition(as1, "b", as2, new ModalEdgePropertyImpl(ModalType.MAY));
        s.addTransition(as2, "a", as2, new ModalEdgePropertyImpl(ModalType.MAY));
        s.addTransition(as2, "b", as2, new ModalEdgePropertyImpl(ModalType.MAY));

        t.addTransition(bs0, "a", bs0, new ModalEdgePropertyImpl(ModalType.MUST));
        t.addTransition(bs0, "b", bs1, new ModalEdgePropertyImpl(ModalType.MUST));
        t.addTransition(bs1, "a", bs2, new ModalEdgePropertyImpl(ModalType.MAY));
        t.addTransition(bs1, "b", bs2, new ModalEdgePropertyImpl(ModalType.MAY));
        t.addTransition(bs2, "a", bs2, new ModalEdgePropertyImpl(ModalType.MAY));
        t.addTransition(bs2, "b", bs2, new ModalEdgePropertyImpl(ModalType.MAY));

        Assert.assertTrue(MTSUtil.isRefinementOf(s, t, alphabet));
    }

}