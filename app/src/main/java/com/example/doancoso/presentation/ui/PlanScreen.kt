package com.example.doancoso.presentation.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.doancoso.R
import com.example.doancoso.data.models.PlanResult
import com.example.doancoso.domain.AuthState
import com.example.doancoso.domain.AuthViewModel
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
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPlanToDelete by remember { mutableStateOf<PlanResult?>(null) }


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
//                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Text("Lỗi: $errorMessage", color = MaterialTheme.colorScheme.error)
//                    }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Bạn chưa có kế hoạch nào.")
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
                            PlanCards(
                                plan = plan,
                                onCardClick = { selectedPlan ->
                                    selectedPlan.uid?.let { planId ->
                                        navController.navigate("planDetail/$planId")
                                    }
                                },
                                onDeleteClick = { planToDelete ->
                                    selectedPlanToDelete = planToDelete
                                    showDeleteDialog = true
                                }
                            )
                        }

                    }
                }
            }
        }
    }
    if (showDeleteDialog && selectedPlanToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa kế hoạch này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedPlanToDelete?.uid?.let { planIdDialog ->
                            user?.uid?.let { uid ->
                                planViewModel.deletePlan(uid, planIdDialog)
                            }
                        }
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
fun PlanCards(
    plan: PlanResult, onCardClick: (PlanResult) -> Unit,
    onDeleteClick: (PlanResult) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCardClick(plan) },
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
            Spacer(modifier = Modifier.height(12.dp))


            Button(
                onClick = { onDeleteClick(plan) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Xóa kế hoạch", color = Color.White)
            }
        }
    }
}

