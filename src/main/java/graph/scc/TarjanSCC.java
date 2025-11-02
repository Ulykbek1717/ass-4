package graph.scc;

import graph.core.Edge;
import graph.core.Graph;
import metrics.Metrics;

import java.util.*;

/**
 * Алгоритм Тарьяна для поиска сильно связных компонент (SCC).
 * Работает за O(V + E) с использованием одного DFS.
 */
public class TarjanSCC {
    private final Graph g;
    private final Metrics metrics;
    private final int n;

    private final int[] idx;       // время входа в вершину
    private final int[] low;       // минимальный индекс достижимой вершины
    private final boolean[] onStack;
    private final Deque<Integer> stack; // заменили Stack на Deque
    private final int[] compOf;    // номер компоненты для каждой вершины
    private final List<List<Integer>> comps = new ArrayList<>();

    private int time = 0;

    public TarjanSCC(Graph g, Metrics metrics) {
        this.g = g;
        this.metrics = metrics;
        this.n = g.n();
        this.idx = new int[n];
        this.low = new int[n];
        this.onStack = new boolean[n];
        this.stack = new ArrayDeque<>();
        this.compOf = new int[n];
        Arrays.fill(idx, -1);

        for (int v = 0; v < n; v++) {
            if (idx[v] == -1) dfs(v);
        }
    }

    private void dfs(int v) {
        metrics.inc(Metrics.Counter.SCC_DFS_VISITS);
        idx[v] = low[v] = time++;
        stack.push(v);
        onStack[v] = true;

        for (Edge e : g.adj(v)) {
            metrics.inc(Metrics.Counter.SCC_DFS_EDGES);
            int w = e.to;

            if (idx[w] == -1) {
                dfs(w);
                low[v] = Math.min(low[v], low[w]);
            } else if (onStack[w]) {
                low[v] = Math.min(low[v], idx[w]);
            }
        }

        // Если v — корень компоненты
        if (low[v] == idx[v]) {
            List<Integer> comp = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                compOf[w] = comps.size();
                comp.add(w);
            } while (w != v);
            comps.add(comp);
        }
    }

    public List<List<Integer>> components() {
        return comps;
    }

    public int[] compOf() {
        return compOf;
    }
}
