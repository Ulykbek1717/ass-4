[README.md](https://github.com/user-attachments/files/23290610/README.md)
Assignment 4 — SCC, Topological Ordering, DAG Shortest/Longest Paths
1. Purpose
   This project implements the full pipeline required by the “Smart City / Smart Campus Scheduling” assignment:

Find strongly connected components (SCC) with Tarjan.
Build condensation graph (SCC → DAG).
Topologically sort the DAG (Kahn).
Run shortest-path and longest-path (critical path) DP on the DAG.
Log metrics (time, ops) for every dataset under /data/.
All runs are automated by the integration test GraphAlgorithmsIntegrationTest which iterates over all *.json in /data and writes:

data/output.json — structured JSON report per dataset
data/metrics.csv — tabular metrics
This README describes the actual code in the repository, not a template.


2. Project Structure
```text
src/
 ├── main/java/
 │    ├── Main.java
 │    ├── graph/scc/
 │    │     ├── TarjanSCC.java
 │    │     └── CondensationBuilder.java
 │    ├── graph/topo/
 │    │     └── KahnTopologicalSort.java
 │    ├── graph/dagsp/
 │    │     ├── DAGShortestPath.java
 │    │     └── DAGLongestPath.java
 │    ├── graph/util/
 │    │     └── SCCUtils.java
 │    └── metrics/
 │          ├── Metrics.java
 │          └── MetricsTracker.java
 └── test/java/
      └── GraphAlgorithmsIntegrationTest.java

data/
 ├── small1.json
 ├── small2.json
 ├── small3.json
 ├── medium1.json
 ├── medium2.json
 ├── medium3.json
 ├── large1.json
 ├── large2.json
 ├── large3.json
 ├── metrics.csv        ← generated
 └── output.json        ← generated
```




3. Data Summary

| **Dataset**       | **Vertices (n)** | **Edges (m)** | **Cyclic / DAG**                                |
| ----------------- | ---------------- | ------------- | ----------------------------------------------- |
| `dag_small1.json` | 6                | 8             | **DAG**                                         |
| `small1.json`     | 6                | 6             | **Cyclic** (contains 0→1→2→0)                   |
| `small2.json`     | 8                | 7             | **DAG**                                         |
| `small3.json`     | 10               | 10            | **Cyclic** (contains 0→1→2→0 and 3→4→5→3)       |
| `medium1.json`    | 14               | 15            | **Cyclic** (contains 0→1→2→0 and 4→5→6→4)       |
| `medium2.json`    | 16               | 16            | **Cyclic** (contains 1→2→3→1)                   |
| `medium3.json`    | 18               | 18            | **Cyclic** (contains 0→1→2→0, 3→4→5→3, 6→7→6)   |
| `large1.json`     | 30               | 30            | **Cyclic** (contains 0→1→2→0 and 3→4→5→3, etc.) |
| `large2.json`     | 40               | 40            | **Cyclic** (contains 0→1→2→3→4→0 and 5→6→7→5)   |
| `large3.json`     | 50               | 54            | **Cyclic** (contains 0→1→2→3→4→5→0, etc.)       |








| file            | vertices | edges | Tarjan_SCC_count | Tarjan_time_ms | Tarjan_DFS_ops | Kahn_time_ms | Kahn_queue_ops | DAGSP_short_time_ms | DAGSP_short_relax_ops | DAGSP_long_time_ms | DAGSP_long_relax_ops | DAGSP_long_max |
| --------------- | -------- | ----- | ---------------- | -------------- | -------------- | ------------ | -------------- | ------------------- | --------------------- | ------------------ | -------------------- | -------------- |
| dag_small1.json | 6        | 8     | 6                | 0.0499         | 6              | 0.0176       | 12             | 0.0299              | 8                     | 0.0271             | 5                    | 12             |
| large1.json     | 30       | 30    | 24               | 0.0401         | 30             | 0.0565       | 48             | 0.0308              | 21                    | 0.0221             | 21                   | 21             |
| large2.json     | 40       | 40    | 34               | 0.0505         | 40             | 0.0478       | 68             | 0.0215              | 32                    | 0.0218             | 32                   | 32             |
| large3.json     | 50       | 53    | 30               | 0.0540         | 50             | 0.0393       | 60             | 0.0260              | 29                    | 0.0344             | 29                   | 29             |
| medium1.json    | 14       | 15    | 10               | 0.0226         | 14             | 0.0154       | 20             | 0.0077              | 9                     | 0.0071             | 9                    | 8              |
| medium2.json    | 16       | 16    | 14               | 0.0228         | 16             | 0.0200       | 28             | 0.0119              | 13                    | 0.0100             | 13                   | 13             |
| medium3.json    | 18       | 18    | 13               | 0.0245         | 18             | 0.0676       | 26             | 0.0087              | 10                    | 0.0082             | 10                   | 10             |
| small1.json     | 6        | 6     | 4                | 0.0125         | 6              | 0.0081       | 8              | 0.0036              | 3                     | 0.0033             | 3                    | 6              |
| small2.json     | 8        | 7     | 8                | 0.0132         | 8              | 0.0167       | 16             | 0.0067              | 7                     | 0.0127             | 7                    | 4              |
| small3.json     | 10       | 10    | 6                | 0.0182         | 10             | 0.0133       | 12             | 0.0048              | 4                     | 0.0044             | 4                    | 4              |




Choice of Weight Model:
Two datasets dag_small1.json and small1.json use an integer weight model, where each edge has an associated positive weight.


All other datasets are unweighted, meaning all edges are treated as having equal cost (implicitly weight = 1).


5. Analysis

        1. General Trends

The execution times of all algorithms — Tarjan’s SCC, Kahn’s topological sort, and DAG shortest/longest paths —
increase slightly as the number of vertices and edges grows.

However, even for the largest graphs, execution times remain very low (below 0.06 ms).

This shows that the algorithms scale efficiently and perform well on moderately sized datasets.



        2. Strongly Connected Components 

Acyclic graphs, such as dag_small1.json, display the expected one vertex per component structure
(6 SCCs for 6 nodes).

Cyclic graphs (e.g., small1.json, medium1.json, large2.json) contain larger SCCs,
where multiple vertices are grouped together (up to 21 vertices in large3.json).

This confirms that cycle detection and component merging are working correctly.

The number of DFS operations grows proportionally with the number of vertices,
matching the O(V + E) complexity of Kosaraju’s algorithms.

        3. Kahn’s Algorithm

Topological sorting is performed on the condensation graph .

The number of queue operations scales nearly linearly with the number of components
about 8 operations for small graphs and up to 68 operations for large2.json

Execution times remain stable — typically under 0.07 ms,
which demonstrates efficient queue-based processing.



        4. Shortest and Critical Paths in DAGs

For acyclic datasets, shortest and longest path computations produce
results proportional to the depth of the graph.

Example: longest path = 12 in dag_small1.json,
and around 21–32 in larger DAG-like graphs.

The number of relaxation steps corresponds closely to the number of edges
within the DAG, which is expected for single-source shortest-path algorithms.



6. Conclusions
   Run SCC first, then condensation + topo + DAG-SP.
   Condensation drastically reduces DAG size when cycles exist.
   DAG-SP efficiently computes both shortest and longest (critical) paths.
   Metrics confirm linear behavior across datasets.



      Outputs:

  * data/output.json
  * data/metrics.csv
