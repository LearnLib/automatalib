package net.automatalib.util.ts.modal;

import com.google.common.collect.Lists;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.ts.modal.CompactMC;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.MTSTransition;
import net.automatalib.ts.modal.ModalContractEdgeProperty;
import net.automatalib.ts.modal.ModalContractEdgePropertyImpl;
import net.automatalib.ts.modal.ModalEdgeProperty;
import net.automatalib.ts.modal.ModalEdgePropertyImpl;
import net.automatalib.ts.modal.MutableModalEdgeProperty;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MCUtilTest {

    @Test
    public void testSystemComponent() {
    }

    @Test
    public void testRedContextLanguage() {

        CompactMC<Character> monitor = new CompactMC<>(Alphabets.characters('a', 'd'),
                                                       Alphabets.fromArray('b', 'd'));
        Integer s0 = monitor.addInitialState();
        Integer s1 = monitor.addState();
        Integer s2 = monitor.addState();
        Integer s3 = monitor.addState();

        monitor.addTransition(s0, 'd', s0, new ModalContractEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST, false, ModalContractEdgeProperty.EdgeColor.RED));
        monitor.addTransition(s0, 'a', s1, new ModalContractEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST, false, null));
        monitor.addTransition(s1, 'b', s2, new ModalContractEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST, false, ModalContractEdgeProperty.EdgeColor.GREEN));
        monitor.addTransition(s2, 'c', s3, new ModalContractEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST, false, null));
        monitor.addTransition(s3, 'd', s0, new ModalContractEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST, false, ModalContractEdgeProperty.EdgeColor.GREEN));

        MCUtil.SystemComponent<CompactMTS<Character>, Integer, Character, MTSTransition<Character, MutableModalEdgeProperty>, MutableModalEdgeProperty> system = MCUtil.systemComponent(monitor, CompactMTS<Character>::new, t -> null, () -> new ModalEdgePropertyImpl(
                ModalEdgeProperty.ModalType.MAY));

        DFA<?, Character> dfa = MCUtil.redContextLanguage(monitor, system);

        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bb")));
        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bdb")));
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("d")));
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("dbbdd")));


    }

    @Test
    public void testRedContextComponent() {
    }
}