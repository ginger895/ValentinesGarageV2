// screens/ReportsScreen.kt
package com.example.valentinesgaragev2.screens

import android.database.Cursor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
fun ReportsScreen(navController: NavController) {
    val vm: GarageViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchPlate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.loadEmployeeReport()
        vm.loadOutstandingTasks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Employee") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Employees") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Vehicle History") },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "History") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Outstanding") },
                    icon = { Icon(Icons.Default.Warning, contentDescription = "Outstanding") }
                )
            }

            when (selectedTab) {
                0 -> EmployeeReportTab(vm)
                1 -> VehicleHistoryTab(vm, searchPlate, onSearch = { searchPlate = it })
                2 -> OutstandingTasksTab(vm)
            }
        }
    }
}

@Composable
fun EmployeeReportTab(vm: GarageViewModel) {
    val employeeReport by vm.employeeReport.observeAsState()
    var selectedMechanic by remember { mutableStateOf<String?>(null) }
    val employees by vm.employees.observeAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (employees != null && (employees?.count ?: 0) > 0) {
            Text("Select Mechanic", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val mechanicNames = getEmployeeNames(employees)
                FilterChip(
                    selected = selectedMechanic == null,
                    onClick = { selectedMechanic = null; vm.loadEmployeeReport() },
                    label = { Text("All") }
                )
                mechanicNames.take(3).forEach { name ->
                    FilterChip(
                        selected = selectedMechanic == name,
                        onClick = { selectedMechanic = name; vm.loadEmployeeReport(name) },
                        label = { Text(name.take(10)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (employeeReport == null || employeeReport?.count == 0) {
            Card(modifier = Modifier.fillMaxSize(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp))
                        Text("No completed tasks yet", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(getEmployeeReportItems(employeeReport)) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("👤 ${item.mechanicName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("✅ ${item.tasksCompleted} tasks", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tasks: ${item.tasks}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleHistoryTab(vm: GarageViewModel, searchPlate: String, onSearch: (String) -> Unit) {
    val vehicleHistory by vm.vehicleHistory.observeAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchPlate,
            onValueChange = onSearch,
            label = { Text("Enter License Plate") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { if (searchPlate.isNotBlank()) vm.loadVehicleHistory(searchPlate) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchPlate.isBlank()) {
            Card(modifier = Modifier.fillMaxSize(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(48.dp))
                        Text("Enter a license plate to view history", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        } else if (vehicleHistory == null || vehicleHistory?.count == 0) {
            Card(modifier = Modifier.fillMaxSize(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No history found for $searchPlate")
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(getVehicleHistoryItems(vehicleHistory)) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("📅 ${item.date}", fontWeight = FontWeight.Bold)
                                Text("📊 ${item.km} km", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (item.exterior.isNotEmpty()) Text("🛞 Exterior: ${item.exterior}", fontSize = 12.sp)
                            if (item.interior.isNotEmpty()) Text("💺 Interior: ${item.interior}", fontSize = 12.sp)
                            if (item.engine.isNotEmpty()) Text("🔧 Engine: ${item.engine}", fontSize = 12.sp)
                            if (item.tires.isNotEmpty()) Text("⚙️ Tires: ${item.tires}", fontSize = 12.sp)
                            if (item.notes.isNotEmpty()) Text("📝 Notes: ${item.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutstandingTasksTab(vm: GarageViewModel) {
    val outstandingTasks by vm.outstandingTasks.observeAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Outstanding Tasks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { vm.loadOutstandingTasks() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (outstandingTasks == null || outstandingTasks?.count == 0) {
            Card(modifier = Modifier.fillMaxSize(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(48.dp))
                        Text("All tasks completed!", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(getOutstandingTasksList(outstandingTasks)) { task ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = when (task.status) {
                        "Blocked" -> MaterialTheme.colorScheme.errorContainer
                        "In-Progress" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    })) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${task.registration} - ${task.taskName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${task.category} | Status: ${task.status}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (task.mechanic.isNotEmpty()) Text("Assigned: ${task.mechanic}", fontSize = 11.sp)
                            }
                            Surface(shape = MaterialTheme.shapes.small, color = when (task.status) {
                                "Blocked" -> MaterialTheme.colorScheme.error
                                "In-Progress" -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.primary
                            }) {
                                Text(task.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = androidx.compose.ui.graphics.Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class EmployeeReportItem(val mechanicName: String, val tasksCompleted: Int, val tasks: String)
data class VehicleHistoryItem(val date: String, val km: Int, val exterior: String, val interior: String, val engine: String, val tires: String, val notes: String)
data class OutstandingTask(val registration: String, val taskName: String, val category: String, val status: String, val mechanic: String)

fun getEmployeeNames(cursor: Cursor?): List<String> {
    val names = mutableListOf<String>()
    cursor?.use { while (it.moveToNext()) names.add(it.getString(it.getColumnIndexOrThrow("name"))) }
    return names
}

fun getEmployeeReportItems(cursor: Cursor?): List<EmployeeReportItem> {
    val items = mutableListOf<EmployeeReportItem>()
    cursor?.use {
        while (it.moveToNext()) {
            items.add(EmployeeReportItem(
                mechanicName = it.getString(it.getColumnIndexOrThrow("mechanic_name")),
                tasksCompleted = it.getInt(it.getColumnIndexOrThrow("tasks_completed")),
                tasks = it.getString(it.getColumnIndexOrThrow("tasks"))
            ))
        }
    }
    return items
}

fun getVehicleHistoryItems(cursor: Cursor?): List<VehicleHistoryItem> {
    val items = mutableListOf<VehicleHistoryItem>()
    cursor?.use {
        while (it.moveToNext()) {
            items.add(VehicleHistoryItem(
                date = it.getString(it.getColumnIndexOrThrow("date")),
                km = it.getInt(it.getColumnIndexOrThrow("km")),
                exterior = it.getString(it.getColumnIndexOrThrow("exterior_condition")),
                interior = it.getString(it.getColumnIndexOrThrow("interior_condition")),
                engine = it.getString(it.getColumnIndexOrThrow("engine_condition")),
                tires = it.getString(it.getColumnIndexOrThrow("tires_condition")),
                notes = it.getString(it.getColumnIndexOrThrow("notes"))
            ))
        }
    }
    return items
}

fun getOutstandingTasksList(cursor: Cursor?): List<OutstandingTask> {
    val tasks = mutableListOf<OutstandingTask>()
    cursor?.use {
        while (it.moveToNext()) {
            tasks.add(OutstandingTask(
                registration = it.getString(it.getColumnIndexOrThrow("registration")),
                taskName = it.getString(it.getColumnIndexOrThrow("task_name")),
                category = it.getString(it.getColumnIndexOrThrow("category")),
                status = it.getString(it.getColumnIndexOrThrow("status")),
                mechanic = it.getString(it.getColumnIndexOrThrow("mechanic_name"))
            ))
        }
    }
    return tasks
}

@Preview(showBackground = true, name = "Reports Screen Preview")
@Composable
fun ReportsScreenPreview() {
    MaterialTheme {
        ReportsScreen(navController = rememberNavController())
    }
}