package net.automatalib.automata.base.compact;

import java.io.Serializable;
import java.util.Collection;

import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.MutableDeterministic.FullIntAbstraction;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * Abstract super class for compact (i.e. array-based) automata representations.
 *
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 */
public abstract class AbstractCompact<I, T, SP, TP> implements MutableAutomaton<Integer, I, T, SP, TP>,
                                                               StateIDs<Integer>,
                                                               UniversalFiniteAlphabetAutomaton<Integer, I, T, SP, TP>,
                                                               GrowableAlphabetAutomaton<I>,
                                                               Serializable {

    protected static final float DEFAULT_RESIZE_FACTOR = 1.5f;
    protected static final int DEFAULT_INIT_CAPACITY = 11;
    protected static final int INVALID_STATE = FullIntAbstraction.INVALID_STATE;

    protected Alphabet<I> alphabet;
    private final float resizeFactor;
    protected int alphabetSize;
    private int stateCapacity;
    private int numStates;

    public AbstractCompact(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public AbstractCompact(Alphabet<I> alphabet, AbstractCompact<?, ?, ?, ?> other) {
        this(alphabet, other.stateCapacity, other.resizeFactor);
    }

    public AbstractCompact(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        this.alphabet = alphabet;
        this.alphabetSize = alphabet.size();
        this.resizeFactor = resizeFactor;
        this.stateCapacity = stateCapacity;
    }

    @Override
    public Collection<Integer> getStates() {
        return CollectionsUtil.intRange(0, numStates);
    }

    @Override
    public StateIDs<Integer> stateIDs() {
        return this;
    }

    @Override
    public int size() {
        return numStates;
    }

    @Override
    public int getStateId(Integer state) {
        return getId(state);
    }

    @Override
    public Integer getState(int id) {
        return makeId(id);
    }

    @Override
    public void clear() {
        numStates = 0;
    }

    @Override
    public final Integer addState(SP property) {
        int newState = numStates++;
        ensureCapacity(numStates);
        setStateProperty(newState, property);
        return newState;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return alphabet;
    }

    private void ensureCapacity(int newCapacity) {
        if (newCapacity <= stateCapacity) {
            return;
        }

        final int newCap = Math.max((int) (stateCapacity * resizeFactor), newCapacity);

        increaseStateCapacity(stateCapacity, newCap);
        this.stateCapacity = newCap;
    }

    @Override
    public final void addAlphabetSymbol(I symbol) {

        if (this.alphabet.containsSymbol(symbol)) {
            return;
        }

        final int oldAlphabetSize = this.alphabetSize;
        final int newAlphabetSize = oldAlphabetSize + 1;
        final int newArraySize = newAlphabetSize * this.stateCapacity;

        increaseAlphabetCapacity(oldAlphabetSize, newAlphabetSize, newArraySize);

        this.alphabet = Alphabets.withNewSymbol(this.alphabet, symbol);
        this.alphabetSize = newAlphabetSize;
    }

    protected abstract void increaseStateCapacity(int oldCapacity, int newCapacity);

    protected abstract void increaseAlphabetCapacity(int oldAlphabetSize, int newAlphabetSize, int newCapacity);

    protected static int getId(Integer id) {
        return (id != null) ? id.intValue() : INVALID_STATE;
    }

    protected static Integer makeId(int id) {
        return (id != INVALID_STATE) ? Integer.valueOf(id) : null;
    }

}
