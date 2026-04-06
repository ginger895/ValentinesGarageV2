package com.example.valentinesgaragev2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.valentinesgaragev2.viewmodel.GarageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInDetailScreen(navController: NavController, checkInId: Long) {
    val vm: GarageViewModel = viewModel()
    val checkInDetails by vm.currentCheckInDetails.observeAsState()

    LaunchedEffect(checkInId) {
        vm.loadCheckInDetails(checkInId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check-in Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (checkInDetails == null || checkInDetails?.count == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                checkInDetails?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Truck Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("🚛 Registration: ${cursor.getString(cursor.getColumnIndexOrThrow("registration"))}")
                                Text("📝 Make/Model: ${cursor.getString(cursor.getColumnIndexOrThrow("make"))} ${cursor.getString(cursor.getColumnIndexOrThrow("model"))}")
                                Text("📅 Year: ${cursor.getInt(cursor.getColumnIndexOrThrow("year"))}")

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                Text("Check-in Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("📆 Date: ${cursor.getString(cursor.getColumnIndexOrThrow("date"))}")
                                Text("📊 Kilometers: ${cursor.getInt(cursor.getColumnIndexOrThrow("km"))}")
                                Text("👤 Employee ID: ${cursor.getString(cursor.getColumnIndexOrThrow("employee_id"))}")

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                Text("Vehicle Condition", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("🚗 Exterior: ${cursor.getString(cursor.getColumnIndexOrThrow("exterior_condition"))}")
                                Text("💺 Interior: ${cursor.getString(cursor.getColumnIndexOrThrow("interior_condition"))}")
                                Text("🔧 Engine: ${cursor.getString(cursor.getColumnIndexOrThrow("engine_condition"))}")
                                Text("⚙️ Tires: ${cursor.getString(cursor.getColumnIndexOrThrow("tires_condition"))}")

                                val notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"))
                                if (notes.isNotEmpty()) {
                                    Text("📝 Notes: $notes")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Check In Detail Screen Preview")
@Composable
fun CheckInDetailScreenPreview() {
    MaterialTheme {
        CheckInDetailScreen(navController = rememberNavController(), checkInId = 1L)
    }
}