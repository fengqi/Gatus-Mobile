package me.fengqi.gatusmobile.data.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val isoFormats = listOf(
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
).also { it.forEach { fmt -> fmt.timeZone = TimeZone.getTimeZone("UTC") } }

private val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).also {
    it.timeZone = TimeZone.getDefault()
}

private fun parseIsoTimestamp(timestamp: String): java.util.Date? {
    for (fmt in isoFormats) {
        try {
            return fmt.parse(timestamp)
        } catch (_: Exception) {}
    }
    return null
}

fun formatTimeAgo(timestamp: String?): String {
    if (timestamp == null) return ""
    val date = parseIsoTimestamp(timestamp) ?: return ""
    val diffMs = System.currentTimeMillis() - date.time
    return when {
        diffMs < 500 -> "now"
        diffMs > 3 * 86400000 -> "${diffMs / 86400000}d ago"
        diffMs > 3600000 -> "${diffMs / 3600000}h ago"
        diffMs > 60000 -> "${diffMs / 60000}m ago"
        else -> "${diffMs / 1000}s ago"
    }
}

fun formatTimestamp(timestamp: String): String {
    val date = parseIsoTimestamp(timestamp) ?: return timestamp
    return outputFormat.format(date)
}

fun calculateAverageResponseTime(results: List<me.fengqi.gatusmobile.data.model.HealthCheckResult>): String {
    val valid = results.filter { it.duration > 0 }
    if (valid.isEmpty()) return "N/A"
    val avg = valid.sumOf { it.duration / 1_000_000 } / valid.size
    return "${avg}ms"
}

fun calculateResponseTimeRange(results: List<me.fengqi.gatusmobile.data.model.HealthCheckResult>): String {
    val valid = results.filter { it.duration > 0 }
    if (valid.isEmpty()) return "N/A"
    val min = valid.minOf { it.duration / 1_000_000 }
    val max = valid.maxOf { it.duration / 1_000_000 }
    return if (min == max) "${min}ms" else "$min-${max}ms"
}

fun formatResponseTimeSummary(results: List<me.fengqi.gatusmobile.data.model.HealthCheckResult>): String? {
    val valid = results.filter { it.duration > 0 }
    if (valid.isEmpty()) return null
    val avg = valid.sumOf { it.duration / 1_000_000 } / valid.size
    return "~${avg}ms"
}
