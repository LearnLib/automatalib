package net.automatalib.util.ts.modal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.ts.modal.CompactMC;
import net.automatalib.ts.modal.MTSTransition;
import net.automatalib.ts.modal.MembershipMC;
import net.automatalib.ts.modal.ModalContractEdgeProperty;
import net.automatalib.ts.modal.ModalContractMembershipEdgePropertyImpl;
import net.automatalib.ts.modal.ModalEdgeProperty;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.MutableModalContract;
import net.automatalib.ts.modal.MutableModalContractEdgeProperty;
import net.automatalib.ts.modal.MutableModalEdgeProperty;
import net.automatalib.ts.modal.MutableModalTransitionSystem;
import net.automatalib.ts.modal.TauEdge;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.fixedpoint.Closures;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.automatalib.serialization.dot.GraphDOT;

public class MCUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemComponent.class);

    private MCUtil() {
        // prevent instantiation
    }

    public static CompactMC<String> loadMCFromPath(String path) throws IOException {
        Path file = Paths.get(path);
        if (!Files.exists(file) || !file.toString().endsWith(".dot")) {
            throw new FileNotFoundException("Expected " + path + " to be an existing .dot file!");
        }

        CompactMC<String> parsed = DOTParsers.mc().readModel(file.toFile()).model;

        for (Integer s : parsed.getStates()) {
            for (String label : parsed.getInputAlphabet()) {
                for (MTSTransition<String, MutableModalContractEdgeProperty> transition : parsed.getTransitions(s, label)) {

                    if (transition.getProperty().getColor() == ModalContractEdgeProperty.EdgeColor.RED ||
                            transition.getProperty().getColor() == ModalContractEdgeProperty.EdgeColor.GREEN) {
                        parsed.getCommunicationAlphabet().add(label);
                    }

                }
            }
        }

        return parsed;
    }

    public static MembershipMC<String> loadMMCFromPath(String path) throws IOException {
        Path file = Paths.get(path);
        if (!Files.exists(file) || !file.toString().endsWith(".dot")) {
            throw new FileNotFoundException("Expected " + path + " to be an existing .dot file!");
        }

        MembershipMC<String> parsed = DOTParsers.mmc().readModel(file.toFile()).model;

        for (Integer s : parsed.getStates()) {
            for (String label : parsed.getInputAlphabet()) {
                for (MTSTransition<String, ModalContractMembershipEdgePropertyImpl> transition : parsed.getTransitions(s, label)) {

                    if (transition.getProperty().getColor() == ModalContractEdgeProperty.EdgeColor.RED ||
                        transition.getProperty().getColor() == ModalContractEdgeProperty.EdgeColor.GREEN) {
                        parsed.getCommunicationAlphabet().add(label);
                    }

                }
            }
        }

        return parsed;
    }

    public static <A extends MutableModalContract<S1, I, T1, TP1>, S1, I, T1, TP1 extends MutableModalContractEdgeProperty, B extends MutableModalTransitionSystem<S2, I, T2, TP2>, S2, T2, TP2 extends MutableModalEdgeProperty> SystemComponent<B, S2, I, T2, TP2> systemComponent(
            A modalContract,
            AutomatonCreator<B, I> creator,
            Function<? super T1, ? extends TP2> tpMapping,
            Supplier<? extends TP2> mayOnlySupplier
    ) {
        B result = creator.createAutomaton(modalContract.getInputAlphabet());

        Mapping<S1, S2> mapping = AutomatonLowLevelCopy.rawCopy(AutomatonCopyMethod.DFS,
                                                  modalContract,
                                                  modalContract.getInputAlphabet(),
                                                  result,
                                                  i -> i,
                                                  sp -> null,
                                                  tpMapping,
                                                  s -> true,
                                                  (s, i, t) -> modalContract.getTransitionProperty(t).getColor() != ModalContractEdgeProperty.EdgeColor.RED);

        S2 uniqueState = result.addState();

        for (S1 state : modalContract.getStates()) {
            for (I input : modalContract.getInputAlphabet()) {
                for (T1 transition : modalContract.getTransitions(state, input)) {
                    if (modalContract.getTransitionProperty(transition).getColor() == ModalContractEdgeProperty.EdgeColor.RED) {
                        S2 source = mapping.get(state);
                        TP2 property = mayOnlySupplier.get();
                        assert property.isMayOnly();
                        result.addTransition(source, input, uniqueState, property);
                    }
                }
            }
        }

        for (I input : modalContract.getInputAlphabet()) {
            TP2 property = mayOnlySupplier.get();
            assert property.isMayOnly();
            result.addTransition(uniqueState, input, uniqueState, property);
        }

        return SystemComponent.of(result, uniqueState);
    };

    public static <A extends MutableModalContract<S1, I, T1, TP1>, S1, I, T1, TP1 extends MutableModalContractEdgeProperty & TauEdge, B extends ModalTransitionSystem<S2, I, T2, TP2>, S2, T2, TP2 extends ModalEdgeProperty> DFA<Integer, I> redContextLanguage(
            SystemComponent<B, S2, I, T2, TP2> system,
            Collection<I> inputs
    ) {
        Pair<Map<Set<S2>,Integer>, CompactDFA<I>> res = Closures.simpleClosure(system.systemComponent, inputs, system.systemComponent.getInputAlphabet(), CompactDFA::new, (s, i, t) -> inputs.contains(i));

        CompactDFA<I> dfa = res.getSecond();
        Map<Set<S2>, Integer> mapping = res.getFirst();

        for (Map.Entry<Set<S2>,Integer> entry : mapping.entrySet()) {
            if (entry.getKey().contains(system.uniqueState)) {
                dfa.setStateProperty(entry.getValue(), Boolean.TRUE);
            }
        }
        //TODO: change to mutable versions

        CompactDFA<I> complementDfa = DFAs.complement(dfa, Alphabets.fromCollection(inputs));
        CompactDFA<I> minimalDfa = DFAs.minimize(complementDfa);

        return minimalDfa;
    }

    public static <B extends MutableModalTransitionSystem<S1, I, T, TP>, S1, I, T, TP extends MutableModalEdgeProperty, S2> B redContextComponent(
            DFA<S2, I> dfa,
            AutomatonCreator<B, I> creator,
            Collection<I> inputs,
            Supplier<? extends TP> mayOnlySupplier
    ) {
        B result = creator.createAutomaton(Alphabets.fromCollection(inputs));

        AutomatonLowLevelCopy.rawCopy(AutomatonCopyMethod.STATE_BY_STATE,
                                      dfa,
                                      inputs,
                                      result,
                                      sp -> null,
                                      s -> mayOnlySupplier.get(),
                                      dfa::isAccepting,
                                      (s,i,t) -> true);

        return result;
    }

    public static <A extends MutableModalContract<S, I, T, TP>, S, I, T, TP extends MutableModalContractEdgeProperty & TauEdge> DFA<Integer, I> greenContextLanguage(
            A modalContract
    ) {
        Alphabet<I> alphabet = modalContract.getInputAlphabet();
        Alphabet<I> gamma = modalContract.getCommunicationAlphabet();
        assert alphabet.containsAll(gamma);

        Pair<Map<Set<S>,Integer>, CompactDFA<I>> res = Closures.closure(modalContract,
                                                                         gamma,
                                                                         CompactDFA::new,
                                                                         Closures.toClosureOperator(modalContract, alphabet, (s, i, t) -> !gamma.contains(i)),
                                                                         (s, i, t) -> gamma.contains(i) & !modalContract.getTransitionProperty(t).isRed());

        CompactDFA<I> dfa = res.getSecond();
        Map<Set<S>, Integer> mapping = res.getFirst();

        // Use acceptance to remove unnecessary may transitions
        Set<S> keepStates = new HashSet<>();
        for (S src : modalContract.getStates()){
            for (I input : gamma) {
                for (T t : modalContract.getTransitions(src, input)) {
                    TP property = modalContract.getTransitionProperty(t);
                    if (!property.isRed() && property.isMust()) {
                        keepStates.add(modalContract.getSuccessor(t));
                    }
                }
            }
        }

        for (Map.Entry<Set<S>,Integer> entry : mapping.entrySet()) {
            if (!Collections.disjoint(entry.getKey(), keepStates)) {
                dfa.setStateProperty(entry.getValue(), Boolean.TRUE);
            }
        }

        //TODO: change to mutable versions
        CompactDFA<I> complementDfa = DFAs.complete(dfa, modalContract.getCommunicationAlphabet());
        CompactDFA<I> minimalDfa = DFAs.minimize(complementDfa);

        // Reset acceptance to default semantics of MTS
        Set<Integer> nonAcceptingStates = minimalDfa.getStates().parallelStream()
                  .filter(s -> minimalDfa.getInputAlphabet().parallelStream()
                                         .allMatch(i -> Objects.equals(minimalDfa.getSuccessor(s, i), s)))
                  .collect(Collectors.toSet());

        assert nonAcceptingStates.size() == 1;

        for (Integer state : minimalDfa.getStates()) {
            minimalDfa.setAccepting(state, !nonAcceptingStates.contains(state));
        }

        return minimalDfa;
    }

    public static <B extends MutableModalTransitionSystem<S1, I, T, TP>, S1, I, T, TP extends MutableModalEdgeProperty, S2> B greenContextComponent(
            DFA<S2, I> dfa,
            AutomatonCreator<B, I> creator,
            Collection<I> inputs,
            Supplier<? extends TP> mayOnlySupplier,
            Supplier<? extends TP> mustSupplier
    ) {
        B result = creator.createAutomaton(Alphabets.fromCollection(inputs));

        AutomatonLowLevelCopy.rawCopy(AutomatonCopyMethod.STATE_BY_STATE,
                                      dfa,
                                      inputs,
                                      result,
                                      sp -> null,
                                      t -> dfa.isAccepting(dfa.getSuccessor(t)) ? mustSupplier.get() : mayOnlySupplier.get(),
                                      s -> true,
                                      (s,i,t) -> true);

        return result;
    }

    public static final class SystemComponent<A extends ModalTransitionSystem<S, I ,T, TP>, S, I, T, TP extends ModalEdgeProperty> {

        public final A systemComponent;
        public final S uniqueState;

        public SystemComponent(A systemComponent, S uniqueState) {
            this.systemComponent = systemComponent;
            this.uniqueState = uniqueState;
        }

        public static <A extends MutableModalTransitionSystem<S, I ,T, TP>, S, I, T, TP extends MutableModalEdgeProperty> SystemComponent<A, S, I, T, TP> of(
                A graph,
                S state
        ) {
            return new SystemComponent<>(graph, state);
        }
    }
}
