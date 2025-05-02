package com.example.carego.screens.caregiver.mainscreen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.carego.R
import com.example.carego.screens.user.mainscreen.LogoutButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareGiverProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableStateOf(0L) }
    val caregiverDetails = remember { mutableStateMapOf<String, String>() }
    val isLoggingOut = remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        userId?.let {
            firestore.collection("caregivers").document(it).get().addOnSuccessListener { doc ->
                profileImageUrl = doc.getString("profileImageUrl")

                val fullName = listOf(
                    doc.getString("firstName"),
                    doc.getString("middleName"),
                    doc.getString("lastName")
                ).filterNotNull().filter { it.isNotEmpty() }.joinToString(" ")

                caregiverDetails["Name"] = fullName
                caregiverDetails["Gender"] = doc.getString("gender") ?: ""
                caregiverDetails["Birthday"] = doc.getString("birthday") ?: ""
                caregiverDetails["Username"] = doc.getString("username") ?: ""
                caregiverDetails["Email"] = doc.getString("email") ?: ""
                caregiverDetails["Contact Number"] = doc.getString("contactNumber") ?: ""
                caregiverDetails["Address"] = doc.getString("address") ?: ""
                caregiverDetails["License Type"] = doc.getString("license") ?: ""
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            val imageRef = storage.reference.child("profile_images/$userId.jpg")
            imageRef.putFile(uri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    profileImageUrl = downloadUri.toString()
                    firestore.collection("caregivers").document(userId!!)
                        .update("profileImageUrl", profileImageUrl)
                    Toast.makeText(context, "Profile image updated", Toast.LENGTH_SHORT).show()
                    isUploading = false
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                isUploading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caregiver Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LaunchedEffect(isLoggingOut.value) {
            if (isLoggingOut.value) {
                kotlinx.coroutines.delay(2000)
                FirebaseAuth.getInstance().signOut()

                profileImageUrl = null
                caregiverDetails.clear()

                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                navController.navigate("choose_screen") {
                    popUpTo(0) { inclusive = true }
                }

                isLoggingOut.value = false
            }
        }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable {
                        val now = System.currentTimeMillis()
                        if (now - lastClickTime > 2000) {
                            lastClickTime = now
                            launcher.launch("image/*")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    CircularProgressIndicator()
                } else {
                    Image(
                        painter = if (profileImageUrl != null)
                            rememberAsyncImagePainter(profileImageUrl)
                        else
                            rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionCard("Personal Information") {
                DisplayCard("Name", caregiverDetails["Name"] ?: "")
                DisplayCard("Gender", caregiverDetails["Gender"] ?: "")
                DisplayCard("Birthday", caregiverDetails["Birthday"] ?: "")
                DisplayCard("Username", caregiverDetails["Username"] ?: "")
            }

            SectionCard("Contact Information") {
                DisplayCard("Email", caregiverDetails["Email"] ?: "")
                DisplayCard("Contact Number", caregiverDetails["Contact Number"] ?: "")
                DisplayCard("Address", caregiverDetails["Address"] ?: "")
                DisplayCard("License Type", caregiverDetails["License Type"] ?: "")
            }

            LogoutButton(
                isLoggingOut = isLoggingOut.value,
                onClick = {
                    if (!isLoggingOut.value) {
                        isLoggingOut.value = true
                    }
                }
            )
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun DisplayCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "$label:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

