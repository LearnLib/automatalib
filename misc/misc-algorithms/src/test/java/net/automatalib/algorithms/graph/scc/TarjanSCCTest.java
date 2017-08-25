package net.automatalib.algorithms.graph.scc;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import net.automatalib.graphs.base.compact.CompactSimpleGraph;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TarjanSCCTest {

    Integer n0, n1, n2, n3, n4, n5;
    private CompactSimpleGraph<Void> graph;

    @BeforeClass
    public void setUp() {
        graph = new CompactSimpleGraph<>();

        n0 = graph.addNode();
        n1 = graph.addNode();
        n2 = graph.addNode();
        n3 = graph.addNode();

        graph.connect(n0, n1);
        graph.connect(n1, n0);
        graph.connect(n1, n2);
        graph.connect(n2, n1);
        graph.connect(n2, n3);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSCC() {
        Set<Set<Integer>> sccs = SCCs.collectSCCs(graph).stream().map(HashSet::new).collect(Collectors.toSet());

        Assert.assertEquals(sccs.size(), 2);
        Assert.assertEquals(sccs, Sets.newHashSet(Sets.newHashSet(0, 1, 2), Sets.newHashSet(3)));
    }
}
