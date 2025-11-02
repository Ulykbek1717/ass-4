package graph;

import graph.core.Graph;
import graph.core.GraphIO;
import graph.dagsp.DagPaths;
import graph.scc.TarjanSCC;
import graph.topo.KahnTopoSort;
import graph.topo.TopoSortResult;
import metrics.SimpleMetrics;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SccAndDagTest {
    @Test
    public void testSccSmall() throws Exception {
        Graph g = GraphIO.read(Paths.get("data/small3.json"));
        var metrics = new SimpleMetrics();
        TarjanSCC scc = new TarjanSCC(g, metrics);
        assertTrue(scc.components().size() >= 2);
        int countLarge = (int)scc.components().stream().filter(c -> c.size() >= 3).count();
        assertEquals(2, countLarge);
    }

    @Test
    public void testTopoOnDag() throws Exception {
        Graph g = GraphIO.read(Paths.get("data/small2.json"));
        var metrics = new SimpleMetrics();
        TopoSortResult topo = KahnTopoSort.sort(g, metrics);
        assertEquals(g.n(), topo.order().size());
    }

    @Test
    public void testDagShortest() throws Exception {
        Graph g = GraphIO.read(Paths.get("data/dag_small1.json"));
        var metrics = new SimpleMetrics();
        var topo = KahnTopoSort.sort(g, metrics);
        DagPaths.ShortestResult res = DagPaths.shortest(g, 0, topo.order(), metrics);
        // Expected shortest distance to node 5 is 8: 0->1(2) ->2(1) ->4(3) ->3(2) ->5(1) = 2+1+3+2+1=9? Let's compute 0->1->3->5 = 2 +7 +1 = 10, 0->2->4->3->5 = 4+3+2+1=10, 0->1->2->4->5 = 2+1+3+5=11, 0->1->2->4->3->5 = 2+1+3+2+1 = 9. Shortest should be 9.
        assertEquals(9.0, res.dist()[5], 1e-9);
    }
}

