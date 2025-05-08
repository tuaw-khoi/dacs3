package com.example.doancoso.presentation.ui.plan

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.doancoso.R
import com.example.doancoso.data.models.PlanResultDb
import com.example.doancoso.domain.AuthState
import com.example.doancoso.domain.AuthViewModel
import com.example.doancoso.domain.PlanUiState
import com.example.doancoso.domain.PlanViewModel

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

    var specialties by remember { mutableStateOf("") }
    var transportation by remember { mutableStateOf("") }

    LaunchedEffect(planId) {
        user?.let {
            planViewModel.fetchPlanByIdFromFirebase(it.uid, planId)
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
                if (specialties.isEmpty()) {
                    specialties = plan.itinerary.specialties?.joinToString(", ") ?: ""
                    transportation = plan.itinerary.transportation?.joinToString(", ") ?: ""
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
                                        value = specialties,
                                        onValueChange = { specialties = it },
                                        label = { Text("Món đặc sản") },
                                        modifier = Modifier.fillMaxWidth(),
                                        leadingIcon = {
                                            Image(
                                                painter = painterResource(id = R.drawable.food),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = transportation,
                                        onValueChange = { transportation = it },
                                        label = { Text("Phương tiện di chuyển") },
                                        modifier = Modifier.fillMaxWidth(),
                                        leadingIcon = {
                                            Image(
                                                painter = painterResource(id = R.drawable.car),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val updatedPlan = plan.copy(
                                        itinerary = plan.itinerary.copy(
                                            specialties = specialties.split(",").map { it.trim() },
                                            transportation = transportation.split(",")
                                                .map { it.trim() }
                                        )
                                    )
                                    user?.let {
                                        planViewModel.updatePlanToFirebase(
                                            it.uid,
                                            updatedPlan,
                                            planId,
                                            onSuccess = {
                                                navController.popBackStack()
                                                planViewModel.fetchPlanByIdFromFirebase(
                                                    it.uid,
                                                    planId
                                                )
                                            }
                                        )
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