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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.doancoso.data.models.DayPlanDb
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Lỗi: $message", color = MaterialTheme.colorScheme.error)
            }
        }

        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailContent(
    planDb: PlanResultDb,
    navController: NavHostController,
    planId: String,
    planViewModel: PlanViewModel,
    uid: String
) {  // Thêm navController vào đây
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableStateOf(-1) }
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

                        // Nút chỉnh sửa từng ngày
                        TextButton(onClick = {
                            selectedDayIndex = index
                            showSheet = true
                        }) {
                            Text("Chỉnh sửa ngày", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Danh sách các hoạt động trong ngày
                    dayPlan.activities?.forEachIndexed { activityIndex, activity ->
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
                                    planViewModel.deleteActivityFromPlan(index, activityIndex, planId, uid)
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

    }

    if (showSheet && selectedDayIndex >= 0 && selectedDayIndex < planDb.itinerary.itinerary.size) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = bottomSheetState
        ) {
            EditDayBottomSheet(
                dayIndex = selectedDayIndex,
                dayPlan = planDb.itinerary.itinerary[selectedDayIndex],
                onClose = { showSheet = false },
                planViewModel = planViewModel,
                planId = planId,
                uid = uid
            )
        }
    }
}

@Composable
fun EditDayBottomSheet(
    dayIndex: Int,
    dayPlan: DayPlanDb,
    onClose: () -> Unit,
    planViewModel: PlanViewModel,
    planId: String,
    uid: String
) {
    // State để lưu các mô tả đã cập nhật cho từng activity
    val updatedDescriptions = remember {
        mutableStateOf(dayPlan.activities?.map { it.description } ?: emptyList())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Chỉnh sửa Ngày ${dayIndex + 1}", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Duyệt qua tất cả activities và cho phép chỉnh sửa mô tả của từng activity
        dayPlan.activities?.forEachIndexed { index, activity ->
            OutlinedTextField(
                value = updatedDescriptions.value.getOrElse(index) { activity.description },
                onValueChange = { updatedDescription ->
                    updatedDescriptions.value = updatedDescriptions.value.toMutableList().apply {
                        this[index] = updatedDescription
                    }
                },
                label = { Text("Mô tả hoạt động ${index + 1}") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val updatedActivities = dayPlan.activities.mapIndexed { index, activity ->
                    activity.copy(description = updatedDescriptions.value.getOrElse(index) { activity.description })
                }

                val updatedDayPlan = dayPlan.copy(activities = updatedActivities)

                Log.d("EditDayBottomSheet", "Updated DayPlan: $updatedActivities")
                Log.d("EditDayBottomSheet", "Updated DayPlan: $updatedDayPlan")

                // Gọi lại fetch dữ liệu sau khi lưu
                planViewModel.updateDayPlan(
                    uid = uid,
                    planId = planId,
                    dayIndex = dayIndex,
                    updatedDayPlan = updatedDayPlan
                )

                planViewModel.fetchPlanByIdFromFirebase(uid, planId)

                onClose()
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Lưu")
        }


    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlanScreen(
    navController: NavHostController,
    planId: String,
    planViewModel: PlanViewModel,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val planState by planViewModel.planState.collectAsState()
    val user = (authState as? AuthState.UserLoggedIn)?.user

    var destination by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    LaunchedEffect(planId) {
        if (user != null) {
            planViewModel.fetchPlanByIdFromFirebase(user.uid, planId)
        }
    }

    when (planState) {
        is PlanUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Đang tải...")
            }
        }

        is PlanUiState.Success -> {
            val plan = (planState as PlanUiState.Success).plan
            if (plan is PlanResultDb) {
                if (destination.isEmpty()) {
                    destination = plan.destination
                    startDate = plan.itinerary.startDate
                    endDate = plan.itinerary.endDate
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Chỉnh sửa kế hoạch") },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Quay lại"
                                    )
                                }
                            }
                        )
                    },
                    content = { paddingValues ->
                        Column(
                            modifier = Modifier
                                .padding(paddingValues)
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "Thông tin kế hoạch",
                                        style = MaterialTheme.typography.titleLarge
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = destination,
                                        onValueChange = { destination = it },
                                        label = { Text("Điểm đến") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(Icons.Default.Place, contentDescription = null)
                                        }
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = startDate,
                                        onValueChange = { startDate = it },
                                        label = { Text("Ngày bắt đầu") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(Icons.Default.DateRange, contentDescription = null)
                                        }
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = endDate,
                                        onValueChange = { endDate = it },
                                        label = { Text("Ngày kết thúc") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(Icons.Default.DateRange, contentDescription = null)
                                        }
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val updatedPlan = plan.copy(
                                        destination = destination,
                                        itinerary = plan.itinerary.copy(
                                            startDate = startDate,
                                            endDate = endDate
                                        )
                                    )
                                    if (user != null) {
                                        planViewModel.updatePlanToFirebase(user.uid,
                                            updatedPlan,
                                            planId,
                                            onSuccess = {
                                                navController.popBackStack()
                                                planViewModel.fetchPlanByIdFromFirebase(
                                                    user.uid,
                                                    planId
                                                )
                                            })
//                                        navController.popBackStack()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Lưu thay đổi", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                )
            }
        }

        is PlanUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Có lỗi xảy ra. Vui lòng thử lại.", color = Color.Red)
            }
        }

        else -> {}
    }
}


