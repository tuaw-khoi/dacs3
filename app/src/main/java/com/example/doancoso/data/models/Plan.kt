package com.example.doancoso.data.models

import com.example.doancoso.domain.BasePlan

data class PlanResult(
    var uid: String? = null,
    val destination: String = "",
    val itinerary: Itinerary = Itinerary(),
    val photos: List<String> = emptyList()
) : BasePlan

data class Itinerary(
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val itinerary: List<DayPlan> = emptyList(),
    val transportation: List<String>? = null,
    val specialties: List<String>? = null
)

data class DayPlan(
    val day: Int = 0,
    val date: String = "",
    val activities: List<Activity>? = null
)

data class Activity(
    val timeOfDay: String = "",
    val description: String = "",
    val location: String = "",
    val transportation: String = ""
)
