package me.fengqi.gatusmobile.data.model

data class EndpointStatus(
    val key: String = "",
    val name: String = "",
    val group: String? = null,
    val results: List<HealthCheckResult>? = emptyList(),
    val events: List<EndpointEvent>? = emptyList()
)

data class EndpointEvent(
    val type: String = "",
    val timestamp: String = "",
    val success: Boolean = false
) {
    val fancyText: String
        get() = when (type) {
            "HEALTHY" -> "Endpoint became healthy"
            "UNHEALTHY" -> "Endpoint became unhealthy"
            "START" -> "Monitoring started"
            else -> type
        }
}
