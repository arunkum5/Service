package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.UserRole
import com.example.ui.screens.AdminOversightScreen
import com.example.ui.screens.CreateJobCardScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.GoogleMapWorkshopLocationScreen
import com.example.ui.screens.InventoryManagementScreen
import com.example.ui.screens.JobCardDetailScreen
import com.example.ui.screens.StaffDashboardScreen
import com.example.ui.screens.TechnicianDashboardScreen
import com.example.ui.screens.TechnicianInspectionScreen
import com.example.ui.screens.VehicleQrScannerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GvdViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GvdAutoWorldApp()
                }
            }
        }
    }
}

@Composable
fun GvdAutoWorldApp(
    viewModel: GvdViewModel = viewModel()
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeJobCardId by viewModel.activeJobCardDetailId.collectAsState()

    // Handle Android system back button for sub-screens
    BackHandler(enabled = currentScreen != "HOME") {
        viewModel.navigateTo("HOME")
    }

    when (currentRole) {
        UserRole.CUSTOMER -> {
            if (currentScreen == "GOOGLE_MAP") {
                GoogleMapWorkshopLocationScreen(
                    onBack = { viewModel.navigateTo("HOME") },
                    onBookServiceClick = { viewModel.navigateTo("HOME") }
                )
            } else {
                CustomerHomeScreen(viewModel = viewModel)
            }
        }

        UserRole.TECHNICIAN -> {
            TechnicianDashboardScreen(viewModel = viewModel)
        }

        UserRole.STAFF -> {
            when (currentScreen) {
                "HOME" -> {
                    StaffDashboardScreen(
                        viewModel = viewModel,
                        onCreateJobCardClick = { viewModel.navigateTo("CREATE_JOBCARD") },
                        onJobCardClick = { id -> viewModel.navigateTo("JOBCARD_DETAIL", id) },
                        onOpenInventoryClick = { viewModel.navigateTo("INVENTORY") },
                        onOpenTechnicianInspectionClick = { viewModel.navigateTo("TECHNICIAN_INSPECTION") },
                        onOpenAppointmentsClick = { /* Could switch or show bookings */ },
                        onOpenQrScannerClick = { viewModel.navigateTo("QR_SCANNER") }
                    )
                }

                "QR_SCANNER" -> {
                    VehicleQrScannerScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("HOME") },
                        onJobCardFound = { id -> viewModel.navigateTo("JOBCARD_DETAIL", id) },
                        onCreateJobCardForVehicle = { vehNo ->
                            viewModel.prefillDraftWithVehicleNumber(vehNo)
                            viewModel.navigateTo("CREATE_JOBCARD")
                        }
                    )
                }

                "CREATE_JOBCARD" -> {
                    CreateJobCardScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("HOME") },
                        onJobCardCreated = { id -> viewModel.navigateTo("JOBCARD_DETAIL", id) }
                    )
                }

                "JOBCARD_DETAIL" -> {
                    JobCardDetailScreen(
                        jobCardId = activeJobCardId ?: 1L,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("HOME") },
                        onOpenTechnicianInspection = { viewModel.navigateTo("TECHNICIAN_INSPECTION") }
                    )
                }

                "TECHNICIAN_INSPECTION" -> {
                    TechnicianInspectionScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("HOME") }
                    )
                }

                "INVENTORY" -> {
                    InventoryManagementScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo("HOME") }
                    )
                }

                else -> {
                    StaffDashboardScreen(
                        viewModel = viewModel,
                        onCreateJobCardClick = { viewModel.navigateTo("CREATE_JOBCARD") },
                        onJobCardClick = { id -> viewModel.navigateTo("JOBCARD_DETAIL", id) },
                        onOpenInventoryClick = { viewModel.navigateTo("INVENTORY") },
                        onOpenTechnicianInspectionClick = { viewModel.navigateTo("TECHNICIAN_INSPECTION") },
                        onOpenAppointmentsClick = { },
                        onOpenQrScannerClick = { viewModel.navigateTo("QR_SCANNER") }
                    )
                }
            }
        }

        UserRole.ADMIN -> {
            AdminOversightScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
