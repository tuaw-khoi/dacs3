package com.example.doancoso.data.models

import com.example.doancoso.domain.BasePlan

data class PlanResultDb(
    var uid: String? = null,
    val destination: String = "", // "ho chi minh"
    val itinerary: ItineraryDb = ItineraryDb(),
) : BasePlan

data class ItineraryDb(
    val destination: String = "", // "Thành phố Hồ Chí Minh, Việt Nam"
    var startDate: String = "", // "22/4/2025"
    var endDate: String = "", // "24/4/2025"
    var itinerary: List<DayPlanDb> = emptyList(), // List các ngày
    val specialties: List<String> = emptyList(), // Các đặc sản
    val transportation: List<String> = emptyList() // Các phương tiện di chuyển
)

data class DayPlanDb(
    var activities: List<ActivityDetailDb> = emptyList()
)

data class ActivityDetailDb(
    val description: String = "",
    val location: String = "",
    val timeOfDay: String = "",
    val transportation: String = ""
)
