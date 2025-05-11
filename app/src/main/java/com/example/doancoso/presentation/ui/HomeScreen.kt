package com.example.doancoso.presentation.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.doancoso.R
import com.example.doancoso.domain.AuthState
import com.example.doancoso.domain.AuthViewModel
import com.example.doancoso.domain.PlanViewModel
import com.example.doancoso.domain.preferences.UserPreferences


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    planViewModel: PlanViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val userPreferences = UserPreferences(context)
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val startCalendar = remember { java.util.Calendar.getInstance() }
    val endCalendar = remember { java.util.Calendar.getInstance() }

    var chatInput by remember { mutableStateOf("") }
    var chatResponse by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isDialogOpen by remember { mutableStateOf(false) }

    val startDatePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedStart = java.util.Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }

                if (endDate.isNotEmpty() && selectedStart.after(endCalendar)) {
                    // Ngày bắt đầu sau ngày kết thúc => không cho phép
                    Toast.makeText(
                        context,
                        "Ngày bắt đầu không được sau ngày kết thúc!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    startCalendar.set(year, month, dayOfMonth)
                    startDate = "$dayOfMonth/${month + 1}/$year"
                }
            },
            startCalendar.get(java.util.Calendar.YEAR),
            startCalendar.get(java.util.Calendar.MONTH),
            startCalendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    val endDatePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedEnd = java.util.Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }

                if (startDate.isNotEmpty() && selectedEnd.before(startCalendar)) {
                    // Ngày kết thúc trước ngày bắt đầu => không cho phép
                    Toast.makeText(
                        context,
                        "Ngày kết thúc không được trước ngày bắt đầu!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    endCalendar.set(year, month, dayOfMonth)
                    endDate = "$dayOfMonth/${month + 1}/$year"
                }
            },
            endCalendar.get(java.util.Calendar.YEAR),
            endCalendar.get(java.util.Calendar.MONTH),
            endCalendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }


    val user = (authState as? AuthState.UserLoggedIn)?.user

    // Điều hướng về login nếu chưa đăng nhập
    LaunchedEffect(authState) {
        Log.d("AuthDebug", "AuthState hiện tại: $authState ")
        Log.d(
            "AuthDebug",
            "UserPreferences - Name: ${userPreferences.userNameFlow}, Email: ${userPreferences.userEmailFlow}"
        )

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
        }
        return
    }

    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Thanh AppBar
        TopAppBar(
            title = {
                Text(
                    text = "Xin chào, ${user.name}!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { /* TODO: Xử lý thông báo */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.notification),
                        contentDescription = "Thông báo"
                    )
                }
            }
        )

        // Ảnh banner
        Image(
            painter = painterResource(id = R.drawable.homebanner),
            contentDescription = "Hình ảnh du lịch",
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentScale = ContentScale.Crop
        )

        // Ô tìm kiếm
        Box(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Khám phá ngay") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()) {
                            planViewModel.fetchPlans(
                                destination = searchQuery,
                                startDate = startDate,
                                endDate = endDate
                            )
                            navController.navigate(Screen.SearchPlan.route)
                        } else {
                            Toast.makeText(
                                context,
                                "Vui lòng điền đầy đủ thông tin",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = null
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { startDatePickerDialog.show() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Từ ngày")
            }

            Button(
                onClick = { endDatePickerDialog.show() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Đến ngày")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (startDate.isNotEmpty() && endDate.isNotEmpty())
                "Từ $startDate đến $endDate"
            else
                "Vui lòng chọn khoảng thời gian",
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )



        // Kế hoạch du lịch
        Text(
            text = "Kế hoạch du lịch của bạn",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp, top = 20.dp)
        )

        val travelPlans = remember { listOf<String>() } // TODO: Lấy danh sách từ API/database

        if (travelPlans.isEmpty()) {
            Text(
                text = "Chưa có kế hoạch nào. Hãy khám phá ngay!",
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
            )
        } else {
            // TODO: Hiển thị danh sách kế hoạch du lịch ở đây
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Điểm đến phổ biến
        Text(
            text = "Điểm đến phổ biến",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp, top = 20.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            DestinationCard(imageRes = R.drawable.indonesia, title = "INDONESIA")
            DestinationCard(imageRes = R.drawable.japan, title = "JAPAN")
        }
//        Column(modifier = Modifier.fillMaxSize()) {
//            // IconButton to open the dialog
//            IconButton(onClick = { isDialogOpen = true }) {
//                Icon(Icons.Default.Info, contentDescription = "Open Gemini Assistant")
//            }
//
//            // Show Dialog when isDialogOpen is true
//            if (isDialogOpen) {
//                GeminiDialog(
//                    chatInput = chatInput,
//                    onChatInputChange = { chatInput = it },
//                    onSubmit = {
//                        if (chatInput.isNotBlank()) {
//                            isLoading = true
//                            planViewModel.askGemini(chatInput) { result ->
//                                chatResponse = result
//                                isLoading = false
//                            }
//                        }
//                    },
//                    chatResponse = chatResponse,
//                    isLoading = isLoading,
//                    onDismiss = { isDialogOpen = false }
//                )
//            }
//        }
//

        Spacer(modifier = Modifier.weight(1f))

        // Thanh điều hướng dưới cùng
        BottomAppBar(containerColor = Color.White) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.homeicon),
                        contentDescription = "Trang chủ"
                    )
                }
                Divider(modifier = Modifier
                    .height(40.dp)
                    .width(1.dp), color = Color.Gray)
                IconButton(onClick = { navController.navigate("plan") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.planicon),
                        contentDescription = "Kế hoạch"
                    )
                }
                Divider(modifier = Modifier
                    .height(40.dp)
                    .width(1.dp), color = Color.Gray)
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
}


@Composable
fun GeminiDialog(
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    chatResponse: String,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Trợ lý du lịch Gemini", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = chatInput,
                    onValueChange = onChatInputChange,
                    label = { Text("Hỏi về điểm đến...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSubmit,
                    enabled = !isLoading
                ) {
                    Text("Hỏi Gemini")
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else if (chatResponse.isNotEmpty()) {
                    Text("Gemini trả lời:", fontWeight = FontWeight.Bold)
                    Text(chatResponse)
                }
            }
        }
    }
}




@Composable
fun DestinationCard(imageRes: Int, title: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .size(150.dp)
            .shadow(8.dp)
    ) {
        Box {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    "\u2B50 $title",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}
