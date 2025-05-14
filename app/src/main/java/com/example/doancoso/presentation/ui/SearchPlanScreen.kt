package com.example.doancoso.presentation.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancoso.domain.PlanViewModel
import com.example.doancoso.domain.PlanUiState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.doancoso.R
import com.example.doancoso.data.models.PlanResult
import com.example.doancoso.domain.AuthState
import com.example.doancoso.domain.AuthViewModel

@Composable
fun SearchPlanScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    planViewModel: PlanViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val planState by planViewModel.planState.collectAsState()
    val user = (authState as? AuthState.UserLoggedIn)?.user
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Idle || user == null) {
            Log.d("AuthDebug", "Chưa đăng nhập, chuyển về Login")
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    if (authState !is AuthState.UserLoggedIn || user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
            Spacer(Modifier.height(8.dp))
            Text("Đang tải...")
        }
        return
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {  navController.navigate("home") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.homeicon),
                            contentDescription = "Trang chủ"
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp), color = Color.Gray
                    )
                    IconButton(onClick = { navController.navigate("plan") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.planicon),
                            contentDescription = "Kế hoạch"
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp), color = Color.Gray
                    )
                    IconButton(onClick = {
                        navController.navigate("setting")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.setting),
                            contentDescription = "Cài đặt"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = planState) {
                is PlanUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is PlanUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Lỗi: ${state.message}")
                    }
                }

                is PlanUiState.Success -> {
                    val plan = state.plan

                    // Kiểm tra nếu plan là PlanResult (không phải PlanResultDb)
                    if (plan is PlanResult) {
                        LazyColumn(modifier = Modifier.padding(16.dp)) {
                            item {
                                PlanCard(plan)
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        planViewModel.savePlanToFirebase(user.uid, plan) { success, error ->
                                            if (success) {
                                                Toast.makeText(context, "Lưu kế hoạch thành công!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Lỗi: $error", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text("Tạo Plan")
                                }
                            }
                        }
                    } else {
                        // Nếu không phải PlanResult (ví dụ là PlanResultDb), có thể xử lý khác hoặc không làm gì
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Kế hoạch không hợp lệ.")
                        }
                    }
                }

                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Vui lòng tìm kiếm để xem kế hoạch.")
                    }
                }
            }
        }
    }
}

@Composable
fun PlanCard(plan: PlanResult) {
    val itinerary = plan.itinerary ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📍 Điểm đến: ${plan.destination}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "📅 Từ ${itinerary.startDate} đến ${itinerary.endDate}",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            itinerary.itinerary.forEach { dayPlan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🗓️ Ngày ${dayPlan.day} (${dayPlan.date})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        dayPlan.activities?.forEach { activity ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("🕒 ${activity.timeOfDay}", fontWeight = FontWeight.Bold)
                                Text("📍 Địa điểm: ${activity.location}")
                                Text("📖 Hoạt động: ${activity.description}")
                                Text("🚗 Di chuyển: ${activity.transportation}")
                            }

                            Divider(
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .fillMaxWidth(0.9f),
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Phương tiện sử dụng: ${itinerary.transportation?.joinToString(", ") ?: "Không có thông tin"}",
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )

            Text(
                text = "Đặc sản gợi ý: ${itinerary.specialties?.joinToString(", ") ?: "Không có thông tin"}",
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }
    }
}

