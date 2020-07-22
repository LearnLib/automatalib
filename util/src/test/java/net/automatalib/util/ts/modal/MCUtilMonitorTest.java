package net.automatalib.util.ts.modal;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.ts.modal.CompactMC;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.MTSTransition;
import net.automatalib.ts.modal.transitions.ModalContractEdgeProperty;
import net.automatalib.ts.modal.transitions.ModalContractEdgePropertyImpl;
import net.automatalib.ts.modal.transitions.ModalEdgeProperty;
import net.automatalib.ts.modal.transitions.ModalEdgePropertyImpl;
import net.automatalib.ts.modal.transitions.MutableModalEdgeProperty;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MCUtilMonitorTest {

    private <S, I, T, SP, TP> Set<T> allOutgoingTransitions(UniversalAutomaton<S, I, T, SP, TP> ts, Collection<I> inputs, S state) {
        Set<T> transitions = Sets.newHashSetWithExpectedSize(inputs.size());
        for (I symbol : inputs) {
            transitions.addAll(ts.getTransitions(state, symbol));
        }
        return transitions;
    }

    private <S, I, T, SP, TP> Set<T> allIncomingTransitions(UniversalAutomaton<S, I, T, SP, TP> ts, Collection<I> inputs, S state) {
        Set<T> transitions = Sets.newHashSetWithExpectedSize(inputs.size());
        for (S s : ts.getStates()) {
            for (I symbol : inputs) {
                ts.getTransitions(s, symbol).stream()
                  .filter(t -> state.equals(ts.getSuccessor(t)))
                  .forEachOrdered(transitions::add);
            }
        }
        return transitions;
    }

    private <S, I, T, SP, TP> Set<T> allTransitions(UniversalAutomaton<S, I, T, SP, TP> ts, Collection<I> inputs) {
        Set<T> transitions = Sets.newHashSetWithExpectedSize(ts.size() * inputs.size());
        for (S state : ts.getStates()) {
            for (I symbol : inputs) {
                transitions.addAll(ts.getTransitions(state, symbol));
            }
        }
        return transitions;
    }

    protected static CompactMC<Character> monitor;

    @BeforeClass
    public void setUp() {
        monitor = new CompactMC<>(Alphabets.characters('a', 'd'),
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
    }

    @Test
    public void testSystemComponent() {
        MCUtil.SystemComponent<CompactMTS<Character>, Integer, Character, MTSTransition<Character, MutableModalEdgeProperty>, MutableModalEdgeProperty> system = MCUtil.systemComponent(monitor, CompactMTS<Character>::new, t -> null, () -> new ModalEdgePropertyImpl(
                ModalEdgeProperty.ModalType.MAY));

        assertThat(system.systemComponent.getInitialStates())
                .hasSize(1);

        Integer m0 = system.systemComponent.getInitialStates().iterator().next();

        assertThat(system.systemComponent.getTransitions(m0, 'd'))
                .hasSize(1)
                .allMatch(t -> t.getProperty().isMayOnly());

        assertThat(system.systemComponent.getTransitions(m0, 'a'))
                .hasSize(1)
                .allMatch(t -> t.getProperty().isMust());
    }

    @Test
    public void testRedContextLanguage() {
        MCUtil.SystemComponent<CompactMTS<Character>, Integer, Character, MTSTransition<Character, MutableModalEdgeProperty>, MutableModalEdgeProperty> system = MCUtil.systemComponent(monitor, CompactMTS<Character>::new, t -> null, () -> new ModalEdgePropertyImpl(
                ModalEdgeProperty.ModalType.MAY));

        DFA<?, Character> dfa = MCUtil.redContextLanguage(system, monitor.getCommunicationAlphabet());

        assertThatThrownBy(() -> dfa.accepts(Lists.charactersOf("ab"))).isInstanceOf(IllegalArgumentException.class);

        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bb")), "accept \"bb\"");
        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bdb")), "accept \"bdb\"");
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("d")), "do not accept \"d\"");
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("dbbdd")), "do not accept \"dbbdd\"");
        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bbdddbd")), "accept \"bbdddbd\"");

    }

    @Test
    public void testRedContextComponent() {
        MCUtil.SystemComponent<CompactMTS<Character>, Integer, Character, MTSTransition<Character, MutableModalEdgeProperty>, MutableModalEdgeProperty> system = MCUtil.systemComponent(monitor, CompactMTS<Character>::new, t -> null, () -> new ModalEdgePropertyImpl(
                ModalEdgeProperty.ModalType.MAY));

        DFA<?, Character> dfa = MCUtil.redContextLanguage(system, monitor.getCommunicationAlphabet());

        CompactMTS<Character> redContext = MCUtil.redContextComponent(dfa, CompactMTS::new, monitor.getCommunicationAlphabet(), () -> new ModalEdgePropertyImpl(
                ModalEdgeProperty.ModalType.MAY));

        assertThat(allTransitions(redContext, monitor.getCommunicationAlphabet()))
                .hasSize(5)
                .allMatch(t -> t.getProperty().isMayOnly());

        // the following tests require fixed state-labels
        assertThat(allOutgoingTransitions(redContext, monitor.getCommunicationAlphabet(), 0))
                .hasSize(1)
                .allMatch(t -> t.getTarget() == 1)
                .allMatch(t -> t.getLabel()  == 'b');

        assertThat(allOutgoingTransitions(redContext, monitor.getCommunicationAlphabet(), 1))
                .hasSize(2)
                .anyMatch(t -> t.getTarget() == 2)
                .anyMatch(t -> t.getLabel()  == 'b')
                .anyMatch(t -> t.getTarget() == 0)
                .anyMatch(t -> t.getLabel()  == 'd');

        assertThat(allOutgoingTransitions(redContext, monitor.getCommunicationAlphabet(), 2))
                .hasSize(2)
                .allMatch(t -> t.getTarget() == 2);
    }

    @Test
    public void testGreenContextLanguage() {
        DFA<?, Character> dfa = MCUtil.greenContextLanguage(monitor);

        assertThatThrownBy(() -> dfa.accepts(Lists.charactersOf("ab"))).isInstanceOf(IllegalArgumentException.class);

        Assert.assertTrue(dfa.accepts(Lists.charactersOf("")), "accept \"\"");
        Assert.assertTrue(dfa.accepts(Lists.charactersOf("b")), "accept \"b\"");
        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bd")), "accept \"bd\"");
        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bdb")), "accept \"bdb\"");
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("d")), "do not accept \"d\"");
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("dbbdd")), "do not accept \"dbbdd\"");
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("bbdddbd")), "do not accept \"bbdddbd\"");
    }

    @Test
    public void testGreenContextComponent() {
        DFA<?, Character> dfa = MCUtil.greenContextLanguage(monitor);

        CompactMTS<Character> greenContext = MCUtil.greenContextComponent(dfa, CompactMTS::new, monitor.getCommunicationAlphabet(), () -> new ModalEdgePropertyImpl(
                ModalEdgeProperty.ModalType.MAY), () -> new ModalEdgePropertyImpl(
                ModalEdgeProperty.ModalType.MUST));

        assertThat(greenContext.getInputAlphabet())
                .containsExactlyInAnyOrderElementsOf(monitor.getCommunicationAlphabet());

        assertThat(allTransitions(greenContext, monitor.getCommunicationAlphabet()))
                .hasSize(6);

        assertThat(greenContext.getInitialStates())
                .hasSize(1);

        Integer initial = greenContext.getInitialStates().iterator().next();

        assertThat(greenContext.getTransitions(initial, 'd'))
                .allMatch(t -> t.getProperty().isMayOnly())
                .hasSize(1);

        Integer sink = greenContext.getTransitions(initial, 'd').iterator().next().getTarget();

        assertThat(allOutgoingTransitions(greenContext, monitor.getCommunicationAlphabet(), sink))
                .allMatch(t -> t.getProperty().isMayOnly())
                .hasSize(2);

        assertThat(allIncomingTransitions(greenContext, monitor.getCommunicationAlphabet(), sink))
                .allMatch(t -> t.getProperty().isMayOnly())
                .hasSize(4);
    }
}