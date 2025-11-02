package graph.topo;

import graph.core.Graph;
import graph.core.Edge;
import metrics.Metrics;

import java.util.*;

/**
 * Алгоритм Кана для топологической сортировки DAG.
 * Работает за O(V + E).
 */
public class KahnTopoSort {

    public static TopoSortResult sort(Graph g, Metrics metrics) {
        int n = g.n();
        int[] indeg = g.indegrees();
        Deque<Integer> queue = new ArrayDeque<>();

        // Инициализация — добавляем вершины с нулевой степенью захода
        for (int i = 0; i < n; i++) {
            if (indeg[i] == 0) {
                queue.add(i);
                metrics.inc(Metrics.Counter.KAHN_PUSHES);
            }
        }

        List<Integer> order = new ArrayList<>(n);

        // Основной цикл Кана
        while (!queue.isEmpty()) {
            int u = queue.remove();
            metrics.inc(Metrics.Counter.KAHN_POPS);
            order.add(u);

            for (Edge e : g.adj(u)) {
                metrics.inc(Metrics.Counter.KAHN_EDGES);
                if (--indeg[e.to] == 0) {
                    queue.add(e.to);
                    metrics.inc(Metrics.Counter.KAHN_PUSHES);
                }
            }
        }

        if (order.size() != n) {
            throw new IllegalStateException("Graph is not a DAG (cycle detected)");
        }

        return new TopoSortResult(order);
    }
}
