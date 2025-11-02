package graph.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GraphIO {

    public static Graph read(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (var in = Files.newInputStream(path)) {
            JsonNode root = mapper.readTree(in);

            boolean directed = root.path("directed").asBoolean(true);

            int n = root.path("n").asInt(-1);
            JsonNode edges = root.path("edges");

            if (n < 0 && edges.isArray()) {
                int maxVertex = -1;
                for (JsonNode e : edges) {
                    int u = e.has("u") ? e.get("u").asInt() : e.path("from").asInt();
                    int v = e.has("v") ? e.get("v").asInt() : e.path("to").asInt();
                    maxVertex = Math.max(maxVertex, Math.max(u, v));
                }
                n = maxVertex + 1;
            }

            if (n < 0)
                throw new IOException("Missing 'n' or 'edges' in JSON: " + path);

            Graph g = new Graph(n, directed);

            if (edges.isArray()) {
                for (JsonNode e : edges) {
                    int u = e.has("u") ? e.get("u").asInt() : e.path("from").asInt();
                    int v = e.has("v") ? e.get("v").asInt() : e.path("to").asInt();
                    double w = e.path("w").asDouble(1.0);
                    g.addEdge(u, v, w);
                }
            }

            return g;
        }
    }
}
