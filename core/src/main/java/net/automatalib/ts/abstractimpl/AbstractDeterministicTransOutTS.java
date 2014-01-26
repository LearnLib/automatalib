package net.automatalib.ts.abstractimpl;

import java.util.List;

import net.automatalib.ts.transout.DeterministicTransitionOutputTS;

public abstract class AbstractDeterministicTransOutTS<S, I, T, O> extends AbstractDTS<S, I, T> implements
		DeterministicTransitionOutputTS<S, I, T, O> {
	
	public static <S,I,T,O>
	O getOutput(DeterministicTransitionOutputTS<S, I, T, O> _this, S state, I input) {
		T trans = _this.getTransition(state, input);
		if(trans == null) {
			return null;
		}
		return _this.getTransitionOutput(trans);
	}
	
	public static <S,I,T,O>
	boolean trace(DeterministicTransitionOutputTS<S,I,T,O> _this,
			Iterable<I> input, List<O> output) {
		return _this.trace(_this.getInitialState(), input, output);
	}
	
	public static <S,I,T,O>
	boolean trace(DeterministicTransitionOutputTS<S, I, T, O> _this,
			S state, Iterable<I> input, List<O> output) {
		
		for(I sym : input) {
			T trans = _this.getTransition(state, sym);
			if(trans == null) {
				return false;
			}
			O out = _this.getTransitionOutput(trans);
			output.add(out);
			state = _this.getSuccessor(trans);
		}
		return true;
	}
	

	@Override
	public O getOutput(S state, I input) {
		return getOutput(this, state, input);
	}

	@Override
	public boolean trace(Iterable<I> input, List<O> output) {
		return trace(this, input, output);
	}

	@Override
	public boolean trace(S state, Iterable<I> input, List<O> output) {
		return trace(this, state, input, output);
	}

}
