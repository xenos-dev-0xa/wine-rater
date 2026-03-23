package com.example.vine_rater.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.vine_rater.R
import com.example.vine_rater.data.Wine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WineDetailScreen(
    wineId: Int,
    viewModel: WineViewModel,
    onEditClick: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val wines by viewModel.allWines.collectAsState()
    val wine = wines.find { it.id == wineId }
    var fullscreenPhotoUri by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(wine?.name ?: "Wine Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    wine?.let {
                        IconButton(onClick = { onEditClick(it.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = {
                            viewModel.deleteWine(it)
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        wine?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                if (it.photoUris.isEmpty()) {
                    AsyncImage(
                        model = R.drawable.placeholder,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(it.photoUris) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = it.name,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(250.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { fullscreenPhotoUri = uri },
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.placeholder),
                                error = painterResource(R.drawable.placeholder)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailItem(label = "Vintage Year", value = it.year)
                    DetailItem(label = "Type", value = it.type)
                    DetailItem(label = "Rating", value = it.rating)
                    DetailItem(label = "Bought At", value = it.boughtAt)
                    DetailItem(label = "Taste Notes", value = it.tasteNotes)
                    DetailItem(label = "Smell Notes", value = it.smellNotes)
                    
                    if (it.customNotes.isNotBlank()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Custom Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(it.customNotes, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (fullscreenPhotoUri != null) {
        Dialog(
            onDismissRequest = { fullscreenPhotoUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = fullscreenPhotoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullscreenPhotoUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    if (value.isNotBlank()) {
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
