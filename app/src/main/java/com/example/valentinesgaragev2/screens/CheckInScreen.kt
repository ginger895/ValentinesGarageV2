package com.example.valentinesgaragev2.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.valentinesgaragev2.viewmodel.GarageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(navController: NavController) {
    val vm: GarageViewModel = viewModel()
    val context = LocalContext.current

    var registration by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var kilometers by remember { mutableStateOf("") }
    var exteriorCondition by remember { mutableStateOf("") }
    var interiorCondition by remember { mutableStateOf("") }
    var engineCondition by remember { mutableStateOf("") }
    var tiresCondition by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expandedSection by remember { mutableStateOf("truck") }

    val checkInResult by vm.checkInResult.observeAsState()

    fun checkIfTruckExists(reg: String) {
        val cursor = vm.getTruckByRegistration(reg)
        cursor.use {
            if (it.moveToFirst()) {
                val existingMake = it.getString(it.getColumnIndexOrThrow("make"))
                val existingModel = it.getString(it.getColumnIndexOrThrow("model"))
                val existingYear = it.getInt(it.getColumnIndexOrThrow("year"))
                make = existingMake
                model = existingModel
                year = existingYear.toString()
                Toast.makeText(context, "Truck found! Auto-filled details.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(checkInResult) {
        when (checkInResult) {
            true -> {
                Toast.makeText(context, "Check-in successful!", Toast.LENGTH_SHORT).show()
                vm.clearCheckInResult()
                navController.navigate("trucks")
            }
            false -> {
                Toast.makeText(context, "Check-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                vm.clearCheckInResult()
            }
            null -> { }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Truck Check-In", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ExpandableCard(
                title = "🚛 Truck Information",
                expanded = expandedSection == "truck",
                onExpandChange = { expanded -> expandedSection = if (expanded) "truck" else "" }
            ) {
                OutlinedTextField(
                    value = registration,
                    onValueChange = {
                        registration = it
                        if (it.length >= 3) checkIfTruckExists(it)
                    },
                    label = { Text("Registration Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = make,
                        onValueChange = { make = it },
                        label = { Text("Make *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            ExpandableCard(
                title = "📋 Check-in Information",
                expanded = expandedSection == "checkin",
                onExpandChange = { expanded -> expandedSection = if (expanded) "checkin" else "" }
            ) {
                OutlinedTextField(
                    value = employeeId,
                    onValueChange = { employeeId = it },
                    label = { Text("Employee ID *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = kilometers,
                    onValueChange = { kilometers = it },
                    label = { Text("Kilometers Driven *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = kilometers.isNotEmpty() && kilometers.toIntOrNull() == null
                )

                if (kilometers.isNotEmpty() && kilometers.toIntOrNull() == null) {
                    Text(
                        "Please enter a valid number",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                }
            }

            ExpandableCard(
                title = "🔍 Vehicle Condition",
                expanded = expandedSection == "condition",
                onExpandChange = { expanded -> expandedSection = if (expanded) "condition" else "" }
            ) {
                Text("Exterior Condition", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = exteriorCondition,
                    onValueChange = { exteriorCondition = it },
                    placeholder = { Text("Dents, scratches, rust, paint condition...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Interior Condition", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = interiorCondition,
                    onValueChange = { interiorCondition = it },
                    placeholder = { Text("Seats, dashboard, cleanliness, damage...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Engine Condition", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = engineCondition,
                    onValueChange = { engineCondition = it },
                    placeholder = { Text("Noise, leaks, performance, warning lights...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Tire Condition", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = tiresCondition,
                    onValueChange = { tiresCondition = it },
                    placeholder = { Text("Tread depth, pressure, damage, alignment...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            ExpandableCard(
                title = "📝 Additional Notes",
                expanded = expandedSection == "notes",
                onExpandChange = { expanded -> expandedSection = if (expanded) "notes" else "" }
            ) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Any special instructions or observations...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (registration.isNotBlank() && make.isNotBlank() && model.isNotBlank() &&
                        employeeId.isNotBlank() && kilometers.isNotBlank()) {
                        vm.addCheckIn(
                            registration = registration,
                            make = make,
                            model = model,
                            year = year.toIntOrNull() ?: 2024,
                            employeeId = employeeId,
                            km = kilometers.toInt(),
                            exterior = exteriorCondition,
                            interior = interiorCondition,
                            engine = engineCondition,
                            tires = tiresCondition,
                            notes = notes
                        )
                    } else {
                        Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Complete Check-In", fontSize = MaterialTheme.typography.titleMedium.fontSize)
            }
        }
    }
}

@Composable
fun ExpandableCard(
    title: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandChange(!expanded) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    content()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Check In Screen Preview")
@Composable
fun CheckInScreenPreview() {
    MaterialTheme {
        CheckInScreen(navController = rememberNavController())
    }
}