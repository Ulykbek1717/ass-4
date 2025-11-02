// java
package metrics;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class SimpleMetrics implements Metrics {
    private final EnumMap<Counter, AtomicLong> counters = new EnumMap<>(Counter.class);
    private final AtomicLong totalTimeNs = new AtomicLong();

    public SimpleMetrics() {
        for (Counter c : Counter.values()) {
            counters.put(c, new AtomicLong(0));
        }
    }

    @Override
    public long markStart() {
        return System.nanoTime();
    }

    @Override
    public void addTimeSince(long startNs) {
        long delta = System.nanoTime() - startNs;
        if (delta > 0) totalTimeNs.addAndGet(delta);
    }

    @Override
    public void inc(Counter c) {
        counters.get(c).incrementAndGet();
    }

    @Override
    public void incBy(Counter c, long delta) {
        counters.get(c).addAndGet(delta);
    }

    @Override
    public long get(Counter c) {
        return counters.get(c).get();
    }

    @Override
    public long totalTimeNs() {
        return totalTimeNs.get();
    }

    @Override
    public Map<String, Long> snapshot() {
        Map<String, Long> map = counters.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().get()));
        map.put("TIME_NS", totalTimeNs.get());
        return map;
    }

    @Override
    public String toString() {
        return snapshot().toString();
    }
}
