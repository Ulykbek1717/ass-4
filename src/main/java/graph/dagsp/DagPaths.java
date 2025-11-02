package graph.dagsp;

import graph.core.Edge;
import graph.core.Graph;
import metrics.Metrics;

import java.util.*;

public class DagPaths {

    public static class ShortestResult {
        private final double[] dist;
        private final List<List<Integer>> paths;

        public ShortestResult(double[] dist, List<List<Integer>> paths) {
            this.dist = dist;
            this.paths = paths;
        }

        public double[] dist() { return dist; }
        public List<List<Integer>> paths() { return paths; }
    }

    public static ShortestResult shortest(Graph dag, int src, List<Integer> topo, Metrics metrics) {
        int n = dag.n();
        double INF = Double.POSITIVE_INFINITY;
        double[] dist = new double[n];
        int[] parent = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        dist[src] = 0.0;

        for (int u : topo) {
            if (dist[u] == INF) continue;
            for (Edge e : dag.adj(u)) {
                double nd = dist[u] + e.w;
                metrics.inc(Metrics.Counter.DAG_RELAXATIONS);
                if (nd < dist[e.to]) {
                    dist[e.to] = nd;
                    parent[e.to] = u;
                }
            }
        }

        List<List<Integer>> paths = new ArrayList<>(n);
        for (int v = 0; v < n; v++) paths.add(reconstruct(parent, src, v));
        return new ShortestResult(dist, paths);
    }

    public static class LongestResult {
        private final double maxLen;
        private final List<Integer> path;

        public LongestResult(double maxLen, List<Integer> path) {
            this.maxLen = maxLen;
            this.path = path;
        }

        public double maxLen() { return maxLen; }
        public List<Integer> path() { return path; }
    }

    public static LongestResult longest(Graph dag, List<Integer> topo, Metrics metrics) {
        int n = dag.n();
        double[] best = new double[n];
        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        for (int u : topo) {
            for (Edge e : dag.adj(u)) {
                double cand = best[u] + e.w;
                metrics.inc(Metrics.Counter.DAG_RELAXATIONS);
                if (cand > best[e.to]) {
                    best[e.to] = cand;
                    parent[e.to] = u;
                }
            }
        }

        int end = 0;
        for (int i = 1; i < n; i++)
            if (best[i] > best[end]) end = i;

        List<Integer> path = reconstruct(parent, -1, end);
        return new LongestResult(best[end], path);
    }

    private static List<Integer> reconstruct(int[] parent, int src, int v) {
        if (v < 0) return List.of();

        ArrayDeque<Integer> stack = new ArrayDeque<>();
        int cur = v;

        if (src >= 0 && parent[v] == -1 && src != v)
            return List.of();

        while (cur != -1) {
            stack.push(cur);
            cur = parent[cur];
        }

        return new ArrayList<>(stack);
    }
}
