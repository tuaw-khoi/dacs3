package com.example.doancoso.presentation.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.doancoso.R
import com.example.doancoso.domain.AuthState
import com.example.doancoso.domain.AuthViewModel
import com.example.doancoso.domain.PlanUiState
import com.example.doancoso.domain.PlanViewModel
import com.example.doancoso.domain.preferences.UserPreferences
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch


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

    var showSheet by remember { mutableStateOf(false) }
    var selectedDestination by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf(R.drawable.indonesia) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val planState by planViewModel.planState.collectAsState()

    val startDatePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedStart = java.util.Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }

                if (endDate.isNotEmpty() && selectedStart.after(endCalendar)) {
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

    LaunchedEffect(authState) {
        Log.d("AuthDebug", "AuthState hiện tại: $authState ")
        Log.d(
            "AuthDebug",
            "UserPreferences - Name: ${userPreferences.userNameFlow}, Email: ${userPreferences.userEmailFlow}"
        )

        if (authState is AuthState.Idle || user == null) {
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

    LaunchedEffect(user.uid) {
        planViewModel.fetchPlansFromFirebase(user.uid)
    }

    var searchQuery by remember { mutableStateOf("") }

    val onExplore = { selectedStartDate: String, selectedEndDate: String ->
        startDate = selectedStartDate
        endDate = selectedEndDate
        // Thực hiện tìm kiếm kế hoạch với ngày đã chọn
        planViewModel.fetchPlans(selectedDestination, startDate, endDate)
        navController.navigate(Screen.SearchPlan.route)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        TopAppBar(
            title = {
                Text(
                    text = "Xin chào, ${user.name}!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = {
                    navController.navigate("scanQr")
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.scanqr),
                        contentDescription = "Quét mã QR"
                    )
                }
                IconButton(onClick = { /* TODO: Xử lý thông báo */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.notification),
                        contentDescription = "Thông báo"
                    )
                }
            }
        )

        Image(
            painter = painterResource(id = R.drawable.homebanner),
            contentDescription = "Hình ảnh du lịch",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kế hoạch du lịch của bạn",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Chỉ hiển thị nút "Xem thêm" nếu có >= 3 kế hoạch
            if (planState is PlanUiState.SuccessMultiple) {
                val plans = (planState as PlanUiState.SuccessMultiple).plans
                if (plans.size >= 3) {
                    TextButton(onClick = {
                        navController.navigate("plan") // Chuyển sang màn hình Plan
                    }) {
                        Text("Xem thêm")
                    }
                }
            }
        }

        when (planState) {
            is PlanUiState.SuccessMultiple -> {
                val plans = (planState as PlanUiState.SuccessMultiple).plans

                val displayedPlans = plans.take(1)

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)) {
                    displayedPlans.forEach { plan ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    navController.navigate("planDetail/${plan.uid}")
                                },
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = plan.destination,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Từ ${plan.itinerary.startDate} đến ${plan.itinerary.endDate}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            is PlanUiState.FetchingPlans -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Đang tải...")
                }
            }

            is PlanUiState.Error -> {
                Text(
                    text = "Chưa có kế hoạch nào. Hãy khám phá ngay!",
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            else -> {}
        }

        Text(
            text = "Điểm đến phổ biến",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp, top = 20.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            DestinationCard(
                imageRes = R.drawable.indonesia,
                title = "INDONESIA",
                onClick = {
                    selectedDestination = "INDONESIA"
                    selectedImage = R.drawable.indonesia
                    showSheet = true
                }
            )
            DestinationCard(
                imageRes = R.drawable.japan,
                title = "JAPAN",
                onClick = {
                    selectedDestination = "JAPAN"
                    selectedImage = R.drawable.japan
                    showSheet = true
                }
            )
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch {
                        sheetState.hide()
                        showSheet = false
                    }
                },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                tonalElevation = 8.dp,
                containerColor = Color.White
            ) {
                DestinationBottomSheetContent(
                    destination = selectedDestination,
                    imageRes = selectedImage,
                    description = when (selectedDestination) {
                        "INDONESIA" -> "Khám phá vẻ đẹp hoang sơ và những bãi biển tuyệt đẹp tại Indonesia."
                        "JAPAN" -> "Trải nghiệm văn hóa truyền thống kết hợp hiện đại tại Nhật Bản."
                        else -> ""
                    },
                    onDismiss = {
                        coroutineScope.launch {
                            sheetState.hide()
                            showSheet = false
                        }
                    },
                    onExplore = onExplore
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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
                IconButton(onClick = { navController.navigate("setting") }) {
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
fun DestinationCard(imageRes: Int, title: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .size(150.dp)
            .shadow(8.dp)
            .clickable { onClick() }
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


@Composable
fun DestinationBottomSheetContent(
    destination: String,
    imageRes: Int,
    description: String,
    onDismiss: () -> Unit,
    onExplore: (String, String) -> Unit
) {
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    val context = LocalContext.current
    val startCalendar = remember { java.util.Calendar.getInstance() }
    val endCalendar = remember { java.util.Calendar.getInstance() }

    val startDatePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                startCalendar.set(year, month, dayOfMonth)
                startDate = "$dayOfMonth/${month + 1}/$year"
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
                endCalendar.set(year, month, dayOfMonth)
                endDate = "$dayOfMonth/${month + 1}/$year"
            },
            endCalendar.get(java.util.Calendar.YEAR),
            endCalendar.get(java.util.Calendar.MONTH),
            endCalendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = destination,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = destination,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))


        Text(
            text = if (startDate.isNotEmpty() && endDate.isNotEmpty())
                "Từ $startDate đến $endDate"
            else
                "Vui lòng chọn thời gian",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { startDatePickerDialog.show() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Chọn ngày bắt đầu")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { endDatePickerDialog.show() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Chọn ngày kết thúc")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                    onExplore(startDate, endDate)
                    onDismiss()
                } else {
                    Toast.makeText(context, "Vui lòng chọn thời gian", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Khám phá ngay")
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanQrScreen(
    navController: NavController,
    planViewModel: PlanViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val scope = rememberCoroutineScope()

    // Xin quyền CAMERA runtime
    val cameraPermissionState = rememberPermissionState(permission = android.Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Quét mã QR", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.3f)
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                // Camera preview
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Khung quét mã QR (Overlay)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(48.dp), // khoảng cách từ viền màn hình
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 4.dp.toPx()
                        val cornerLength = 30.dp.toPx()
                        val color = Color.White.copy(alpha = 0.9f)

                        // Kích thước khung vuông ở giữa
                        val frameSize = androidx.compose.ui.geometry.Size(width = 250.dp.toPx(), height = 250.dp.toPx())
                        val frameTopLeft = Offset(
                            (size.width - frameSize.width) / 2,
                            (size.height - frameSize.height) / 2
                        )
                        val frameBottomRight = frameTopLeft + Offset(frameSize.width, frameSize.height)

                        // 4 góc
                        val topLeft = frameTopLeft
                        val topRight = Offset(frameBottomRight.x, frameTopLeft.y)
                        val bottomLeft = Offset(frameTopLeft.x, frameBottomRight.y)
                        val bottomRight = frameBottomRight

                        // Top-left corner (L)
                        drawLine(color, topLeft, topLeft + Offset(cornerLength, 0f), strokeWidth)
                        drawLine(color, topLeft, topLeft + Offset(0f, cornerLength), strokeWidth)

                        // Top-right corner (L)
                        drawLine(color, topRight, topRight - Offset(cornerLength, 0f), strokeWidth)
                        drawLine(color, topRight, topRight + Offset(0f, cornerLength), strokeWidth)

                        // Bottom-left corner (L)
                        drawLine(color, bottomLeft, bottomLeft + Offset(cornerLength, 0f), strokeWidth)
                        drawLine(color, bottomLeft, bottomLeft - Offset(0f, cornerLength), strokeWidth)

                        // Bottom-right corner (L)
                        drawLine(color, bottomRight, bottomRight - Offset(cornerLength, 0f), strokeWidth)
                        drawLine(color, bottomRight, bottomRight - Offset(0f, cornerLength), strokeWidth)
                    }

                }


            }
        }


        LaunchedEffect(cameraProviderFuture) {
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val barcodeScanner = BarcodeScanning.getClient()
                val analysisUseCase = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysisUseCase.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { qrContent ->
                                        imageProxy.close()

                                        handleQrContent(
                                            qrContent,
                                            context,
                                            navController,
                                            planViewModel,
                                            authViewModel
                                        )

                                        barcodeScanner.close()
                                        return@addOnSuccessListener
                                    }
                                }
                                imageProxy.close()
                            }
                            .addOnFailureListener {
                                imageProxy.close()
                            }
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    analysisUseCase
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khởi tạo camera: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    } else {
        // Nếu chưa có quyền, hiện UI xin quyền
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ứng dụng cần quyền truy cập camera để quét mã QR")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Cấp quyền camera")
                }
            }
        }
    }
}

fun handleQrContent(
    qrContent: String,
    context: Context,
    navController: NavController,
    planViewModel: PlanViewModel,
    authViewModel: AuthViewModel
) {
    if (!qrContent.startsWith("https://")) {
        Toast.makeText(context, "Không nhận diện được liên kết!", Toast.LENGTH_SHORT).show()
        return
    }
    Log.d("handleQrContent", "qrContent = $qrContent")

    Toast.makeText(context, "Đang tải kế hoạch từ QR...", Toast.LENGTH_SHORT).show()

    val outerUri = Uri.parse(qrContent)
    val innerLinkEncoded = outerUri.getQueryParameter("link") ?: ""

    // Giải mã link bên trong
    val innerLink = Uri.decode(innerLinkEncoded)

    if (innerLink.isEmpty()) {
        Toast.makeText(context, "Mã QR không hợp lệ!", Toast.LENGTH_SHORT).show()
        return
    }

    val innerUri = Uri.parse(innerLink)
    val planId = outerUri.getQueryParameter("planId")
    val ownerUid = innerUri.getQueryParameter("uid")

    Log.d("handleQrContent", "planId = $planId, ownerUid = $ownerUid, link = $innerLink")

    if (planId.isNullOrEmpty() || ownerUid.isNullOrEmpty()) {
        Toast.makeText(context, "Mã QR không hợp lệ!", Toast.LENGTH_SHORT).show()
        return
    }

    planViewModel.importSharedPlan(planId, ownerUid) { success, plan ->
        if (success && plan != null) {
            Log.d("handleQrContent", "Đã tải kế hoạch thành công")
            navController.navigate("planDetailOwnerQR/${planId}/${ownerUid}")
        } else {
            Toast.makeText(context, "Không thể tải kế hoạch", Toast.LENGTH_SHORT).show()
        }
    }
}
