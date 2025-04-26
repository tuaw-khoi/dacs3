package com.example.doancoso.data.remote

import com.example.doancoso.data.models.DestinationDetails
import com.example.doancoso.data.models.PlanResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlanService {
    @GET("travel/plan")
    suspend fun getPlans(
        @Query("destination") destination: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<PlanResult>

    @GET("travel/location/details")
    suspend fun getLocationDetails(
        @Query("destination") destination: String,
    ): Response<DestinationDetails>
}
