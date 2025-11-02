// java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graph.dagsp.DAGLongestPath;
import graph.dagsp.DAGShortestPath;
import graph.scc.CondensationBuilder;
import graph.scc.TarjanSCC;
import graph.topo.KahnTopologicalSort;
import graph.util.SCCUtils;
import metrics.MetricsTracker;

import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: scc|topo|dagsp <file-or-dir> [source]");
            return;
        }

        String mode = args[0];
        Path path = Paths.get(args[1]);

        if (Files.notExists(path)) {
            System.err.println("File or directory not found: " + path);
            return;
        }

        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, "*.json")) {
                for (Path p : ds) {
                    System.out.println("Processing: " + p);
                    try {
                        processFile(mode, p, args);
                    } catch (Exception e) {
                        System.err.println("Error processing " + p + ": " + e.getMessage());
                    }
                }
            }
        } else {
            processFile(mode, path, args);
        }
    }

    private static void processFile(String mode, Path file, String[] args) throws Exception {
        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(file.toFile());

        if (root == null) {
            System.err.println("Skipping " + file + ": empty JSON");
            return;
        }

        JsonNode nNode = root.get("n");
        JsonNode edgesNode = root.get("edges");

        if (nNode == null || !nNode.isInt()) {
            System.err.println("Skipping " + file + ": missing or invalid field `n`");
            return;
        }
        if (edgesNode == null || !edgesNode.isArray()) {
            System.err.println("Skipping " + file + ": missing or invalid field `edges`");
            return;
        }

        int n = nNode.asInt();
        JsonNode edges = edgesNode;

        List<List<Integer>> adj = new ArrayList<>();
        List<List<int[]>> adjW = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
            adjW.add(new ArrayList<>());
        }
        for (JsonNode e : edges) {
            JsonNode uNode = e.get("u");
            JsonNode vNode = e.get("v");
            if (uNode == null || vNode == null || !uNode.canConvertToInt() || !vNode.canConvertToInt()) {
                System.err.println("Skipping malformed edge in " + file + ": " + e);
                continue;
            }
            int u = uNode.asInt();
            int v = vNode.asInt();
            int w = e.has("w") && e.get("w").canConvertToInt() ? e.get("w").asInt() : 1;
            if (w == 0) w = 1;
            if (u < 0 || u >= n || v < 0 || v >= n) {
                System.err.println("Skipping out-of-range edge in " + file + ": " + e);
                continue;
            }
            adj.get(u).add(v);
            adjW.get(u).add(new int[]{v, w});
        }

        MetricsTracker sccM = new MetricsTracker();
        TarjanSCC tarjan = new TarjanSCC(adj, sccM);
        List<List<Integer>> comps = tarjan.run();

        if (mode.equals("scc")) {
            System.out.println("SCC count = " + comps.size());
            for (int i = 0; i < comps.size(); i++) {
                System.out.println(i + ": " + comps.get(i));
            }
            return;
        }

        List<List<Integer>> dag = CondensationBuilder.buildCondensation(adj, comps);
        List<List<int[]>> dagW = CondensationBuilder.buildWeightedCondensation(adj, adjW, comps);

        MetricsTracker topoM = new MetricsTracker();
        List<Integer> topo = KahnTopologicalSort.topo(dag, topoM);

        if (mode.equals("topo")) {
            System.out.println("Topo (components): " + topo);
            System.out.println("Derived tasks: " + SCCUtils.expandOrder(topo, comps));
            return;
        }

        if (mode.equals("dagsp")) {
            int src = (args.length >= 3) ? Integer.parseInt(args[2]) : 0;
            int[] compOf = SCCUtils.buildVertexToComp(comps, n);
            int compSrc = compOf[src];

            MetricsTracker shortM = new MetricsTracker();
            int[] dist = DAGShortestPath.shortestFrom(compSrc, topo, dagW, shortM);
            System.out.println("Shortest distances: " + Arrays.toString(dist));

            MetricsTracker longM = new MetricsTracker();
            DAGLongestPath.LongestResult lr =
                    DAGLongestPath.longestFrom(compSrc, topo, dagW, longM);
            int[] longDist = lr.dist();
            System.out.println("Longest distances: " + Arrays.toString(longDist));

            int best = Integer.MIN_VALUE;
            int target = -1;
            for (int i = 0; i < longDist.length; i++) {
                if (longDist[i] > best) {
                    best = longDist[i];
                    target = i;
                }
            }
            if (target != -1) {
                System.out.println("Critical path (components): " +
                        DAGLongestPath.rebuildPath(target, lr));
                System.out.println("Critical length: " + best);
            }
        }
    }
}
