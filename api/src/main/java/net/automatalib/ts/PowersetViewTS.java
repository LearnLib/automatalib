package net.automatalib.ts;

import java.util.Collection;

public interface PowersetViewTS<S,I,T,OS,OT> extends DeterministicTransitionSystem<S, I, T> {
	
	public Collection<? extends OS> getOriginalStates(S state);
	
	public Collection<? extends OT> getOriginalTransitions(T transition);

}
