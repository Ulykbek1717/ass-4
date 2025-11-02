package graph.core;

import java.util.*;

public class Graph {
    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adj;
    private int m;

    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        this.m = 0;
    }

    public int n() { return n; }
    public int m() { return m; }
    public boolean directed() { return directed; }

    public List<Edge> adj(int u) { return adj.get(u); }

    public void addEdge(int u, int v, double w) {
        adj.get(u).add(new Edge(v, w));
        m++;
        if (!directed) adj.get(v).add(new Edge(u, w));
    }

    public Graph reverse() {
        Graph r = new Graph(n, directed);
        for (int u = 0; u < n; u++) {
            for (Edge e : adj.get(u)) r.addEdge(e.to, u, e.w);
        }
        return r;
    }

    public int[] indegrees() {
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) for (Edge e : adj.get(u)) indeg[e.to]++;
        return indeg;
    }
}

