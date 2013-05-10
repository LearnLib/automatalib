package net.automatalib.util.automata.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.ts.TransitionSystem;

abstract class AbstractAutomatonCopy<S1, I1, T1, S2, I2, T2, SP2, TP2, TS1 extends TransitionSystem<S1, I1, T1>> {

	protected final TS1 in;
	protected final Collection<? extends I1> inputs;
	protected final MutableAutomaton<S2, I2, T2, SP2, TP2> out;
	protected final MutableMapping<S1,S2> stateMapping;
	protected final Mapping<? super I1,? extends I2> inputsMapping;
	protected final Mapping<? super S1,? extends SP2> spMapping;
	protected final Mapping<? super T1,? extends TP2> tpMapping;
	
	
	public AbstractAutomatonCopy(TS1 in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2, I2, T2, SP2, TP2> out,
			Mapping<? super I1, ? extends I2> inputsMapping,
			Mapping<? super S1, ? extends SP2> spMapping,
			Mapping<? super T1, ? extends TP2> tpMapping) {
		this.in = in;
		this.inputs = inputs;
		this.out = out;
		this.stateMapping = in.createStaticStateMapping();
		this.inputsMapping = inputsMapping;
		this.spMapping = spMapping;
		this.tpMapping = tpMapping;
	}

	protected S2 copyState(S1 s1) {
		SP2 prop = spMapping.get(s1);
		S2 s2 = out.addState(prop);
		stateMapping.put(s1, s2);
		return s2;
	}
	
	protected S2 copyInitialState(S1 s1) {
		SP2 prop = spMapping.get(s1);
		S2 s2 = out.addInitialState(prop);
		stateMapping.put(s1, s2);
		return s2;
	}
	
	protected T2 copyTransition(S2 src2, I2 input2, T1 trans1, S1 succ1) {
		TP2 prop = tpMapping.get(trans1);
		
		S2 succ2 = stateMapping.get(succ1);
		
		T2 trans2 = out.createTransition(succ2, prop);
		out.addTransition(src2, input2, trans2);
		return trans2;
	}
	
	protected void copyTransitions(S2 src2, I2 input2, Collection<? extends T1> transitions1) {
		List<T2> transitions2 = new ArrayList<>(transitions1.size());
		
		for(T1 trans1 : transitions1) {
			S1 succ1 = in.getSuccessor(trans1);
			S2 succ2 = stateMapping.get(succ1);
			TP2 prop = tpMapping.get(trans1);
			T2 trans2 = out.createTransition(succ2, prop);
			transitions2.add(trans2);
		}
		
		out.addTransitions(src2, input2, transitions2);
	}
	
	protected S2 copyTransitionChecked(S2 src2, I2 input2, T1 trans1, S1 succ1) {
		TP2 prop = tpMapping.get(trans1);
		
		S2 succ2 = stateMapping.get(succ1);
		S2 freshSucc = null;
		if(succ2 == null)
			freshSucc = succ2 = copyState(succ1);
		
		T2 trans2 = out.createTransition(succ2, prop);
		out.addTransition(src2, input2, trans2);
		return freshSucc;
	}
	
	public abstract void doCopy();
	
	protected final void updateInitials() {
		for(S1 init1 : in.getInitialStates()) {
			S2 init2 = stateMapping.get(init1);
			if(init2 == null)
				continue;
			out.setInitial(init2, true);
		}
	}
}
