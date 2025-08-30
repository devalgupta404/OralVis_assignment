package com.example.oralvis.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.oralvis.utils.MediaStoreUtils
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.ImageCaptureException
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    sessionId: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (hasCameraPermission) {
            CameraPreview(
                onImageCaptureReady = { capture ->
                    imageCapture = capture
                },
                onError = { /* Handle error */ }
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "Session: $sessionId",
                    modifier = Modifier.padding(12.dp),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = {
                    navController.navigate("end_session/$sessionId")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "End Session",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("End Session")
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            CaptureButton(
                onCaptureClick = {
                    if (imageCapture != null && !isCapturing) {
                        isCapturing = true
                        captureImage(context, imageCapture!!, sessionId) { success ->
                            isCapturing = false
                            if (success) {
                                showSuccessMessage = true
                                kotlinx.coroutines.MainScope().launch {
                                    kotlinx.coroutines.delay(2000)
                                    showSuccessMessage = false
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Camera not ready", Toast.LENGTH_SHORT).show()
                    }
                },
                isCapturing = isCapturing
            )
        }
        
        if (showSuccessMessage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Photo captured!",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun captureImage(
    context: android.content.Context,
    imageCapture: ImageCapture,
    sessionId: String,
    onComplete: (Boolean) -> Unit
) {
    try {
        val photoFile = File(
            context.getExternalFilesDir(null),
            "IMG_${System.currentTimeMillis()}.jpg"
        )
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    saveToMediaStore(context, photoFile, sessionId) { success ->
                        onComplete(success)
                        if (success) {
                            Toast.makeText(context, "Photo captured successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    onComplete(false)
                    Toast.makeText(context, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    } catch (e: Exception) {
        onComplete(false)
        Toast.makeText(context, "Capture exception: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun saveToMediaStore(
    context: android.content.Context,
    photoFile: File,
    sessionId: String,
    onComplete: (Boolean) -> Unit
) {
    try {
        val uri = MediaStoreUtils.createImageUri(context, sessionId)
        if (uri != null) {
            val inputStream = photoFile.inputStream()
            val outputStream = MediaStoreUtils.openOutputStream(context, uri)
            
            if (outputStream != null) {
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                MediaStoreUtils.finalizeImageUri(context, uri)
                onComplete(true)
            } else {
                saveToAppDirectory(context, photoFile, sessionId, onComplete)
            }
        } else {
            saveToAppDirectory(context, photoFile, sessionId, onComplete)
        }
    } catch (e: Exception) {
        saveToAppDirectory(context, photoFile, sessionId, onComplete)
    }
}

private fun saveToAppDirectory(
    context: android.content.Context,
    photoFile: File,
    sessionId: String,
    onComplete: (Boolean) -> Unit
) {
    try {
        val sessionDir = File(context.getExternalFilesDir(null), "sessions/$sessionId")
        if (!sessionDir.exists()) {
            sessionDir.mkdirs()
        }
        
        val destinationFile = File(sessionDir, "IMG_${System.currentTimeMillis()}.jpg")
        photoFile.copyTo(destinationFile, overwrite = true)
        
        onComplete(true)
    } catch (e: Exception) {
        onComplete(false)
    } finally {
        if (photoFile.exists()) {
            photoFile.delete()
        }
    }
}

@Composable
fun CameraPreview(
    onImageCaptureReady: (ImageCapture) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isInitialized by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        try {
            val future = ProcessCameraProvider.getInstance(context)
            cameraProvider = future.get()
        } catch (e: Exception) {
            onError(ImageCaptureException(ImageCapture.ERROR_UNKNOWN, "Failed to get camera provider", e))
        }
    }
    
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraProvider?.unbindAll()
            } catch (e: Exception) {
                // Handle cleanup error
            }
        }
    }
    
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            if (cameraProvider != null && !isInitialized) {
                try {
                    val preview = Preview.Builder().build()
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    
                    cameraProvider!!.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        capture
                    )
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    imageCapture = capture
                    isInitialized = true
                    onImageCaptureReady(capture)
                } catch (e: Exception) {
                    onError(ImageCaptureException(ImageCapture.ERROR_UNKNOWN, e.message ?: "Unknown error", e))
                }
            }
        }
    )
}

@Composable
fun CaptureButton(
    onCaptureClick: () -> Unit,
    isCapturing: Boolean
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = if (isCapturing) Color.Gray else Color.White,
            onClick = if (!isCapturing) onCaptureClick else { {} }
        ) {
            if (isCapturing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}
