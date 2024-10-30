package net.automatalib.util.partitionrefinement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.fsa.NFA;

public class ValmariInitializers {

    public static <S, I, A extends NFA<S, I> & InputAlphabetHolder<I>> Valmari initializeNFA(A nfa) {
        return initializeNFA(nfa, nfa.getInputAlphabet());
    }

    public static <S, I> Valmari initializeNFA(NFA<S, I> nfa, Alphabet<I> alphabet) {
        return initializeUniversal(nfa, alphabet, nfa::getStateProperty);
    }

    public static <S, I, T> Valmari initializeUniversal(UniversalAutomaton<S, I, T, ?, ?> automaton,
                                                        Alphabet<I> alphabet,
                                                        Function<? super S, ?> initialClassifier) {

        final int n = automaton.size();
        final int k = alphabet.size();

        final int[] blocks = new int[n];
        final StateIDs<S> stateIDs = automaton.stateIDs();
        final Map<Object, Integer> signatures = new HashMap<>();

        int m = 0;
        int cnt = 0;

        for (int i = 0; i < n; i++) {
            final S s = stateIDs.getState(i);
            final Object sig = initialClassifier.apply(s);
            Integer id = signatures.get(sig);
            if (id == null) {
                id = cnt;
                signatures.put(sig, cnt++);
            }
            blocks[i] = id;

            for (int j = 0; j < k; j++) {
                m += automaton.getTransitions(s, alphabet.getSymbol(j)).size();
            }
        }

        final int[] tail = new int[m];
        final int[] label = new int[m];
        final int[] head = new int[m];

        cnt = 0;

        for (int i = 0; i < k; i++) {
            final I symbol = alphabet.getSymbol(i);
            for (int j = 0; j < n; j++) {
                final S state = stateIDs.getState(j);
                for (T t : automaton.getTransitions(state, symbol)) {
                    final S succ = automaton.getSuccessor(t);
                    final int succId = stateIDs.getStateId(succ);

                    tail[cnt] = j;
                    label[cnt] = i;
                    head[cnt] = succId;
                    cnt++;
                }
            }
        }

        return new Valmari(blocks, tail, label, head);
    }
}
