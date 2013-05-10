package net.automatalib.util.automata.copy;

import java.util.Collection;

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.Holder;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.traversal.TraversalOrder;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.util.ts.traversal.TSTraversalAction;
import net.automatalib.util.ts.traversal.TSTraversalVisitor;

final class TraversalAutomatonCopy<S1, I1, T1, S2, I2, T2, SP2, TP2> extends
		AbstractAutomatonCopy<S1, I1, T1, S2, I2, T2, SP2, TP2, TransitionSystem<S1,I1,T1>> implements TSTraversalVisitor<S1, I1, T1, S2> {

	private final TraversalOrder traversalOrder;
	private final int limit;
	
	public TraversalAutomatonCopy(TraversalOrder traversalOrder,
			int limit,
			TransitionSystem<S1, I1, T1> in,
			Collection<? extends I1> inputs,
			MutableAutomaton<S2, I2, T2, SP2, TP2> out,
			Mapping<? super I1, ? extends I2> inputsMapping,
			Mapping<? super S1, ? extends SP2> spMapping,
			Mapping<? super T1, ? extends TP2> tpMapping) {
		super(in, inputs, out, inputsMapping, spMapping, tpMapping);
		this.traversalOrder = traversalOrder;
		this.limit = limit;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.automata.copy.AbstractAutomatonCopy#doCopy()
	 */
	@Override
	public void doCopy() {
		TSTraversal.traverse(traversalOrder, in, limit, inputs, this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.ts.traversal.TSTraversalVisitor#processInitial(java.lang.Object, net.automatalib.commons.util.Holder)
	 */
	@Override
	public TSTraversalAction processInitial(S1 state, Holder<S2> outData) {
		outData.value = copyInitialState(state);
		return TSTraversalAction.EXPLORE;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.ts.traversal.TSTraversalVisitor#startExploration(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean startExploration(S1 state, S2 data) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.util.ts.traversal.TSTraversalVisitor#processTransition(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, net.automatalib.commons.util.Holder)
	 */
	@Override
	public TSTraversalAction processTransition(S1 source, S2 srcData, I1 input,
			T1 transition, S1 succ, Holder<S2> outData) {
		S2 succ2 = copyTransitionChecked(srcData, inputsMapping.get(input), transition, succ);
		if(succ2 == null)
			return TSTraversalAction.IGNORE;
		outData.value = succ2;
		return TSTraversalAction.EXPLORE;
	}

}
