package net.automatalib.automata.visualization;

import java.util.Map;

import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.ts.modal.transitions.GroupMemberEdge;
import net.automatalib.ts.modal.ModalContract;
import net.automatalib.ts.modal.transitions.ModalContractEdgeProperty;

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
        ModalContractEdgeProperty.EdgeColor color = transitionProperty.getColor();

        if (color == ModalContractEdgeProperty.EdgeColor.GREEN) {
            // use colors from 3 to 7 of colorscheme greens9
            properties.put("colorscheme", "greens9");
            properties.put(EdgeAttrs.COLOR, String.valueOf(7 - (memberId % 5)));

        }
        else if (color == ModalContractEdgeProperty.EdgeColor.RED) {
            if (0 <= memberId && memberId <= 6) {
                // use colors from 3 to 9 of colorscheme reds9
                properties.put("colorscheme", "reds9");
                properties.put(EdgeAttrs.COLOR, String.valueOf(9 - memberId));
            }
            else if (7 <= memberId && memberId <= 13) {
                properties.put("colorscheme", "ylorrd9");
                properties.put(EdgeAttrs.COLOR, String.valueOf(16 - memberId));
            }
            else if (14 <= memberId) {
                properties.put("colorscheme", "rdpu9");
                properties.put(EdgeAttrs.COLOR, String.valueOf(9 - (memberId % 7)));
            }
        }

        properties.put(MMCAttrs.MEMBERSHIP, String.valueOf(memberId));

        return true;
    }
}
