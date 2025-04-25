package com.askel.coursesplatform.service.impl;

import com.askel.coursesplatform.service.CounterService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class CounterServiceImpl implements CounterService {

    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override
    public void increment(String uri) {
        counters.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
    }

    @Override
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        counters.forEach((uri, counter) -> stats.put(uri, counter.get()));
        return stats;
    }
}
