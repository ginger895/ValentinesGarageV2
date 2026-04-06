package com.example.valentinesgaragev2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Valentine's Garage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {

            Card(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(60.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🚚", fontSize = 60.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Truck Service Management",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            MenuCard(
                title = "Check-In Truck",
                description = "Register new truck and record condition",
                icon = "📝",
                onClick = { navController.navigate("checkin") }
            )

            MenuCard(
                title = "Manage Repairs",
                description = "View active trucks and update repair tasks",
                icon = "🔧",
                onClick = { navController.navigate("trucks") }
            )

            MenuCard(
                title = "Truck Management",
                description = "View and manage all trucks in the system",
                icon = "🚛",
                onClick = { navController.navigate("truck_management") }
            )

            MenuCard(
                title = "Reports & Analytics",
                description = "Employee activity and vehicle history",
                icon = "📊",
                onClick = { navController.navigate("reports") }
            )
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    description: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 40.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Main Menu Preview")
@Composable
fun MainMenuPreview() {
    MaterialTheme {
        MainMenu(navController = rememberNavController())
    }
}