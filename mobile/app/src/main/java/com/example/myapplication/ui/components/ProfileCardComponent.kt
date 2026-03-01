package com.example.myapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.model.User
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.R

@Composable
fun ProfileCardComponent(
    user: User,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "User Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            HorizontalDivider(color = Color(0xFFE5E7EB))
            ProfileInfoRow("Email", user.email)
            ProfileInfoRow("Full Name", user.fullName)
            user.username?.let { ProfileInfoRow("Username", it) }
            ProfileInfoRow("User Type", user.userType)
            ProfileInfoRow("Status", if (user.isActive) "Active" else "Inactive")
            ProfileInfoRow("Verified", if (user.isVerified) "Yes" else "No")
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Black,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun ProfileImageComponent(
    profilePhotoUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
    ) {
        if (!profilePhotoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(profilePhotoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Profile",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
