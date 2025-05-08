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
    // Fetch kế hoạch từ Firebase
    LaunchedEffect(authState) {
        if (authState is AuthState.Idle || user == null) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        } else {
            user.uid.let {
                Log.d("PlanDetailScreen", "User UID: ${user.uid}, Plan ID: $planId")
                planViewModel.fetchPlanByIdFromFirebase(user.uid, planId)
            }
        }
    }


    when (planState) {
        is PlanUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Đang tải...")
            }
        }

        is PlanUiState.Success -> {
            val plan = (planState as PlanUiState.Success).plan
            if (plan is PlanResultDb) {
                if (user != null) {
                    PlanDetailContent(plan, navController, planId, planViewModel, user.uid)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Không thể hiển thị kế hoạch",
                        color = MaterialTheme.colorScheme.error
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
                            textAlign = TextAlign.Center // Đặt text căn giữa
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
    uid: String
) {
    // Tạo một state để hiển thị dialog xác nhận xóa ngày
    var showDeleteDialog by remember { mutableStateOf(false) }
    var dayIndexToDelete by remember { mutableStateOf(-1) }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 Điểm đến: ${planDb.destination}",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        TextButton(onClick = {
                            navController.navigate("editPlan/$planId")
                        }) {
                            Text("Chỉnh sửa", color = MaterialTheme.colorScheme.primary)
                        }
                    }

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

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🍽️ Đặc sản gợi ý: ${planDb.itinerary.specialties.joinToString(", ")}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🚗 Phương tiện di chuyển: ${
                            planDb.itinerary.transportation.joinToString(
                                ", "
                            )
                        }",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ngày ${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Nút xóa ngày
                        IconButton(
                            onClick = {
                                dayIndexToDelete = index
                                showDeleteDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Xóa ngày",
                                tint = Color.Red
                            )
                        }

                        // Nút chỉnh sửa ngày
                        TextButton(onClick = {
                            navController.navigate("editDay/$planId/$uid/$index")
                        }) {
                            Text("Chỉnh sửa ngày", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val sortedActivities = dayPlan.activities
                        ?.sortedWith(compareBy { activity ->
                            when (activity.timeOfDay) {
                                "Buổi Sáng" -> 0
                                "Buổi Chiều" -> 1
                                "Buổi Tối" -> 2
                                else -> 3
                            }
                        })

                    // Danh sách các hoạt động trong ngày
                    sortedActivities?.forEachIndexed { activityIndex, activity ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "• ${activity.description}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                )
                                Text(
                                    text = "📍 ${activity.location}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .padding(start = 8.dp, bottom = 4.dp)
                                        .clickable {
                                            // Điều hướng đến màn hình chi tiết điểm đến
                                            navController.navigate("destinationDetail/${activity.location}")
                                        }
                                )
                                Text(
                                    text = "🕒 ${activity.timeOfDay}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                )
                                Text(
                                    text = "🚗 Phương tiện: ${activity.transportation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                )
                            }

                            // Nút xóa hoạt động
                            IconButton(
                                onClick = {
                                    planViewModel.deleteActivityFromPlan(
                                        index,
                                        activityIndex,
                                        planId,
                                        uid
                                    )
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Xóa hoạt động",
                                    tint = Color.Red
                                )
                            }
                        }
                    }

                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    planViewModel.addDayToPlan(uid, planId) { newDayIndex ->
                        navController.navigate("editDay/$planId/$uid/$newDayIndex")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "Thêm ngày mới",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }

        }


    }

    // Cảnh báo xác nhận xóa ngày
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa ngày") },
            text = { Text("Bạn chắc chắn muốn xóa ngày ${dayIndexToDelete + 1}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d(
                            "PlanDetailContent",
                            "PlanDetailContent: $dayIndexToDelete, $planId, $uid"
                        )
                        planViewModel.deleteDayFromPlan(
                            dayIndexToDelete,
                            planId,
                            uid,
                            navController
                        )

                        showDeleteDialog = false
                    }
                ) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }


}








