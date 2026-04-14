package com.example.vine_rater.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.vine_rater.R
import com.example.vine_rater.data.Wine
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWineScreen(
    wineId: Int? = null,
    viewModel: WineViewModel,
    onNavigateBack: () -> Unit
) {
    val wines by viewModel.allWines.collectAsState()
    val existingWine = remember(wineId, wines) {
        wines.find { it.id == wineId }
    }

    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Red") }
    var rating by remember { mutableStateOf("Good") }
    var boughtAt by remember { mutableStateOf("") }
    var tasteNotes by remember { mutableStateOf("") }
    var smellNotes by remember { mutableStateOf("") }
    var customNotes by remember { mutableStateOf("") }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showPhotoOptions by remember { mutableStateOf(false) }

    LaunchedEffect(existingWine) {
        existingWine?.let {
            name = it.name
            year = it.year
            type = it.type
            rating = it.rating
            boughtAt = it.boughtAt
            tasteNotes = it.tasteNotes
            smellNotes = it.smellNotes
            customNotes = it.customNotes
            photoUris = it.photoUris.map { uri -> Uri.parse(uri) }
        }
    }

    val context = LocalContext.current
    var currentTempUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentTempUri?.let { uri ->
                photoUris = photoUris + uri
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        val savedUris = uris.mapNotNull { uri ->
            saveImageToInternalStorage(context, uri)
        }
        photoUris = photoUris + savedUris
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.filesDir, "wine_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            currentTempUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (wineId == null) "Rate a Wine" else "Edit Tasting") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Gallery Section
            Text("Photos", style = MaterialTheme.typography.titleMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().height(150.dp)
            ) {
                items(photoUris) { uri ->
                    Box {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.size(150.dp).clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { photoUris = photoUris - uri },
                            modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.extraSmall)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.White)
                        }
                    }
                }
                item {
                    OutlinedCard(
                        onClick = { showPhotoOptions = true },
                        modifier = Modifier.size(150.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                                Text("Add")
                            }
                        }
                    }
                }
            }

            if (showPhotoOptions) {
                AlertDialog(
                    onDismissRequest = { showPhotoOptions = false },
                    title = { Text("Add Photo") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = {
                                    showPhotoOptions = false
                                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                        val file = File(context.filesDir, "wine_${System.currentTimeMillis()}.jpg")
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        currentTempUri = uri
                                        cameraLauncher.launch(uri)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                Spacer(Modifier.width(12.dp))
                                Text("Take Photo")
                            }
                            TextButton(
                                onClick = {
                                    showPhotoOptions = false
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(Modifier.width(12.dp))
                                Text("Pick from Gallery")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPhotoOptions = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Wine Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year (Vintage)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

            Text("Wine Type", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Red", "White", "Rosé", "Sparkling").forEach { option ->
                    FilterChip(selected = type == option, onClick = { type = option }, label = { Text(option) })
                }
            }

            Text("Rating", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Very Good", "Good", "Average", "Bad", "Very Bad").forEach { option ->
                    FilterChip(selected = rating == option, onClick = { rating = option }, label = { Text(option) })
                }
            }

            OutlinedTextField(value = boughtAt, onValueChange = { boughtAt = it }, label = { Text("Bought At") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = tasteNotes, onValueChange = { tasteNotes = it }, label = { Text("Taste Notes") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = smellNotes, onValueChange = { smellNotes = it }, label = { Text("Smell Notes") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = customNotes, onValueChange = { customNotes = it }, label = { Text("Custom Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.insertWine(
                            Wine(
                                id = wineId ?: 0,
                                name = name,
                                year = year,
                                photoUris = photoUris.map { it.toString() },
                                type = type,
                                rating = rating,
                                boughtAt = boughtAt,
                                tasteNotes = tasteNotes,
                                smellNotes = smellNotes,
                                customNotes = customNotes,
                                dateAdded = existingWine?.dateAdded ?: System.currentTimeMillis()
                            )
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(if (wineId == null) "Save Tasting" else "Update Tasting")
            }
        }
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "wine_gallery_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
