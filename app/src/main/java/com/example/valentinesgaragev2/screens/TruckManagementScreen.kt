package com.example.valentinesgaragev2.screens

import android.database.Cursor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
fun TruckManagementScreen(navController: NavController) {
    val vm: GarageViewModel = viewModel()
    val trucks by vm.trucks.observeAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Truck Management", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Truck")
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
            if (trucks == null || trucks?.count == 0) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No trucks found")
                            Button(onClick = { showAddDialog = true }, modifier = Modifier.padding(top = 16.dp)) {
                                Text("Add Your First Truck")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(getTrucksList(trucks)) { truck ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("🚛 ${truck.registration}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("${truck.make} ${truck.model} (${truck.year})")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTruckDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { registration, make, model, year ->
                vm.addTruck(registration, make, model, year)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddTruckDialog(
    onDismiss: () -> Unit,
    onAdd: (registration: String, make: String, model: String, year: Int) -> Unit
) {
    var registration by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Truck") },
        text = {
            Column {
                OutlinedTextField(value = registration, onValueChange = { registration = it }, label = { Text("Registration *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = make, onValueChange = { make = it }, label = { Text("Make *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, modifier = Modifier.fillMaxWidth(), singleLine = true, isError = year.isNotEmpty() && year.toIntOrNull() == null)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (registration.isNotBlank() && make.isNotBlank() && model.isNotBlank()) {
                    onAdd(registration, make, model, year.toIntOrNull() ?: 2024)
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

data class TruckItem(val id: Long, val registration: String, val make: String, val model: String, val year: Int)

fun getTrucksList(cursor: Cursor?): List<TruckItem> {
    val list = mutableListOf<TruckItem>()
    cursor?.use {
        while (it.moveToNext()) {
            list.add(TruckItem(
                id = it.getLong(it.getColumnIndexOrThrow("id")),
                registration = it.getString(it.getColumnIndexOrThrow("registration")),
                make = it.getString(it.getColumnIndexOrThrow("make")),
                model = it.getString(it.getColumnIndexOrThrow("model")),
                year = it.getInt(it.getColumnIndexOrThrow("year"))
            ))
        }
    }
    return list
}

@Preview(showBackground = true, name = "Truck Management Screen Preview")
@Composable
fun TruckManagementScreenPreview() {
    MaterialTheme {
        TruckManagementScreen(navController = rememberNavController())
    }
}