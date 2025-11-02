package graph;

import graph.core.Graph;
import graph.core.GraphIO;
import graph.topo.KahnTopoSort;
import metrics.SimpleMetrics;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TopoCycleTest {
    @Test
    public void testCycleDetection() throws Exception {
        Graph g = GraphIO.read(Paths.get("data/small1.json")); // contains a cycle
        var metrics = new SimpleMetrics();
        assertThrows(IllegalStateException.class, () -> KahnTopoSort.sort(g, metrics));
    }
}

