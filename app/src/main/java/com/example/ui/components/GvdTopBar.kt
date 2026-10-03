package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.CrimsonRed

@Composable
fun GvdTopBar(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    title: String = "GVD AUTO WORLD",
    subtitle: String? = null
) {
    var showRoleDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkCrimson,
                            CrimsonRed
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Logo & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "GVD Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GVD AUTO WORLD",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFB300))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "BANGALORE",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Text(
                            text = subtitle ?: when (currentRole) {
                                UserRole.CUSTOMER -> "Customer Portal • 360° Health Report"
                                UserRole.TECHNICIAN -> "Technician Workspace • Daily Labour & Health Inspection"
                                UserRole.STAFF -> "Staff & Advisor Portal • Job Cards & Inventory"
                                UserRole.ADMIN -> "Admin Oversight • Sales & Monthly Reports"
                            },
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Role Switcher Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable { showRoleDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("role_switcher_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (currentRole) {
                                UserRole.CUSTOMER -> Icons.Default.Person
                                UserRole.TECHNICIAN -> Icons.Default.Engineering
                                UserRole.STAFF -> Icons.Default.DirectionsCar
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                            },
                            contentDescription = "Role Icon",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentRole) {
                                UserRole.CUSTOMER -> "Customer"
                                UserRole.TECHNICIAN -> "Technician"
                                UserRole.STAFF -> "Staff"
                                UserRole.ADMIN -> "Admin"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Switch",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showRoleDialog) {
        RoleSwitchDialog(
            currentRole = currentRole,
            onRoleSelected = { role ->
                onRoleSelected(role)
                showRoleDialog = false
            },
            onDismiss = { showRoleDialog = false }
        )
    }
}

@Composable
fun RoleSwitchDialog(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentRole) }
    var credentialInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Select Portal Login",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "GVD Auto World provides 3 specialized formats:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Customer Role Card
                RoleOptionCard(
                    title = "1. Customer Portal",
                    subtitle = "Vehicle service status, 360° health report, pay bills, book service & Google Map location",
                    icon = Icons.Default.Person,
                    isSelected = selectedRole == UserRole.CUSTOMER,
                    onClick = { selectedRole = UserRole.CUSTOMER }
                )

                // Technician Role Card (MANDATORY REQUIREMENT)
                RoleOptionCard(
                    title = "2. Technician Workspace",
                    subtitle = "Daily labour generated ONLY, 3-option component inspections (replace/good/serviced) & photo uploads",
                    icon = Icons.Default.Engineering,
                    isSelected = selectedRole == UserRole.TECHNICIAN,
                    onClick = { selectedRole = UserRole.TECHNICIAN }
                )

                // Staff / Service Advisor Role Card
                RoleOptionCard(
                    title = "3. Staff / Service Advisor",
                    subtitle = "Manage orders, make jobcards, issue & add inventory in Bangalore Hub",
                    icon = Icons.Default.DirectionsCar,
                    isSelected = selectedRole == UserRole.STAFF,
                    onClick = { selectedRole = UserRole.STAFF }
                )

                // Admin Oversight Role Card
                RoleOptionCard(
                    title = "4. Admin Oversight",
                    subtitle = "Executive oversight: daily sales, monthly revenue reports, full financial ledger & reminders",
                    icon = Icons.Default.AdminPanelSettings,
                    isSelected = selectedRole == UserRole.ADMIN,
                    onClick = { selectedRole = UserRole.ADMIN }
                )

                Spacer(modifier = Modifier.height(4.dp))
                when (selectedRole) {
                    UserRole.CUSTOMER -> {
                        Text(
                            text = "Customer Mode: Authenticated for vehicle MH12RY1234 (Ashay Kohad)",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    UserRole.TECHNICIAN -> {
                        Text(
                            text = "Technician Mode: Restricted to daily labour generated. Overall garage finances hidden.",
                            fontSize = 11.sp,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    UserRole.STAFF -> {
                        Text(
                            text = "Staff Mode: Authorized for GVD Auto World Bangalore Workshop Hub",
                            fontSize = 11.sp,
                            color = Color(0xFF0288D1)
                        )
                    }
                    UserRole.ADMIN -> {
                        Text(
                            text = "Admin Mode: Full business analytics, reports & automated reminder rules",
                            fontSize = 11.sp,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onRoleSelected(selectedRole) },
                modifier = Modifier.testTag("confirm_role_button")
            ) {
                Text("Switch to " + when (selectedRole) {
                    UserRole.CUSTOMER -> "Customer"
                    UserRole.TECHNICIAN -> "Technician"
                    UserRole.STAFF -> "Staff"
                    UserRole.ADMIN -> "Admin"
                }, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RoleOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CrimsonRed else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFEBEE) else Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) CrimsonRed else Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color.DarkGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isSelected) CrimsonRed else Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    lineHeight = 14.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = CrimsonRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
