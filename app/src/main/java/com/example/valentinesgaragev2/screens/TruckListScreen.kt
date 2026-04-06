// screens/TruckListScreen.kt
package com.example.valentinesgaragev2.screens

import android.database.Cursor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun TruckListScreen(navController: NavController) {
    val vm: GarageViewModel = viewModel()
    val activeCheckIns by vm.activeCheckIns.observeAsState()
    var selectedCheckInId by remember { mutableStateOf<Long?>(null) }
    var showRepairDialog by remember { mutableStateOf(false) }
    var selectedRepairId by remember { mutableStateOf<Long?>(null) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var truckToComplete by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(selectedCheckInId) {
        selectedCheckInId?.let { id ->
            vm.loadRepairsForCheckIn(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Repairs", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = androidx.compose.ui.graphics.Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("truck_management") }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Manage Trucks", tint = androidx.compose.ui.graphics.Color.White)
                    }
                    IconButton(onClick = { vm.loadActiveCheckIns() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                Text("Active Trucks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))

                if (activeCheckIns == null || activeCheckIns?.count == 0) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("No active check-ins", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(getActiveTrucksList(activeCheckIns)) { truck ->
                            ActiveTruckCard(
                                truck = truck,
                                isSelected = selectedCheckInId == truck.id,
                                onClick = {
                                    selectedCheckInId = truck.id
                                    showRepairDialog = false
                                },
                                onComplete = {
                                    truckToComplete = truck.id
                                    showCompleteDialog = true
                                },
                                onViewDetails = {
                                    navController.navigate("checkin_detail/${truck.id}")
                                }
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                Text("Repair Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))

                selectedCheckInId?.let {
                    val repairs by vm.repairs.observeAsState()

                    if (repairs == null || repairs?.count == 0) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Text("Loading repair tasks...", modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(getRepairsList(repairs)) { repair ->
                                RepairTaskCard(
                                    repair = repair,
                                    onUpdateClick = {
                                        selectedRepairId = repair.id
                                        showRepairDialog = true
                                    }
                                )
                            }
                        }
                    }
                } ?: run {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("Select a truck to view repair tasks", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showRepairDialog && selectedRepairId != null) {
        UpdateRepairDialog(
            repairId = selectedRepairId!!,
            onDismiss = { showRepairDialog = false },
            onUpdate = { mechanic, status, notes ->
                vm.updateRepair(selectedRepairId!!, mechanic, status, notes)
                showRepairDialog = false
            }
        )
    }

    if (showCompleteDialog && truckToComplete != null) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Complete Check-in") },
            text = { Text("Are you sure all repairs are done? This will close this check-in.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.completeCheckIn(truckToComplete!!)
                    vm.loadActiveCheckIns()
                    selectedCheckInId = null
                    showCompleteDialog = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) { Text("No") }
            }
        )
    }
}

@Composable
fun ActiveTruckCard(
    truck: ActiveTruck,
    isSelected: Boolean,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(truck.registration, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary) {
                    Text(truck.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = androidx.compose.ui.graphics.Color.White)
                }
            }
            Text("${truck.make} ${truck.model}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("KM: ${truck.km} | Check-in: ${truck.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onComplete, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("Complete")
                }
                Button(onClick = onViewDetails, modifier = Modifier.weight(1f)) {
                    Text("Details")
                }
            }
        }
    }
}

@Composable
fun RepairTaskCard(repair: RepairTask, onUpdateClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = when (repair.status) {
            "Completed" -> MaterialTheme.colorScheme.tertiaryContainer
            "In-Progress" -> MaterialTheme.colorScheme.secondaryContainer
            "Blocked" -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        })
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(repair.taskName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(repair.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary) {
                    Text(repair.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = androidx.compose.ui.graphics.Color.White)
                }
            }
            if (repair.mechanic.isNotEmpty()) Text("👤 ${repair.mechanic}", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            if (repair.notes.isNotEmpty()) Text("📝 ${repair.notes.take(50)}${if (repair.notes.length > 50) "..." else ""}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            Button(onClick = onUpdateClick, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Update Task") }
        }
    }
}

@Composable
fun UpdateRepairDialog(
    @Suppress("UNUSED_PARAMETER") repairId: Long,
    onDismiss: () -> Unit,
    onUpdate: (mechanic: String, status: String, notes: String) -> Unit
) {
    var mechanic by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Pending") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Repair Task") },
        text = {
            Column {
                OutlinedTextField(value = mechanic, onValueChange = { mechanic = it }, label = { Text("Your Name/ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Status", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = status == "Pending", onClick = { status = "Pending" }, label = { Text("Pending") })
                    FilterChip(selected = status == "In-Progress", onClick = { status = "In-Progress" }, label = { Text("In-Progress") })
                    FilterChip(selected = status == "Completed", onClick = { status = "Completed" }, label = { Text("Completed") })
                    FilterChip(selected = status == "Blocked", onClick = { status = "Blocked" }, label = { Text("Blocked") })
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Work Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3, placeholder = { Text("Describe what work was done...") })
            }
        },
        confirmButton = { TextButton(onClick = { if (mechanic.isNotBlank()) onUpdate(mechanic, status, notes) }) { Text("Update") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

data class ActiveTruck(val id: Long, val registration: String, val make: String, val model: String, val date: String, val km: Int, val status: String)
data class RepairTask(val id: Long, val taskName: String, val category: String, val mechanic: String, val status: String, val notes: String)

fun getActiveTrucksList(cursor: Cursor?): List<ActiveTruck> {
    val list = mutableListOf<ActiveTruck>()
    cursor?.use {
        while (it.moveToNext()) {
            list.add(ActiveTruck(
                id = it.getLong(it.getColumnIndexOrThrow("id")),
                registration = it.getString(it.getColumnIndexOrThrow("registration")),
                make = it.getString(it.getColumnIndexOrThrow("make")),
                model = it.getString(it.getColumnIndexOrThrow("model")),
                date = it.getString(it.getColumnIndexOrThrow("date")),
                km = it.getInt(it.getColumnIndexOrThrow("km")),
                status = it.getString(it.getColumnIndexOrThrow("status"))
            ))
        }
    }
    return list
}

fun getRepairsList(cursor: Cursor?): List<RepairTask> {
    val list = mutableListOf<RepairTask>()
    cursor?.use {
        while (it.moveToNext()) {
            list.add(RepairTask(
                id = it.getLong(it.getColumnIndexOrThrow("id")),
                taskName = it.getString(it.getColumnIndexOrThrow("task_name")),
                category = it.getString(it.getColumnIndexOrThrow("category")),
                mechanic = it.getString(it.getColumnIndexOrThrow("mechanic_name")),
                status = it.getString(it.getColumnIndexOrThrow("status")),
                notes = it.getString(it.getColumnIndexOrThrow("notes"))
            ))
        }
    }
    return list
}

@Preview(showBackground = true, name = "Truck List Screen Preview")
@Composable
fun TruckListScreenPreview() {
    MaterialTheme {
        TruckListScreen(navController = rememberNavController())
    }
}