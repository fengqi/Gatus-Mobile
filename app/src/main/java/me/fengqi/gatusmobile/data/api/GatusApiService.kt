package me.fengqi.gatusmobile.data.api

import me.fengqi.gatusmobile.data.model.AppConfig
import me.fengqi.gatusmobile.data.model.EndpointStatus
import me.fengqi.gatusmobile.data.model.HealthCheckResult
import me.fengqi.gatusmobile.data.model.SuiteStatus
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GatusApiService {
    @GET("/api/v1/config")
    suspend fun getConfig(): AppConfig

    @GET("/api/v1/endpoints/statuses")
    suspend fun getEndpointStatuses(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): List<EndpointStatus>

    @GET("/api/v1/endpoints/{key}/statuses")
    suspend fun getEndpointDetail(
        @Path("key") key: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): EndpointStatus

    @GET("/api/v1/suites/statuses")
    suspend fun getSuiteStatuses(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): List<SuiteStatus>
}
