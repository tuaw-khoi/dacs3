package com.example.doancoso.presentation.ui

import android.util.Log
import android.widget.Toast
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
fun PlanDetailOwnerScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    planId: String,
    planViewModel: PlanViewModel,
) {
    val planState by planViewModel.planState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.UserLoggedIn)?.user
    val context = LocalContext.current

    val isFromDeepLink = authViewModel.pendingDeepLinkUid != null && authViewModel.pendingDeepLinkPlanId != null
    var currentUid = authViewModel.pendingDeepLinkUid

    Log.d("PlanDetailScreen", ": $currentUid")
    // Fetch kế hoạch từ Firebase
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
            planViewModel.fetchPlanByIdFromFirebase(currentUid.toString(), planId)
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
                    // Truyền thêm ownerUserId (userId cha) vào PlanDetailContent
                    PlanDetailOwnerContent(
                        planDb = plan,
                        navController = navController,
                        planId = planId,
                        planViewModel = planViewModel,
                        owner = currentUid.toString(),
                        userId = user.uid,
                        isFromDeepLink = isFromDeepLink
                    )

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
fun PlanDetailOwnerContent(
    planDb: PlanResultDb,
    navController: NavHostController,
    planId: String,
    planViewModel: PlanViewModel,
    owner: String,
    userId:String,
    isFromDeepLink: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var dayIndexToDelete by remember { mutableStateOf(-1) }
    var showJoinDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Sử dụng state để lưu quyền chỉnh sửa
    var canEdit by remember { mutableStateOf(false) }

    // Kiểm tra quyền khi planId hoặc currentUid thay đổi
    LaunchedEffect(planId, userId, isFromDeepLink) {
        canEdit = (owner == userId) && !isFromDeepLink

    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
//            if (!canEdit) {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 16.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
//                    elevation = CardDefaults.cardElevation(4.dp)
//                ) {
//                    Column(Modifier.padding(16.dp)) {
//                        Text(
//                            text = "📩 Bạn chỉ có thể xem kế hoạch này. Muốn trở thành chủ sở hữu?",
//                            color = Color(0xFFEF6C00),
//                            style = MaterialTheme.typography.bodyMedium,
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Button(onClick = { showJoinDialog = true }) {
//                            Text("Tham gia làm chủ sở hữu")
//                        }
//                    }
//                }
//            }

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

                        // Nếu user không phải người mời, hiển thị nút chỉnh sửa
                        if (canEdit) {
                            TextButton(onClick = {
                                navController.navigate("editPlan/$planId")
                            }) {
                                Text("Chỉnh sửa", color = MaterialTheme.colorScheme.primary)
                            }
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
                            planDb.itinerary.transportation.joinToString(", ")
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

                        // Nếu user không phải người mời, hiển thị nút xóa ngày và chỉnh sửa
                        if (canEdit) {
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
                                navController.navigate("editDay/$planId/$owner/$index")
                            }) {
                                Text("Chỉnh sửa ngày", color = MaterialTheme.colorScheme.primary)
                            }
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
                            if (canEdit) { IconButton(
                                onClick = {
                                    planViewModel.deleteActivityFromPlan(
                                        index,
                                        activityIndex,
                                        planId,
                                        owner
                                    )
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Xóa hoạt động",
                                    tint = Color.Red
                                )
                            }}

                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            if (canEdit) {
                Button(
                    onClick = {
                        planViewModel.addDayToPlan(owner, planId) { newDayIndex ->
                            navController.navigate("editDay/$planId/$owner/$newDayIndex")
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
                        planViewModel.deleteDayFromPlan(
                            dayIndexToDelete,
                            planId,
                            owner,
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

    // Dialog xác nhận tham gia làm chủ sở hữu
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Tham gia làm chủ sở hữu") },
            text = { Text("Bạn muốn trở thành chủ sở hữu kế hoạch này?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        planViewModel.addOwnerToPlan(
                            planId = planId,
                            currentUid = owner,
                            ownerUid = userId,
                            onSuccess = {
                                showJoinDialog = false
                                Toast.makeText(context, "Đã thêm bạn làm chủ sở hữu!", Toast.LENGTH_SHORT).show()
                            },
                            onError = {
                                showJoinDialog = false
                                Toast.makeText(context, "Có lỗi: $it", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                ) { Text("Đồng ý") }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) { Text("Hủy") }
            }
        )
    }
}








