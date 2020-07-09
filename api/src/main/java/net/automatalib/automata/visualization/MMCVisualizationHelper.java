package net.automatalib.automata.visualization;

import java.util.Map;

import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.ts.modal.GroupMemberEdge;
import net.automatalib.ts.modal.ModalContract;
import net.automatalib.ts.modal.ModalContractEdgeProperty;

public class MMCVisualizationHelper<S, I, T, TP extends ModalContractEdgeProperty & GroupMemberEdge, M extends ModalContract<S, I, T, TP>>
        extends MCVisualizationHelper<S, I, T, TP, M> {

    public MMCVisualizationHelper(M mc) {
        super(mc);
    }

    @Override
    public boolean getEdgeProperties(S src, TransitionEdge<I, T> edge, S tgt, Map<String, String> properties) {
        if (!super.getEdgeProperties(src, edge, tgt, properties)) {
            return false;
        }

        TP transitionProperty = super.automaton.getTransitionProperty(edge.getTransition());

        int memberId = transitionProperty.getMemberId();
        if (0 <= memberId && memberId <= 4) {
            //properties.compute(EdgeAttrs.LABEL,
            //                   (key, value) -> value != null ? value + " #" + memberId : "#" + memberId );
            properties.put("colorscheme", "ylorrd9");
            properties.put(EdgeAttrs.COLOR, String.valueOf(memberId+1));
        }
        else if (5 <= memberId && memberId <= 11) {
            //properties.compute(EdgeAttrs.LABEL,
            //                   (key, value) -> value != null ? value + " #" + memberId : "#" + memberId );
            properties.put("colorscheme", "reds9");
            properties.put(EdgeAttrs.COLOR, String.valueOf(memberId-3));
        } else if (12 <= memberId) {
            //properties.compute(EdgeAttrs.LABEL,
            //                   (key, value) -> value != null ? value + " #" + memberId : "#" + memberId );
            properties.put("colorscheme", "rdpu9");
            properties.put(EdgeAttrs.COLOR, String.valueOf(memberId-9));
        }

        properties.put(MMCAttrs.MEMBERSHIP, String.valueOf(memberId));

        return true;
    }
}
