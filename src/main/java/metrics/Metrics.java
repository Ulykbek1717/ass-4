// java
package metrics;

import java.util.Map;

public interface Metrics {
    enum Counter {
        SCC_DFS_VISITS,
        SCC_DFS_EDGES,
        KAHN_PUSHES,
        KAHN_POPS,
        KAHN_EDGES,
        DAG_RELAXATIONS
    }

    long markStart();
    void addTimeSince(long startNs);
    void inc(Counter c);
    void incBy(Counter c, long delta);
    long get(Counter c);
    long totalTimeNs();
    Map<String, Long> snapshot();
    String toString();
}
