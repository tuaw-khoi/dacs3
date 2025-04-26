package com.example.doancoso.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.doancoso.R
import com.example.doancoso.data.models.PlanResult
import com.example.doancoso.domain.PlanUiState
import com.example.doancoso.domain.PlanViewModel

@Composable
fun PlanScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    planViewModel: PlanViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    // Lắng nghe sự thay đổi trong dữ liệu kế hoạch
    val planState by planViewModel.planState.collectAsState()
    val user = (authState as? AuthState.UserLoggedIn)?.user
    val context = LocalContext.current

    // State variables to manage UI
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var plans by remember { mutableStateOf<List<PlanResult>>(emptyList()) }

    // Kiểm tra khi authState thay đổi và thực hiện điều hướng hoặc lấy kế hoạch
    LaunchedEffect(authState) {
        if (authState is AuthState.Idle || user == null) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        } else {
            user?.uid?.let {
                planViewModel.fetchPlansFromFirebase(it)
            }
        }
    }


    when (planState) {
        is PlanUiState.Loading -> {
            isLoading = true
        }
        is PlanUiState.Error -> {
            isLoading = false
            errorMessage = (planState as PlanUiState.Error).message
        }
        is PlanUiState.SuccessMultiple -> {
            isLoading = false
            plans = (planState as PlanUiState.SuccessMultiple).plans
        }
        else -> Unit
    }

    // Scaffold layout with bottom bar and content area
    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigate("home") }) {
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
                    IconButton(onClick = { navController.navigate(Screen.Plan.route) }) {
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
                    IconButton(onClick = { navController.navigate("setting") }) {
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
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Lỗi: $errorMessage", color = MaterialTheme.colorScheme.error)
                    }
                }
                plans.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Bạn chưa có kế hoạch nào.")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(plans) { plan ->
                            PlanCards(plan) { selectedPlan ->
                                // Điều hướng đến màn hình chi tiết kế hoạch
                                selectedPlan.uid?.let { planId ->
                                    navController.navigate("planDetail/$planId")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanCards(plan: PlanResult, onCardClick: (PlanResult) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCardClick(plan) },  // click Card thì gọi callback
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📍 Điểm đến: ${plan.destination}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📅 Bắt đầu: ${plan.itinerary.startDate}",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Text(
                text = "📅 Kết thúc: ${plan.itinerary.endDate}",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "🗓️ Số ngày lịch trình: ${plan.itinerary?.itinerary?.size ?: 0} ngày",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}
