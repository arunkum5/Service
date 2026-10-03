package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.CustomerReviewEntity
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.StatusGood
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StarRatingBar(
    rating: Int,
    maxStars: Int = 5,
    onRatingChanged: ((Int) -> Unit)? = null,
    starSize: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..maxStars) {
            val isFilled = i <= rating
            val icon = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarOutline
            val tint = if (isFilled) AccentGold else Color(0xFFC0C0C0)

            if (onRatingChanged != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onRatingChanged(i) }
                        .testTag("star_rating_$i"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Rate $i stars",
                        tint = tint,
                        modifier = Modifier.size(starSize)
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = "$rating stars",
                    tint = tint,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RateServiceDialog(
    initialVehicleNo: String = "",
    initialCustomerName: String = "",
    initialMobile: String = "",
    initialJobCardNo: String = "",
    onDismiss: () -> Unit,
    onSubmit: (CustomerReviewEntity) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var customerName by remember { mutableStateOf(initialCustomerName.ifBlank { "Client" }) }
    var customerMobile by remember { mutableStateOf(initialMobile) }
    var vehicleNumber by remember { mutableStateOf(initialVehicleNo.ifBlank { "KA01MJ9821" }) }
    var serviceType by remember { mutableStateOf("Periodic Lube & General Service") }
    var comment by remember { mutableStateOf("") }
    var selectedTags by remember {
        mutableStateOf(setOf("Transparent Pricing", "Genuine Spares", "On-Time Delivery"))
    }
    var punctualityRating by remember { mutableIntStateOf(5) }
    var cleanlinessRating by remember { mutableIntStateOf(5) }
    var pricingRating by remember { mutableIntStateOf(5) }

    val availableTags = listOf(
        "On-Time Delivery",
        "Transparent Pricing",
        "Digital 360 Report",
        "WhatsApp Alerts",
        "Genuine Spares",
        "Expert Mechanics",
        "Courteous Staff",
        "Clean Lounge",
        "Doorstep Pickup",
        "Flawless Wash"
    )

    val serviceTypes = listOf(
        "Periodic Lube & General Service",
        "Brake & Mechanical Repair",
        "9H Ceramic Coating",
        "PPF Detailing",
        "Major Engine Service",
        "Insurance & Bodywork"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CrimsonRed.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Rate Your Service",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E2022)
                            )
                            Text(
                                text = "GVD Auto World Bangalore",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Star Rating Block
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "How was your overall experience?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        StarRatingBar(
                            rating = rating,
                            onRatingChanged = { rating = it },
                            starSize = 34.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val ratingDescription = when (rating) {
                            5 -> "⭐️⭐️⭐️⭐️⭐️ Outstanding! Highly Recommended"
                            4 -> "⭐️⭐️⭐️⭐️ Very Good Service"
                            3 -> "⭐️⭐️⭐️ Satisfactory / Average"
                            2 -> "⭐️⭐️ Below Expectations"
                            else -> "⭐️ Unsatisfactory Experience"
                        }

                        Text(
                            text = ratingDescription,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rating >= 4) Color(0xFF2E7D32) else if (rating == 3) Color(0xFFE65100) else CrimsonRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vehicle & Service Package selection
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it.uppercase() },
                        label = { Text("Vehicle Reg No", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Client Name", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Service Package:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    serviceTypes.forEach { type ->
                        FilterChip(
                            selected = serviceType == type,
                            onClick = { serviceType = type },
                            label = { Text(type, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonRed.copy(alpha = 0.12f),
                                selectedLabelColor = CrimsonRed
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Aspect ratings
                Text(
                    text = "Detailed Ratings:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))

                AspectRatingRow(
                    label = "Punctuality & Delivery",
                    rating = punctualityRating,
                    onRatingChanged = { punctualityRating = it }
                )
                AspectRatingRow(
                    label = "Workshop Cleanliness",
                    rating = cleanlinessRating,
                    onRatingChanged = { cleanlinessRating = it }
                )
                AspectRatingRow(
                    label = "Pricing & Transparency",
                    rating = pricingRating,
                    onRatingChanged = { pricingRating = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Experience Highlights / Tags
                Text(
                    text = "What did you like the most?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTags = if (isSelected) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                            },
                            label = { Text(tag, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE8F5E9),
                                selectedLabelColor = Color(0xFF1B5E20)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Comments input
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Write your review / feedback", fontSize = 12.sp) },
                    placeholder = {
                        Text(
                            "Share your experience with our technicians, vehicle performance after service, or recommendations...",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("review_comment_input"),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val review = CustomerReviewEntity(
                                customerName = customerName.ifBlank { "Verified Customer" },
                                customerMobile = customerMobile,
                                vehicleNumber = vehicleNumber.ifBlank { "KA01MJ9821" },
                                vehicleModel = "",
                                serviceType = serviceType,
                                rating = rating,
                                aspectPunctuality = punctualityRating,
                                aspectCleanliness = cleanlinessRating,
                                aspectPricing = pricingRating,
                                tags = selectedTags.joinToString(", "),
                                comment = comment.ifBlank { "Great experience with GVD Auto World team in Bangalore." },
                                jobCardNumber = initialJobCardNo,
                                isVerifiedClient = true,
                                createdAt = System.currentTimeMillis()
                            )
                            onSubmit(review)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_review_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Review")
                    }
                }
            }
        }
    }
}

@Composable
private fun AspectRatingRow(
    label: String,
    rating: Int,
    onRatingChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.DarkGray)
        StarRatingBar(
            rating = rating,
            onRatingChanged = onRatingChanged,
            starSize = 18.dp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerReviewCard(
    review: CustomerReviewEntity,
    isAdminMode: Boolean = false,
    onReplyClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(review.createdAt) { dateFormat.format(Date(review.createdAt)) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Avatar, Name, Verified Badge, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (review.rating >= 4) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.customerName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = if (review.rating >= 4) Color(0xFF2E7D32) else CrimsonRed,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = review.customerName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E2022)
                            )
                            if (review.isVerifiedClient) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Client",
                                    tint = StatusGood,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Verified",
                                    fontSize = 10.sp,
                                    color = StatusGood,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (review.vehicleNumber.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = review.vehicleNumber + if (review.vehicleModel.isNotBlank()) " • ${review.vehicleModel}" else "",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    StarRatingBar(rating = review.rating, starSize = 16.dp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Service Package Tag
            if (review.serviceType.isNotBlank()) {
                Surface(
                    color = CrimsonRed.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = review.serviceType,
                        color = CrimsonRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Customer Comment
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    fontSize = 13.sp,
                    color = Color(0xFF2C3437),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tags
            if (review.tags.isNotBlank()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    review.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                        Surface(
                            color = Color(0xFFF1F3F5),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "✓ $tag",
                                fontSize = 10.sp,
                                color = Color(0xFF495057),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Admin Response section
            if (review.adminResponse.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8F9FA))
                        .border(1.dp, Color(0xFFE9ECEF), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GVD Auto World Bangalore Response",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = review.adminResponse,
                            fontSize = 12.sp,
                            color = Color(0xFF333333),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Admin Action Buttons (Reply / Delete)
            if (isAdminMode) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF1F3F5))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onReplyClick != null) {
                        OutlinedButton(
                            onClick = onReplyClick,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("admin_reply_btn_${review.id}"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = CrimsonRed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (review.adminResponse.isBlank()) "Reply to Client" else "Edit Reply",
                                fontSize = 11.sp,
                                color = CrimsonRed
                            )
                        }
                    }

                    if (onDeleteClick != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Review",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReplyDialog(
    review: CustomerReviewEntity,
    onDismiss: () -> Unit,
    onSubmitReply: (String) -> Unit
) {
    var replyText by remember { mutableStateOf(review.adminResponse) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Reply to ${review.customerName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E2022)
                )
                Text(
                    text = "Your response will be visible on the client's service portal and review record.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Review summary snippet
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarRatingBar(rating = review.rating, starSize = 14.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "(${review.serviceType})", fontSize = 10.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${review.comment}\"",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            maxLines = 3
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Workshop Response", fontSize = 12.sp) },
                    placeholder = {
                        Text(
                            "e.g., Thank you for your review! We're glad our Bangalore team took great care of your vehicle.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("admin_reply_text"),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onSubmitReply(replyText.trim())
                            }
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("submit_admin_reply_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                    ) {
                        Text("Post Reply")
                    }
                }
            }
        }
    }
}
