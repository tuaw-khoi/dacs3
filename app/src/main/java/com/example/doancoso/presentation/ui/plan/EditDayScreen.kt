package com.example.doancoso.presentation.ui.plan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.doancoso.data.models.ActivityDetailDb
import com.example.doancoso.data.models.DayPlanDb
import com.example.doancoso.data.models.PlanResultDb
import com.example.doancoso.domain.PlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDayScreen(
    dayIndex: Int,
    planId: String,
    uid: String,
    planViewModel: PlanViewModel,
    navController: NavController,
    plan: PlanResultDb
) {
    // Kiểm tra xem có tồn tại ngày nào ở vị trí dayIndex không
    val dayPlan = plan.itinerary.itinerary.getOrNull(dayIndex)

    // Nếu không có ngày ở vị trí đó, tức là đang thêm ngày mới
    if (dayPlan == null) {
        // Hiển thị giao diện cho ngày mới
        val orderedTimes = listOf("Buổi Sáng", "Buổi Chiều", "Buổi Tối")
        var activities by remember {
            mutableStateOf(
                listOf(
                    ActivityDetailDb(
                        description = "",
                        location = "",
                        timeOfDay = orderedTimes[0],
                        transportation = ""
                    )
                )
            )
        }

        var showAlertDialog by remember { mutableStateOf(false) }

        val canAddActivity = activities.size < 3

        // Hàm kiểm tra các trường có bị thiếu không
        fun hasIncompleteFields(): Boolean {
            return activities.any {
                it.description.isBlank() || it.location.isBlank() || it.timeOfDay.isBlank() || it.transportation.isBlank()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Thêm ngày mới") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Thêm các activity cho ngày mới
                    activities.forEachIndexed { index, activity ->
                        Text(
                            text = "Hoạt động ${index + 1}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = activity.description,
                            onValueChange = { newDescription ->
                                activities = activities.toMutableList().apply {
                                    this[index] = activity.copy(description = newDescription)
                                }
                            },
                            label = { Text("Mô tả") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = false,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )

                        OutlinedTextField(
                            value = activity.location,
                            onValueChange = { newLocation ->
                                activities = activities.toMutableList().apply {
                                    this[index] = activity.copy(location = newLocation)
                                }
                            },
                            label = { Text("Địa điểm") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )

                        OutlinedTextField(
                            value = activity.timeOfDay,
                            onValueChange = { newTimeOfDay ->
                                activities = activities.toMutableList().apply {
                                    this[index] = activity.copy(timeOfDay = newTimeOfDay)
                                }
                            },
                            label = { Text("Thời gian trong ngày") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )

                        OutlinedTextField(
                            value = activity.transportation,
                            onValueChange = { newTransportation ->
                                activities = activities.toMutableList().apply {
                                    this[index] = activity.copy(transportation = newTransportation)
                                }
                            },
                            label = { Text("Phương tiện") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )
                    }

                    if (canAddActivity) {
                        Button(
                            onClick = {
                                val usedTimes = activities.map { it.timeOfDay }
                                val nextTime =
                                    orderedTimes.firstOrNull { it !in usedTimes } ?: "Buổi Sáng"

                                activities = (activities + ActivityDetailDb(
                                    description = "",
                                    location = "",
                                    timeOfDay = nextTime,
                                    transportation = ""
                                )).sortedBy { orderedTimes.indexOf(it.timeOfDay) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                text = "Thêm hoạt động",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (hasIncompleteFields()) {
                                showAlertDialog = true
                            } else {
                                val sortedActivities =
                                    activities.sortedBy { orderedTimes.indexOf(it.timeOfDay) }
                                val updatedDayPlan = DayPlanDb(activities = sortedActivities)

                                planViewModel.updateDayPlan(uid, planId, dayIndex, updatedDayPlan)
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Lưu thay đổi",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }

                    // AlertDialog to notify user
                    if (showAlertDialog) {
                        AlertDialog(
                            onDismissRequest = { showAlertDialog = false },
                            title = { Text("Thông báo") },
                            text = { Text("Bạn chưa điền đầy đủ thông tin cho tất cả các hoạt động. Bạn có muốn tiếp tục lưu lại những hoạt động đã đủ thông tin?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        // Lưu lại các hoạt động đã đủ thông tin
                                        val validActivities = activities.filter {
                                            it.description.isNotBlank() && it.location.isNotBlank() &&
                                                    it.timeOfDay.isNotBlank() && it.transportation.isNotBlank()
                                        }
                                        val updatedDayPlan = DayPlanDb(activities = validActivities)

                                        planViewModel.updateDayPlan(
                                            uid,
                                            planId,
                                            dayIndex,
                                            updatedDayPlan
                                        )
                                        navController.popBackStack()
                                        showAlertDialog = false
                                    }
                                ) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = { showAlertDialog = false }
                                ) {
                                    Text("Hủy")
                                }
                            }
                        )
                    }
                }
            }
        )
    } else {
        // Trường hợp có ngày kế hoạch đã tồn tại
        val orderedTimes = listOf("Buổi Sáng", "Buổi Chiều", "Buổi Tối")
        var activities by remember {
            mutableStateOf(
                dayPlan.activities.sortedBy { orderedTimes.indexOf(it.timeOfDay) }
            )
        }

        var showAlertDialog by remember { mutableStateOf(false) }

        val canAddActivity = activities.size < 3

        // Hàm kiểm tra các trường có bị thiếu không
        fun hasIncompleteFields(): Boolean {
            return activities.any {
                it.description.isBlank() || it.location.isBlank() || it.timeOfDay.isBlank() || it.transportation.isBlank()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chỉnh sửa ngày ${dayIndex + 1}") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    activities.forEachIndexed { index, activity ->
                        Text(
                            text = "Hoạt động ${index + 1}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = activity.description,
                            onValueChange = { newDescription ->
                                activities = activities.toMutableList().apply {
                                    this[index] = activity.copy(description = newDescription)
                                }
                            },
                            label = { Text("Mô tả") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = false,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )
                        OutlinedTextField(
                            value = activity.location,
                            onValueChange = { newLocation ->
                                activities = activities.toMutableList().apply {
                                    this[index] = activity.copy(location = newLocation)
                                }
                            },
                            label = { Text("Địa điểm") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )

                        OutlinedTextField(
                            value = activity.timeOfDay,
                            onValueChange = { newTimeOfDay ->
                                activities = activities.toMutableList().apply {
                                    this[index] = activity.copy(timeOfDay = newTimeOfDay)
                                }
                            },
                            label = { Text("Thời gian trong ngày") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )

                        OutlinedTextField(
                            value = activity.transportation,
                            onValueChange = { newTransportation ->
                                activities = activities.toMutableList().apply {
                                    this[index] = activity.copy(transportation = newTransportation)
                                }
                            },
                            label = { Text("Phương tiện") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )
                    }

                    if (canAddActivity) {
                        Button(
                            onClick = {
                                val usedTimes = activities.map { it.timeOfDay }
                                val nextTime =
                                    orderedTimes.firstOrNull { it !in usedTimes } ?: "Buổi Sáng"

                                activities = (activities + ActivityDetailDb(
                                    description = "",
                                    location = "",
                                    timeOfDay = nextTime,
                                    transportation = ""
                                )).sortedBy { orderedTimes.indexOf(it.timeOfDay) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                text = "Thêm hoạt động",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (hasIncompleteFields()) {
                                showAlertDialog = true
                            } else {
                                val sortedActivities =
                                    activities.sortedBy { orderedTimes.indexOf(it.timeOfDay) }
                                val updatedDayPlan = DayPlanDb(activities = sortedActivities)

                                planViewModel.updateDayPlan(uid, planId, dayIndex, updatedDayPlan)
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Lưu thay đổi",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }

                    // AlertDialog to notify user
                    if (showAlertDialog) {
                        AlertDialog(
                            onDismissRequest = { showAlertDialog = false },
                            title = { Text("Thông báo") },
                            text = { Text("Bạn chưa điền đầy đủ thông tin cho tất cả các hoạt động. Bạn có muốn tiếp tục lưu lại những hoạt động đã đủ thông tin?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        // Lưu lại các hoạt động đã đủ thông tin
                                        val validActivities = activities.filter {
                                            it.description.isNotBlank() && it.location.isNotBlank() &&
                                                    it.timeOfDay.isNotBlank() && it.transportation.isNotBlank()
                                        }
                                        val updatedDayPlan = DayPlanDb(activities = validActivities)
                                        planViewModel.updateDayPlan(
                                            uid,
                                            planId,
                                            dayIndex,
                                            updatedDayPlan
                                        )
                                        navController.popBackStack()
                                        showAlertDialog = false
                                    }
                                ) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = { showAlertDialog = false }
                                ) {
                                    Text("Hủy")
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}