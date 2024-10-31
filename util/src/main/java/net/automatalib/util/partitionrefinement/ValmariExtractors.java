package net.automatalib.util.partitionrefinement;

import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;

public class ValmariExtractors {

    public static <I> CompactNFA<I> toNFA(Valmari valmari, NFA<?, I> original, Alphabet<I> alphabet) {
        return toNFA(valmari, original, alphabet, new CompactNFA.Creator<>());
    }

    public static <S, I, A extends MutableNFA<S, I>> A toNFA(Valmari valmari,
                                                             NFA<?, I> original,
                                                             Alphabet<I> alphabet,
                                                             AutomatonCreator<A, I> creator) {
        return toNFA(valmari, original, alphabet, creator, PruningMode.PRUNE_AFTER);
    }

    public static <S, I, A extends MutableNFA<S, I>> A toNFA(Valmari valmari,
                                                             NFA<?, I> original,
                                                             Alphabet<I> alphabet,
                                                             AutomatonCreator<A, I> creator,
                                                             PruningMode pruningMode) {
        return toUniversal(valmari, original, alphabet, creator, pruningMode);
    }

    public static <S1, S2, I, T, SP, TP, A extends MutableAutomaton<S2, I, ?, SP, TP>> A toUniversal(Valmari valmari,
                                                                                                     UniversalAutomaton<S1, I, T, SP, TP> original,
                                                                                                     Alphabet<I> alphabet,
                                                                                                     AutomatonCreator<A, I> creator) {
        return toUniversal(valmari, original, alphabet, creator, PruningMode.PRUNE_AFTER);
    }

    public static <S1, S2, I, T, SP, TP, A extends MutableAutomaton<S2, I, ?, SP, TP>> A toUniversal(Valmari valmari,
                                                                                                     UniversalAutomaton<S1, I, T, SP, TP> original,
                                                                                                     Alphabet<I> alphabet,
                                                                                                     AutomatonCreator<A, I> creator,
                                                                                                     PruningMode pruningMode) {
        return pruningMode == PruningMode.PRUNE_AFTER ?
                toUniversalPrune(valmari, original, alphabet, creator) :
                toUniversalNoPrune(valmari, original, alphabet, creator);
    }

    private static <S1, S2, I, T, SP, TP, A extends MutableAutomaton<S2, I, ?, ? super SP, ? super TP>> A toUniversalPrune(
            Valmari valmari,
            UniversalAutomaton<S1, I, T, SP, TP> original,
            Alphabet<I> alphabet,
            AutomatonCreator<A, I> creator) {

        final int numBlocks = valmari.blocks.sets + 1;

        @SuppressWarnings("unchecked") // we only put S2s
        final S1[] stateMap = (S1[]) new Object[numBlocks];
        @SuppressWarnings("unchecked") // we only put S2s
        final S2[] repMap = (S2[]) new Object[numBlocks];
        final A result = creator.createAutomaton(alphabet, numBlocks);
        final StateIDs<S1> stateIDs = original.stateIDs();

        int idx = 0;
        for (S1 initialState : original.getInitialStates()) {
            final int id = stateIDs.getStateId(initialState);
            final int blockId = valmari.blocks.sidx[id];

            if (repMap[blockId] == null) {
                final SP sp = original.getStateProperty(initialState);
                final S2 init = result.addInitialState(sp);
                stateMap[idx++] = initialState;
                repMap[blockId] = init;
            }
        }

        int statesPtr = 0;
        int numStates = idx;

        while (statesPtr < numStates) {
            final int resState = statesPtr++;
            final S1 rep = stateMap[resState];
            final int blockId = valmari.blocks.sidx[stateIDs.getStateId(rep)];
            final S2 src = repMap[blockId];

            for (I sym : alphabet) {
                for (T t : original.getTransitions(rep, sym)) {
                    final S1 succ = original.getSuccessor(t);
                    final int succId = stateIDs.getStateId(succ);
                    final int succBlockId = valmari.blocks.sidx[succId];
                    S2 tgt = repMap[succBlockId];

                    if (tgt == null) {
                        final SP sp = original.getStateProperty(succ);
                        tgt = result.addState(sp);
                        repMap[succBlockId] = tgt;
                        stateMap[numStates++] = succ;
                    }

                    result.addTransition(src, sym, tgt, original.getTransitionProperty(t));
                }
            }
        }

        return result;
    }

    private static <S1, S2, I, T, SP, TP, A extends MutableAutomaton<S2, I, ?, ? super SP, ? super TP>> A toUniversalNoPrune(
            Valmari valmari,
            UniversalAutomaton<S1, I, T, SP, TP> original,
            Alphabet<I> alphabet,
            AutomatonCreator<A, I> creator) {
        final int numBlocks = valmari.blocks.sets + 1;

        @SuppressWarnings("unchecked") // we only put S2s
        final S2[] stateArray = (S2[]) new Object[numBlocks];
        final A result = creator.createAutomaton(alphabet, numBlocks);
        final StateIDs<S1> stateIDs = original.stateIDs();
        final Set<S1> initialStates = original.getInitialStates();

        for (int i = 0; i < numBlocks; i++) {
            final int origId = valmari.blocks.elems[valmari.blocks.first[i]];
            final S1 s = stateIDs.getState(origId);

            if (initialStates.contains(s)) {
                stateArray[i] = result.addInitialState(original.getStateProperty(s));
            } else {
                stateArray[i] = result.addState(original.getStateProperty(s));
            }
        }

        for (int i = 0; i < numBlocks; i++) {
            final int origId = valmari.blocks.elems[valmari.blocks.first[i]];
            final S1 s = stateIDs.getState(origId);
            for (I sym : alphabet) {
                for (T t : original.getTransitions(s, sym)) {
                    final S1 succ = original.getSuccessor(t);
                    final int succId = stateIDs.getStateId(succ);

                    result.addTransition(stateArray[i], sym, stateArray[succId], original.getTransitionProperty(t));
                }
            }
        }

        return result;
    }

}
