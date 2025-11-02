Smart City / Smart Campus Scheduling – Assignment 4

Overview
- Implements SCC (Tarjan), condensation DAG, topological ordering (Kahn), and shortest/longest paths in DAGs.
- Edge-weight model is used for DAG shortest/longest paths (weights on edges). Node durations can be converted to edges if desired.

Project Structure
- `app.App` – CLI entry point
- `graph.core` – Graph model and JSON I/O
- `graph.scc` – Tarjan SCC and condensation
- `graph.topo` – Kahn topological sort
- `graph.dagsp` – DAG shortest/longest paths
- `metrics` – Simple metrics counters
- `data/` – Sample datasets (small/medium/large)
- `src/test/java` – JUnit tests

Build
- Requires Java 17+ and Maven 3.9+
- Build: `mvn -q -DskipTests package`
- Run (examples):
  - `java -cp target/smart-graph-assign4-1.0-SNAPSHOT.jar app.App scc data/small1.json`
  - `java -cp target/smart-graph-assign4-1.0-SNAPSHOT.jar app.App topo data/small1.json`
  - `java -cp target/smart-graph-assign4-1.0-SNAPSHOT.jar app.App dagsp-shortest data/dag_small1.json 0`
  - `java -cp target/smart-graph-assign4-1.0-SNAPSHOT.jar app.App dagsp-longest data/dag_small1.json`

JSON Format
```
{
  "directed": true,
  "n": 6,
  "edges": [
    {"u":0, "v":1, "w":2.5},
    {"u":1, "v":2},               // default weight = 1.0
    {"from":2, "to":3, "w":0.5}  // alternative keys supported
  ]
}
```

Datasets
- 9 datasets under `data/` with a mix of cyclic and acyclic graphs across sizes. See file headers for notes.

Instrumentation
- Counters:
  - SCC: `scc.dfs.visits`, `scc.dfs.edges`
  - Topo (Kahn): `topo.pushes`, `topo.pops`, `topo.edges`
  - DAG SP: `dagsp.relax` (shortest), `daglp.relax` (longest)
- Timing: measure wall-clock around operations with `System.nanoTime()` (examples in `app.App`).

Testing
- Run tests: `mvn -q test`
- Tests cover small deterministic SCC and DAG shortest/longest cases.

Notes
- Longest path assumes a DAG and non-negative edge weights. For general DAG with arbitrary weights, the DP still works; for graphs with cycles, longest is undefined.

