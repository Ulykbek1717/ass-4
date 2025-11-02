package app;

import graph.core.Graph;
import graph.core.GraphIO;
import graph.scc.Condensation;
import graph.scc.TarjanSCC;
import graph.topo.KahnTopoSort;
import graph.topo.TopoSortResult;
import graph.dagsp.DagPaths;
import metrics.Metrics;
import metrics.SimpleMetrics;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class App {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            printUsage();
            return;
        }

        String cmd = args[0];
        Path path = Paths.get(args[1]);
        Graph graph;

        switch (cmd) {
            case "scc":
                graph = GraphIO.read(path);
                runScc(graph, new SimpleMetrics());
                break;
            case "topo":
                graph = GraphIO.read(path);
                runTopo(graph, new SimpleMetrics());
                break;
            case "dagsp-shortest":
                if (args.length < 3) {
                    System.out.println("Provide source vertex index for shortest paths.");
                    return;
                }
                graph = GraphIO.read(path);
                runDagShortest(graph, Integer.parseInt(args[2]), new SimpleMetrics());
                break;
            case "dagsp-longest":
                graph = GraphIO.read(path);
                runDagLongest(graph, new SimpleMetrics());
                break;
            case "all":
                runAllFiles(path);
                break;
            default:
                printUsage();
        }
    }

    private static void runScc(Graph g, Metrics m) {
        long t0 = System.nanoTime();
        TarjanSCC scc = new TarjanSCC(g, m);
        List<List<Integer>> comps = scc.components();
        long t1 = System.nanoTime();

        System.out.println("SCC count: " + comps.size());
        for (int i = 0; i < comps.size(); i++)
            System.out.println("Component " + i + " (" + comps.get(i).size() + "): " + comps.get(i));

        Graph cond = Condensation.build(g, scc.compOf(), comps.size());
        printResult("Condensation DAG", cond.n(), cond.m(), t1 - t0, m);
    }

    private static void runTopo(Graph g, Metrics m) {
        TarjanSCC scc = new TarjanSCC(g, m);
        List<List<Integer>> comps = scc.components();
        Graph dag = Condensation.build(g, scc.compOf(), comps.size());

        long t0 = System.nanoTime();
        TopoSortResult topo = KahnTopoSort.sort(dag, m);
        long t1 = System.nanoTime();

        System.out.println("Topo order (components): " + topo.order());
        List<Integer> orderTasks = topo.order().stream()
                .flatMap(cid -> comps.get(cid).stream())
                .collect(Collectors.toList());
        System.out.println("Derived task order: " + orderTasks);
        printResult("TopoSort", dag.n(), dag.m(), t1 - t0, m);
    }

    private static void runDagShortest(Graph dag, int src, Metrics m) {
        TopoSortResult topo = KahnTopoSort.sort(dag, m);
        DagPaths.ShortestResult r = DagPaths.shortest(dag, src, topo.order(), m);
        System.out.println("Shortest from " + src + ": " + Arrays.toString(r.dist()));
        System.out.println("Paths: " + r.paths());
        System.out.println("Metrics: " + m.snapshot());
    }

    private static void runDagLongest(Graph dag, Metrics m) {
        TopoSortResult topo = KahnTopoSort.sort(dag, m);
        DagPaths.LongestResult r = DagPaths.longest(dag, topo.order(), m);
        System.out.println("Critical path len=" + r.maxLen() + ", path=" + r.path());
        System.out.println("Metrics: " + m.snapshot());
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java -cp target/smart-graph.jar app.App scc data/small1.json");
        System.out.println("  java -cp target/smart-graph.jar app.App topo data/small1.json");
        System.out.println("  java -cp target/smart-graph.jar app.App dagsp-shortest data/dag_small1.json 0");
        System.out.println("  java -cp target/smart-graph.jar app.App dagsp-longest data/dag_small1.json");
        System.out.println("  java -cp target/smart-graph.jar app.App all data/");
    }

    private static void runAllFiles(Path path) throws Exception {
        List<String[]> rows = new ArrayList<>();

        List<Path> files = Files.isDirectory(path)
                ? Files.list(path).filter(p -> p.toString().endsWith(".json")).toList()
                : Files.exists(path) ? List.of(path) : List.of();

        if (files.isEmpty()) {
            System.out.println("No JSON files found at: " + path);
            return;
        }

        for (Path p : files) {
            System.out.println("Processing: " + p);
            try {
                Graph g = GraphIO.read(p);

                // SCC
                Metrics m1 = new SimpleMetrics();
                long t0 = System.nanoTime();
                TarjanSCC scc = new TarjanSCC(g, m1);
                List<List<Integer>> comps = scc.components();
                Graph cond = Condensation.build(g, scc.compOf(), comps.size());
                long t1 = System.nanoTime();
                printResult("SCC", cond.n(), cond.m(), t1 - t0, m1);
                rows.add(row(p, "SCC", "count=" + comps.size(), t1 - t0, m1));

                // TOPO
                Metrics m2 = new SimpleMetrics();
                t0 = System.nanoTime();
                TopoSortResult topo = KahnTopoSort.sort(cond, m2);
                t1 = System.nanoTime();
                printResult("TopoSort", cond.n(), cond.m(), t1 - t0, m2);
                rows.add(row(p, "TOPO", topo.order().toString(), t1 - t0, m2));

                // SHORTEST
                Metrics m3 = new SimpleMetrics();
                t0 = System.nanoTime();
                DagPaths.ShortestResult sres = DagPaths.shortest(cond, 0, topo.order(), m3);
                t1 = System.nanoTime();
                System.out.println("Shortest[0]: " + Arrays.toString(sres.dist()));
                rows.add(row(p, "DAG Shortest", Arrays.toString(sres.dist()), t1 - t0, m3));

                // LONGEST
                Metrics m4 = new SimpleMetrics();
                t0 = System.nanoTime();
                DagPaths.LongestResult lres = DagPaths.longest(cond, topo.order(), m4);
                t1 = System.nanoTime();
                System.out.println("Longest path len=" + lres.maxLen() + ", path=" + lres.path());
                rows.add(row(p, "DAG Longest", lres.path().toString(), t1 - t0, m4));

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                rows.add(new String[]{p.getFileName().toString(), "ERROR", e.toString(), "0", ""});
            }
        }

        Path out = Paths.get("results.csv");
        try (var pw = new java.io.PrintWriter(Files.newBufferedWriter(out))) {
            pw.println("File,Algorithm,Output,Time(ns),Metrics");
            rows.forEach(r -> pw.println(Arrays.stream(r)
                    .map(f -> f.contains(",") ? "\"" + f.replace("\"", "\"\"") + "\"" : f)
                    .collect(Collectors.joining(","))));
        }
        System.out.println("Results written to results.csv");
    }

    private static void printResult(String name, int n, int m, long time, Metrics met) {
        System.out.printf("%s: n=%d, m=%d, time=%dns, metrics=%s%n", name, n, m, time, met.snapshot());
    }

    private static String[] row(Path p, String alg, String out, long time, Metrics m) {
        return new String[]{p.getFileName().toString(), alg, out, String.valueOf(time), m.snapshot().toString()};
    }
}
