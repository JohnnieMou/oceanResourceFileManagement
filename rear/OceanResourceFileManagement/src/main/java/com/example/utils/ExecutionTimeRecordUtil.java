package com.example.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExecutionTimeRecordUtil {
    private final Map<String, Long> startTimes = new HashMap<>();
    private final Map<String, Long> durations = new HashMap<>();

    public void start(String phase) {
        startTimes.put(phase, System.currentTimeMillis());
    }

    public void stop(String phase) {
        long endTime = System.currentTimeMillis();
        long startTime = startTimes.getOrDefault(phase, endTime);
        long duration = endTime - startTime;
        durations.merge(phase, duration, Long::sum);
    }

    // 获取格式化的时间
    public String getFormattedTime(String phase) {
        return UnitConversionUtil.formatExecutionTime(durations.getOrDefault(phase, 0L));
    }

    // 获取执行总时间
    public String getTotalFormattedTime() {
        long total = durations.values().stream().mapToLong(Long::longValue).sum();
        return UnitConversionUtil.formatExecutionTime(total);
    }

    // 获取详细的时间记录
    public String getDetailedTimings() {
        return durations.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + getFormattedTime(entry.getKey()))
                .collect(Collectors.joining("\n"));
    }
}
