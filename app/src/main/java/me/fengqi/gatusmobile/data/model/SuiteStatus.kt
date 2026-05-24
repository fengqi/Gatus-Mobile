package me.fengqi.gatusmobile.data.model

data class SuiteStatus(
    val key: String = "",
    val name: String = "",
    val group: String? = null,
    val results: List<HealthCheckResult>? = emptyList()
)
