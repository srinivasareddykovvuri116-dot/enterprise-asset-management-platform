package com.enterprise.assetmanagement.analytics;

import java.util.Map;

public record AnalyticsResponse(
        long totalTasks,
        long completedTasks,
        Map<String, Long> tasksByStatus,
        Map<String, Long> tasksByPriority
) {
}