package com.example.doancoso.presentation.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.doancoso.R
import com.example.doancoso.domain.AuthViewModel
import com.example.doancoso.domain.ThemeViewModel

//@Composable
//fun SettingScreen(
//    navController: NavHostController,
//    authViewModel: AuthViewModel
//) {
//    val context = LocalContext.current
////
////    Box(
////        modifier = Modifier
////            .fillMaxSize()
////            .padding(16.dp),
////        contentAlignment = Alignment.Center
////    ) {
////        Button(
////            onClick = {
////                authViewModel.logoutUser()
////                navController.navigate("login") {
////                    popUpTo("home") { inclusive = true }
////                }
////            },
////            modifier = Modifier
////                .fillMaxWidth()
////                .height(48.dp)
////        ) {
////            Text("Đăng xuất")
////        }
////    }
//    val userState by authViewModel.user.collectAsState()  // Sử dụng collectAsState để lấy thông tin người dùng
//
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = "Hồ sơ của bạn",
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Image(
//                painter = painterResource(id = R.drawable.ic_avatar),
//                contentDescription = "Avatar",
//                modifier = Modifier
//                    .size(120.dp)
//                    .clip(CircleShape)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = userState?.name ?: "Người dùng ẩn danh",
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//            Text(
//                text = userState?.email ?: "Không rõ người dùng",
//                style = MaterialTheme.typography.titleMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Text(
//                text = "Du khách phiêu lưu ✈️",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Divider()
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            ProfileOptionItem(
//                title = "Chỉnh sửa hồ sơ",
//                onClick = {
//                    navController.navigate("editProfile")  // Navigate to EditProfileScreen
//                }
//            )
//
//            ProfileOptionItem(
//                title = "Đăng xuất",
//                color = MaterialTheme.colorScheme.error,
//                onClick = {
//                    authViewModel.logoutUser()
//                    navController.navigate("login") {
//                        popUpTo("home") { inclusive = true }
//                    }
//                }
//            )
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            Text(
//                text = "Ứng dụng du lịch cùng bạn trên mọi hành trình!",
//                fontSize = 14.sp,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 24.dp)
//            )
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel
) {
    val userState by authViewModel.user.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Avatar & User Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(contentAlignment = Alignment.BottomEnd) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_avatar),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userState?.name ?: "Người dùng ẩn danh",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userState?.email ?: "Không rõ người dùng",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Du khách phiêu lưu ✈️",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            Divider()

            // Options
            Column(modifier = Modifier.padding(top = 16.dp)) {
                ProfileOptionItem(
                    title = "Chỉnh sửa hồ sơ",
                    icon = painterResource(R.drawable.pen),
                    onClick = { navController.navigate("editProfile") }
                )

                ProfileOptionItem(
                    title = "Đổi chủ đề giao diện",
                    icon = painterResource(R.drawable.dark_mode),
                    onClick = {
//                        themeViewModel.toggleTheme()
                    }
                )


                ProfileOptionItem(
                    title = "Ngôn ngữ",
                    icon = painterResource(R.drawable.language),
                    onClick = {

                    }
                )

                ProfileOptionItem(
                    title = "Trợ giúp & Hỗ trợ",
                    icon = painterResource(R.drawable.help),
                    onClick = {
                        navController.navigate("help")
                    }
                )

                ProfileOptionItem(
                    title = "Điều khoản & Chính sách",
                    icon = painterResource(R.drawable.terms),
                    onClick = {
                        navController.navigate("terms")
                    }
                )

                ProfileOptionItem(
                    title = "Đăng xuất",
                    icon = painterResource(R.drawable.logout),
                    onClick = {
                        authViewModel.logoutUser()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Phiên bản 1.0 • TravelMate",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        }
    }
}


@Composable
fun ProfileOptionItem(
    title: String,
    icon: Painter,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
}

