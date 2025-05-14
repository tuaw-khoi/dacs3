package com.example.doancoso.presentation.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.doancoso.data.models.PlanResultDb
import com.example.doancoso.domain.AuthState
import com.example.doancoso.domain.AuthViewModel
import com.example.doancoso.domain.PlanUiState
import com.example.doancoso.domain.PlanViewModel

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
    val context = LocalContext.current

    LaunchedEffect(planId) {
        Log.d("PlanDetailScreen", "LaunchedEffect triggered with planId: $planId")
        if (authState is AuthState.Idle || user == null) {
            Log.d("PlanDetailScreen", "User not logged in, navigating to login")
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        } else {
            Log.d("PlanDetailScreen", "Fetching plan details for planId: $planId")
            planViewModel.fetchPlanByIdFromFirebase(user.uid, planId)
        }
    }

    when (planState) {
        is PlanUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Đang tải...", style = MaterialTheme.typography.bodyMedium)
            }
        }

        is PlanUiState.Success -> {
            val plan = (planState as PlanUiState.Success).plan
            if (plan is PlanResultDb) {
                if (user != null) {
                    // Truyền thêm ownerUserId (userId cha) vào PlanDetailContent
                    PlanDetailContent(
                        planDb = plan,
                        navController = navController,
                        planId = planId,
                        planViewModel = planViewModel,
                        userId = user.uid,
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Không thể hiển thị kế hoạch",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        is PlanUiState.Error -> {
            val message = (planState as PlanUiState.Error).message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Không có dữ liệu",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(
                            text = message ?: "Không có dữ liệu kế hoạch",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        else -> {}
    }
}

@Composable
fun PlanDetailContent(
    planDb: PlanResultDb,
    navController: NavHostController,
    planId: String,
    planViewModel: PlanViewModel,
    userId: String,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var dayIndexToDelete by remember { mutableStateOf(-1) }
    val context = LocalContext.current
    val canEdit = true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Thông tin tổng quan
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 ${planDb.destination}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (canEdit) {
                            TextButton(onClick = {
                                navController.navigate("editPlan/$planId")
                            }) {
                                Text("Chỉnh sửa", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoRow("📅 Bắt đầu:", planDb.itinerary.startDate)
                    InfoRow("📅 Kết thúc:", planDb.itinerary.endDate)
                    InfoRow("🗓️ Số ngày:", "${planDb.itinerary.itinerary.size} ngày")
                    InfoRow("🍽️ Đặc sản:", planDb.itinerary.specialties.joinToString(", "))
                    InfoRow("🚗 Di chuyển:", planDb.itinerary.transportation.joinToString(", "))
                }
            }

            Text(
                text = "📝 Lịch trình chi tiết",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Danh sách các ngày
        itemsIndexed(planDb.itinerary.itinerary) { index, dayPlan ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📆 Ngày ${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (canEdit) {
                            Row {
                                IconButton(onClick = {
                                    dayIndexToDelete = index
                                    showDeleteDialog = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.Red)
                                }

                                TextButton(onClick = {
                                    navController.navigate("editDay/$planId/$userId/$index")
                                }) {
                                    Text("Chỉnh sửa", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sắp xếp hoạt động theo thời gian trong ngày
                    val sortedActivities = dayPlan.activities?.sortedWith(compareBy {
                        when (it.timeOfDay) {
                            "Buổi Sáng" -> 0
                            "Buổi Chiều" -> 1
                            "Buổi Tối" -> 2
                            else -> 3
                        }
                    })

                    sortedActivities?.forEachIndexed { actIndex, act ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = "• ${act.description}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "📍 ${act.location}",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .clickable {
                                                navController.navigate("destinationDetail/${act.location}")
                                            }
                                    )
                                    Text(
                                        text = "🕒 ${act.timeOfDay}",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                    Text(
                                        text = "🚗 ${act.transportation}",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                if (canEdit) {
                                    IconButton(onClick = {
                                        planViewModel.deleteActivityFromPlan(
                                            index, actIndex, planId, userId
                                        )
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Thêm ngày mới
        item {
            if (canEdit) {
                Button(
                    onClick = {
                        planViewModel.addDayToPlan(userId, planId) { newIndex ->
                            navController.navigate("editDay/$planId/$userId/$newIndex")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("➕ Thêm ngày mới", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    // Dialog xác nhận xóa ngày
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa ngày") },
            text = { Text("Bạn có chắc muốn xóa ngày ${dayIndexToDelete + 1}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        planViewModel.deleteDayFromPlan(dayIndexToDelete, planId, userId, navController)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun InfoRow(icon: String, value: String) {
    Text(
        text = "$icon $value",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.DarkGray,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}









