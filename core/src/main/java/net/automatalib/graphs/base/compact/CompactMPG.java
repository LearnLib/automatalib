package net.automatalib.graphs.base.compact;

import java.util.Collections;
import java.util.Set;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import net.automatalib.graphs.MutableModalProcessGraph;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import net.automatalib.ts.modal.transition.MutableModalEdgeProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactMPG<L, AP>
        extends AbstractCompactGraph<CompactMPGEdge<L, MutableModalEdgeProperty>, Set<AP>, MutableModalEdgeProperty>
        implements MutableModalProcessGraph<Integer, L, CompactMPGEdge<L, MutableModalEdgeProperty>, AP, MutableModalEdgeProperty> {

    private final ResizingArrayStorage<Set<AP>> nodeProperties;
    private int initialNode;
    private int finalNode;

    public CompactMPG() {
        super();
        this.nodeProperties = new ResizingArrayStorage<>(Set.class);
        this.initialNode = -1;
        this.finalNode = -1;
    }

    @Override
    public void setInitialNode(@Nullable Integer initialNode) {
        if (initialNode == null) {
            this.initialNode = -1;
        } else {
            this.initialNode = initialNode;
        }
    }

    @Override
    public void setFinalNode(@Nullable Integer finalNode) {
        if (finalNode == null) {
            this.finalNode = -1;
        } else {
            this.finalNode = finalNode;
        }
    }

    @Override
    public L getEdgeLabel(CompactMPGEdge<L, MutableModalEdgeProperty> edge) {
        return edge.getLabel();
    }

    @Override
    public void setEdgeLabel(CompactMPGEdge<L, MutableModalEdgeProperty> edge, L label) {
        edge.setLabel(label);
    }

    @Override
    public @Nullable Integer getFinalNode() {
        return this.finalNode < 0 ? null : this.finalNode;
    }

    @Override
    public @Nullable Integer getInitialNode() {
        return this.initialNode < 0 ? null : this.initialNode;
    }

    @Override
    public void setNodeProperty(int node, @Nullable Set<AP> property) {
        nodeProperties.ensureCapacity(node);
        nodeProperties.array[node] = property;
    }

    @Override
    protected CompactMPGEdge<L, MutableModalEdgeProperty> createEdge(int source,
                                                                     int target,
                                                                     @Nullable MutableModalEdgeProperty property) {
        final MutableModalEdgeProperty prop;

        if (property == null) {
            prop = new ModalEdgePropertyImpl(ModalType.MUST);
        } else {
            prop = property;
        }

        return new CompactMPGEdge<>(target, prop, null);
    }

    @Override
    public Set<AP> getNodeProperties(int node) {
        if (node > nodeProperties.array.length) {
            return Collections.emptySet();
        }

        final Set<AP> props = nodeProperties.array[node];
        return props == null ? Collections.emptySet() : props;
    }
}
