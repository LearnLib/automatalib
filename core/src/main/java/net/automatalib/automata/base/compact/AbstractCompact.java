package net.automatalib.automata.base.compact;

import java.io.Serializable;
import java.util.Arrays;
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

        updateStorage(this.stateCapacity, newCap, UpdateType.STATE);

        this.stateCapacity = newCap;
    }

    @Override
    public final void addAlphabetSymbol(I symbol) {

        if (this.alphabet.containsSymbol(symbol)) {
            return;
        }

        updateStorage(this.alphabetSize, this.alphabetSize + 1, UpdateType.ALPHABET);

        this.alphabet = Alphabets.withNewSymbol(this.alphabet, symbol);
        this.alphabetSize++;
    }

    protected enum UpdateType {
        STATE,
        ALPHABET
    }

    protected abstract void updateStorage(int oldSizeHint, int newSizeHint, UpdateType type);

    protected final int[] updateStorage(int[] oldStorage, int oldSizeHint, int newSizeHint, UpdateType type) {
        switch (type) {
            case STATE: {
                final int[] newStorage = new int[newSizeHint * alphabetSize];
                System.arraycopy(oldStorage, 0, newStorage, 0, oldSizeHint * alphabetSize);
                Arrays.fill(newStorage,
                            oldSizeHint * alphabetSize,
                            newSizeHint * alphabetSize,
                            AbstractCompact.INVALID_STATE);
                return newStorage;
            }
            case ALPHABET: {
                final int[] newStorage = new int[newSizeHint * numStates];
                for (int i = 0; i < numStates; i++) {
                    System.arraycopy(oldStorage, i * oldSizeHint, newStorage, i * newSizeHint, oldSizeHint);
                    Arrays.fill(newStorage,
                                i * newSizeHint + oldSizeHint,
                                (i + 1) * newSizeHint,
                                AbstractCompact.INVALID_STATE);
                }
                return newStorage;
            }
            default:
                throw new IllegalArgumentException("Unknown update type: " + type);
        }
    }

    protected final Object[] updateStorage(Object[] oldStorage, int oldSizeHint, int newSizeHint, UpdateType type) {
        switch (type) {
            case STATE: {
                final Object[] newStorage = new Object[newSizeHint * alphabetSize];
                System.arraycopy(oldStorage, 0, newStorage, 0, oldSizeHint * alphabetSize);
                Arrays.fill(newStorage,
                            oldSizeHint * alphabetSize,
                            newSizeHint * alphabetSize,
                            AbstractCompact.INVALID_STATE);
                return newStorage;
            }
            case ALPHABET: {
                final Object[] newStorage = new Object[newSizeHint * numStates];
                for (int i = 0; i < numStates; i++) {
                    System.arraycopy(oldStorage, i * oldSizeHint, newStorage, i * newSizeHint, oldSizeHint);
                    Arrays.fill(newStorage,
                                i * newSizeHint + oldSizeHint,
                                (i + 1) * newSizeHint,
                                AbstractCompact.INVALID_STATE);
                }
                return newStorage;
            }
            default:
                throw new IllegalArgumentException("Unknown update type: " + type);
        }
    }

    protected static int getId(Integer id) {
        return (id != null) ? id.intValue() : INVALID_STATE;
    }

    protected static Integer makeId(int id) {
        return (id != INVALID_STATE) ? Integer.valueOf(id) : null;
    }

    public abstract void setStateProperty(int state, SP property);

}
