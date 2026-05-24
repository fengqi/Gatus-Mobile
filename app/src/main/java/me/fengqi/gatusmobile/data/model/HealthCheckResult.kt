package me.fengqi.gatusmobile.data.model

data class HealthCheckResult(
    val success: Boolean = false,
    val timestamp: String = "",
    val duration: Long = 0,
    val hostname: String? = null,
    val port: String? = null,
    val conditions: List<ConditionResult>? = emptyList(),
    val errors: List<String>? = emptyList()
)

data class ConditionResult(
    val condition: String = "",
    val success: Boolean = false
)
