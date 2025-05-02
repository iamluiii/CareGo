package com.example.carego.screens.user.mainscreen

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.carego.R
import com.example.carego.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val userId = auth.currentUser?.uid

    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableStateOf(0L) }
    val userDetails = remember { mutableStateMapOf<String, String>() }
    val isLoggingOut = remember { mutableStateOf(false) }

    // Load user data
    LaunchedEffect(userId) {
        userId?.let {
            firestore.collection("users").document(it).get().addOnSuccessListener { doc ->
                profileImageUrl = doc.getString("profileImageUrl")

                val first = doc.getString("firstName") ?: ""
                val middle = doc.getString("middleName") ?: ""
                val last = doc.getString("lastName") ?: ""
                val fullName = listOf(first, middle, last).filter { it.isNotEmpty() }.joinToString(" ")

                userDetails["Name"] = fullName
                userDetails["Gender"] = doc.getString("gender") ?: ""
                userDetails["Birthday"] = doc.getString("birthday") ?: ""
                userDetails["Username"] = doc.getString("username") ?: ""
                userDetails["Email"] = doc.getString("email") ?: ""
                userDetails["Contact Number"] = doc.getString("contactNumber") ?: ""
                userDetails["Address"] = doc.getString("address") ?: ""
                userDetails["PWD Type"] = doc.getString("pwdType") ?: ""
                userDetails["Emergency Contact Name"] = doc.getString("emergencyName") ?: ""
                userDetails["Emergency Contact Number"] = doc.getString("emergencyNumber") ?: ""
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
                    firestore.collection("users").document(userId!!).update("profileImageUrl", profileImageUrl)
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
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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


            Spacer(modifier = Modifier.height(16.dp))

            // Profile Image
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
                        contentDescription = "Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop

                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionCard("Personal Information") {
                DisplayCard("Name", userDetails["Name"] ?: "")
                DisplayCard("Gender", userDetails["Gender"] ?: "")
                DisplayCard("Birthday", userDetails["Birthday"] ?: "")
                DisplayCard("Username", userDetails["Username"] ?: "")
            }

            SectionCard("Contact Information") {
                DisplayCard("Email", userDetails["Email"] ?: "")
                DisplayCard("Contact Number", userDetails["Contact Number"] ?: "")
                DisplayCard("Address", userDetails["Address"] ?: "")
                DisplayCard("PWD Type", userDetails["PWD Type"] ?: "")
            }

            SectionCard("Emergency Contact") {
                DisplayCard("Emergency Contact Name", userDetails["Emergency Contact Name"] ?: "")
                DisplayCard("Emergency Contact Number", userDetails["Emergency Contact Number"] ?: "")
            }
            Spacer(modifier = Modifier.height(32.dp))
            LogoutButton(
                isLoggingOut = isLoggingOut.value,
                onClick = {
                    if (!isLoggingOut.value) {
                        isLoggingOut.value = true
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            try {
                                delay(2000)
                                FirebaseAuth.getInstance().signOut()

                                profileImageUrl = null
                                userDetails.clear()

                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                navController.navigate(Screen.UserLoginScreen.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Logout failed: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoggingOut.value = false
                            }
                        }
                    }
                }
            )


        }

    }

}
@Composable
fun LogoutButton(
    isLoggingOut: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoggingOut,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (isLoggingOut) "Logging out..." else "Logout")
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
