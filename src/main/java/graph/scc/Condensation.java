package graph.scc;

import graph.core.Graph;

import java.util.HashSet;
import java.util.Set;

public class Condensation {
    public static Graph build(Graph g, int[] compOf, int compCount) {
        Graph dag = new Graph(compCount, true);
        // avoid parallel edges
        Set<Long> seen = new HashSet<>();
        for (int u = 0; u < g.n(); u++) {
            int cu = compOf[u];
            for (var e : g.adj(u)) {
                int cv = compOf[e.to];
                if (cu != cv) {
                    long key = (((long) cu) << 32) ^ (cv & 0xffffffffL);
                    if (seen.add(key)) dag.addEdge(cu, cv, 1.0);
                }
            }
        }
        return dag;
    }
}

