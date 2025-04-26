package com.example.doancoso.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.doancoso.domain.PlanViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancoso.data.models.PlanResultDb
import com.example.doancoso.domain.PlanUiState

@Composable
fun PlanDetailScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    planId: String,
    planViewModel: PlanViewModel,
) {
    val planState by planViewModel.planState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    val user = (authState as? AuthState.UserLoggedIn)?.user
    // Fetch kế hoạch từ Firebase
    LaunchedEffect(authState) {
        if (authState is AuthState.Idle || user == null) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        } else {
            user.uid.let {
                planViewModel.fetchPlanByIdFromFirebase(user.uid, planId)
            }
        }
    }

    when (planState) {
        is PlanUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is PlanUiState.Success -> {
            val plan = (planState as PlanUiState.Success).plan
            if (plan is PlanResultDb) {
                PlanDetailContent(plan, navController) // Truyền navController vào đây
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Không thể hiển thị kế hoạch", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        is PlanUiState.Error -> {
            val message = (planState as PlanUiState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Lỗi: $message", color = MaterialTheme.colorScheme.error)
            }
        }
        else -> {}
    }
}

@Composable
fun PlanDetailContent(planDb: PlanResultDb, navController: NavHostController) {  // Thêm navController vào đây
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            // Thông tin chung của kế hoạch
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📍 Điểm đến: ${planDb.destination}",
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📅 Bắt đầu: ${planDb.itinerary.startDate}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "📅 Kết thúc: ${planDb.itinerary.endDate}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🗓️ Tổng số ngày: ${planDb.itinerary.itinerary.size} ngày",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    // Hiển thị các món đặc sản
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🍽️ Đặc sản gợi ý: ${planDb.itinerary.specialties.joinToString(", ")}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    // Hiển thị các phương tiện di chuyển
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🚗 Phương tiện di chuyển: ${planDb.itinerary.transportation.joinToString(", ")}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "📝 Chi tiết lịch trình: ",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Danh sách lịch trình theo từng ngày
        itemsIndexed(planDb.itinerary.itinerary) { index, dayPlan ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ngày ${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Danh sách các hoạt động trong ngày
                    dayPlan.activities?.forEach { activity ->
                        Text(
                            text = "• ${activity.description}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                        Text(
                            text = "  📍 ${activity.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp).clickable {
                                // Điều hướng đến màn hình chi tiết điểm đến
                                navController.navigate("destinationDetail/${activity.location}")
                            }
                        )
                        Text(
                            text = "  🕒 ${activity.timeOfDay}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                        Text(
                            text = "  🚗 Phương tiện: ${activity.transportation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
