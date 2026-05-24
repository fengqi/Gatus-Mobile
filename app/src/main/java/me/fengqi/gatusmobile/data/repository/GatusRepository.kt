package me.fengqi.gatusmobile.data.repository

import me.fengqi.gatusmobile.data.api.GatusApiService
import me.fengqi.gatusmobile.data.api.RetrofitClient
import me.fengqi.gatusmobile.data.model.AppConfig
import me.fengqi.gatusmobile.data.model.EndpointStatus
import me.fengqi.gatusmobile.data.model.HealthCheckResult
import me.fengqi.gatusmobile.data.model.SuiteStatus

class GatusRepository(private val baseUrl: String) {

    private val api: GatusApiService get() = RetrofitClient.getApiService(baseUrl)

    suspend fun getConfig(): Result<AppConfig> = runCatching {
        api.getConfig()
    }

    suspend fun getEndpointStatuses(page: Int = 1, pageSize: Int = 50): Result<List<EndpointStatus>> = runCatching {
        api.getEndpointStatuses(page, pageSize)
    }

    suspend fun getEndpointDetail(key: String, page: Int = 1, pageSize: Int = 20): Result<EndpointStatus> = runCatching {
        api.getEndpointDetail(key, page, pageSize)
    }

    suspend fun getSuiteStatuses(page: Int = 1, pageSize: Int = 50): Result<List<SuiteStatus>> = runCatching {
        api.getSuiteStatuses(page, pageSize)
    }
}
