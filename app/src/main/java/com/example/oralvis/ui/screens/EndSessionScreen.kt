package com.example.oralvis.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oralvis.utils.MediaStoreUtils
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndSessionScreen(
    sessionId: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var patientName by remember { mutableStateOf("") }
    var patientAge by remember { mutableStateOf("") }
    var showNameAgeDialog by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf("") }
    var ageError by remember { mutableStateOf("") }
    var sessionMetadata by remember { mutableStateOf<com.example.oralvis.utils.SessionMetadata?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    LaunchedEffect(sessionId) {
        loadSessionImages(context, sessionId) { imageList ->
            images = imageList
            isLoading = false
            
            val metadata = com.example.oralvis.utils.MediaStoreUtils.getSessionMetadata(context, sessionId)
            sessionMetadata = metadata
            
            if (imageList.isNotEmpty() && metadata == null) {
                showNameAgeDialog = true
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = "Session Complete",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Session Ended Successfully",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Session ID: $sessionId",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    
                    sessionMetadata?.let { metadata ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Patient: ${metadata.patientName} (${metadata.patientAge} years)",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Captured Images (${images.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tap on any image to view in full screen",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (images.isEmpty()) {
            EmptyImagesState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(images) { imageUri ->
                    ImageCard(
                        imageUri = imageUri,
                        onClick = {
                            selectedImageUri = imageUri
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Session Completed Successfully!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Use the bottom navigation to start a new session or view all sessions",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    selectedImageUri?.let { uri ->
        FullScreenImageViewer(
            imageUri = uri,
            onClose = {
                selectedImageUri = null
            }
        )
    }
    
    if (showNameAgeDialog && sessionMetadata == null) {
        AlertDialog(
            onDismissRequest = { 
                if (images.isNotEmpty()) {
                    nameError = "Patient information is required"
                } else {
                    showNameAgeDialog = false
                }
            },
            title = {
                Text(
                    text = "Patient Information Required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Please provide patient information to complete this session.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { 
                            patientName = it
                            nameError = ""
                        },
                        label = { Text("Patient Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = nameError.isNotEmpty(),
                        supportingText = {
                            if (nameError.isNotEmpty()) {
                                Text(nameError, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    
                    OutlinedTextField(
                        value = patientAge,
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() }
                            patientAge = filtered
                            ageError = ""
                        },
                        label = { Text("Patient Age *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        isError = ageError.isNotEmpty(),
                        supportingText = {
                            if (ageError.isNotEmpty()) {
                                Text(ageError, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        var isValid = true
                        
                        if (patientName.trim().isEmpty()) {
                            nameError = "Name is required"
                            isValid = false
                        } else if (patientName.trim().length < 2) {
                            nameError = "Name must be at least 2 characters"
                            isValid = false
                        }
                        
                        if (patientAge.isEmpty()) {
                            ageError = "Age is required"
                            isValid = false
                        } else {
                            val age = patientAge.toIntOrNull()
                            if (age == null) {
                                ageError = "Please enter a valid age"
                                isValid = false
                            } else if (age < 1 || age > 120) {
                                ageError = "Age must be between 1 and 120"
                                isValid = false
                            }
                        }
                        
                        if (isValid) {
                            com.example.oralvis.utils.MediaStoreUtils.saveSessionMetadata(
                                context, 
                                sessionId, 
                                patientName.trim(), 
                                patientAge
                            )
                            sessionMetadata = com.example.oralvis.utils.SessionMetadata(
                                patientName = patientName.trim(),
                                patientAge = patientAge,
                                timestamp = System.currentTimeMillis()
                            )
                            showNameAgeDialog = false
                        }
                    },
                    enabled = patientName.isNotBlank() && patientAge.isNotBlank()
                ) {
                    Text("Save & Complete")
                }
            },
            dismissButton = {
                if (images.isEmpty()) {
                    OutlinedButton(
                        onClick = { showNameAgeDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun ImageCard(
    imageUri: Uri,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Captured image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Captured Image",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Tap to view full screen",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "View full screen",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FullScreenImageViewer(
    imageUri: Uri,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Full screen image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyImagesState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No images captured",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start a new session to capture oral images",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private suspend fun loadSessionImages(
    context: android.content.Context,
    sessionId: String,
    onImagesLoaded: (List<Uri>) -> Unit
) {
    try {
        val mediaStoreImages = MediaStoreUtils.listImagesForSession(context, sessionId)
        if (mediaStoreImages.isNotEmpty()) {
            onImagesLoaded(mediaStoreImages)
            return
        }
        
        val sessionDir = java.io.File(context.getExternalFilesDir(null), "sessions/$sessionId")
        if (sessionDir.exists() && sessionDir.isDirectory) {
            val imageFiles = sessionDir.listFiles { file ->
                file.isFile && (file.extension.lowercase() == "jpg" || file.extension.lowercase() == "jpeg" || file.extension.lowercase() == "png")
            }
            
            if (imageFiles != null && imageFiles.isNotEmpty()) {
                val imageUris = imageFiles.map { file ->
                    android.net.Uri.fromFile(file)
                }
                onImagesLoaded(imageUris)
                return
            }
        }
        
        onImagesLoaded(emptyList())
        
    } catch (e: Exception) {
        onImagesLoaded(emptyList())
    }
}
