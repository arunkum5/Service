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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.JobCardEntity
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.StatusGood
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PaymentGatewayDialog(
    jobCard: JobCardEntity,
    onPaymentSuccess: (paymentMethod: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("UPI") }
    var upiId by remember { mutableStateOf("${jobCard.customerMobile}@upi") }
    var cardNumber by remember { mutableStateOf("4532 •••• •••• 8891") }
    var isProcessing by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val payableAmount = if (jobCard.balanceAmount > 0) jobCard.balanceAmount else jobCard.totalAmount

    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Secure",
                        tint = StatusGood,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "GVD Auto Secure Pay",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "256-Bit Encrypted Workshop Billing",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        text = {
            if (isSuccess) {
                // Success Receipt Screen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = StatusGood,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Payment of ₹${payableAmount.toInt()} Successful!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = StatusGood
                    )
                    Text(
                        text = "Transaction ID: GVD-TXN-${System.currentTimeMillis() % 1000000}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9FA)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            ReceiptRow("Vehicle", jobCard.vehicleNumber)
                            ReceiptRow("Job Card", jobCard.jobCardNumber)
                            ReceiptRow("Customer", jobCard.customerName)
                            ReceiptRow("Paid via", selectedMethod)
                            ReceiptRow("Balance", "₹ 0.00 (Cleared)")
                        }
                    }
                }
            } else if (isProcessing) {
                // Processing Animation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = CrimsonRed, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Contacting Bank Gateway...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Authorizing ₹${payableAmount.toInt()} via $selectedMethod",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            } else {
                // Payment Selection Screen
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Invoice Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Payable Amount:",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "₹ ${payableAmount.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = CrimsonRed
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = jobCard.vehicleNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = jobCard.jobCardNumber,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    Text(
                        text = "Select Payment Mode:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    // Option 1: UPI
                    PaymentOptionRow(
                        title = "UPI / QR / Instant Apps",
                        subtitle = "Google Pay, PhonePe, Paytm, BHIM",
                        icon = Icons.Default.QrCode2,
                        isSelected = selectedMethod == "UPI",
                        onSelect = { selectedMethod = "UPI" }
                    )
                    if (selectedMethod == "UPI") {
                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text("Virtual Payment Address (VPA)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Option 2: Cards
                    PaymentOptionRow(
                        title = "Credit / Debit Card",
                        subtitle = "Visa, MasterCard, RuPay (Zero convenience fee)",
                        icon = Icons.Default.CreditCard,
                        isSelected = selectedMethod == "CARD",
                        onSelect = { selectedMethod = "CARD" }
                    )
                    if (selectedMethod == "CARD") {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("Card Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Option 3: NetBanking
                    PaymentOptionRow(
                        title = "Net Banking / Workshop Counter",
                        subtitle = "HDFC, ICICI, SBI, Axis or Workshop Counter",
                        icon = Icons.Default.AccountBalance,
                        isSelected = selectedMethod == "NETBANKING",
                        onSelect = { selectedMethod = "NETBANKING" }
                    )
                }
            }
        },
        confirmButton = {
            if (isSuccess) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGood)
                ) {
                    Text("Done")
                }
            } else if (!isProcessing) {
                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            delay(1600) // simulated bank gateway roundtrip
                            isProcessing = false
                            isSuccess = true
                            onPaymentSuccess(selectedMethod)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    modifier = Modifier.testTag("submit_payment_button")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay ₹${payableAmount.toInt()} Now", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isProcessing && !isSuccess) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun PaymentOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CrimsonRed else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            ),
        color = if (isSelected) Color(0xFFFFF8F8) else Color.White
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) CrimsonRed else Color.DarkGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) CrimsonRed else Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = CrimsonRed)
            )
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
