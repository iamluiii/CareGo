package com.example.carego

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun VerificationScreen(navController: NavController) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var userType by remember { mutableStateOf<String?>(null) }
    var imageUrls by remember { mutableStateOf(mapOf<String, String?>()) }
    var isLicensed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Load user type and license status
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            try {
                val caregiverDoc = FirebaseFirestore.getInstance()
                    .collection("caregivers")
                    .document(currentUserId)
                    .get()
                    .await()

                if (caregiverDoc.exists()) {
                    userType = "Caregiver"
                    isLicensed = caregiverDoc.getString("license") != null
                } else {
                    val userDoc = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUserId)
                        .get()
                        .await()

                    if (userDoc.exists()) {
                        userType = "User"
                    }
                }
            } catch (e: Exception) {
                // Trigger toast correctly
                Toast.makeText(context, "Failed to load user type.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        // Show caregiver or user items based on userType
        if (userType == "Caregiver") {
            Text("Caregiver Verification", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // License or Another Valid ID
            if (isLicensed) {
                VerificationItem("License", "license", currentUserId, userType, context, imageUrls)
            } else {
                VerificationItem("Another Valid ID", "another_valid_id", currentUserId, userType, context, imageUrls)
            }

            // Other Caregiver Items
            listOf(
                "Valid ID" to "valid_id",
                "NBI Clearance" to "nbi_clearance",
                "Picture Holding IDs" to "holding_ids",
                "Signature" to "signature"
            ).forEach { (label, field) ->
                VerificationItem(label, field, currentUserId, userType, context, imageUrls)
            }

        } else if (userType == "User") {
            Text("User Verification", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            listOf(
                "PWD ID" to "pwd_id",
                "Guardian/Relative ID" to "guardian_id",
                "Picture Holding IDs" to "holding_ids",
                "Signature" to "signature"
            ).forEach { (label, field) ->
                VerificationItem(label, field, currentUserId, userType, context, imageUrls)
            }

        }

        Spacer(modifier = Modifier.height(24.dp))

        // Back to Dashboard Button
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFED3782))
        ) {
            Text("Back to Dashboard")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Verify Button
        // Verify Button
        Button(
            onClick = {
                currentUserId?.let { userId ->
                    scope.launch {
                        try {
                            // Check if this is a user or caregiver
                            val collection = if (userType == "Caregiver") "caregivers" else "users"

                            FirebaseFirestore.getInstance()
                                .collection(collection)
                                .document(userId)
                                .update("verificationStatus", "pending")
                                .await()

                            Toast.makeText(context, "Verification submitted successfully.", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to submit verification.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFED3782))
        ) {
            Text("Verify")
        }


        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
fun VerificationItem(label: String, field: String, userId: String?, userType: String?, context: android.content.Context, imageUrls: Map<String, String?>) {
    var imageUrl by remember { mutableStateOf(imageUrls[field]) }

    LaunchedEffect(userId, field) {
        if (userId != null && imageUrl == null) {
            try {
                // Use correct Firestore collection based on user type
                val collection = if (userType == "User") "users" else "caregivers"
                val docRef = FirebaseFirestore.getInstance().collection(collection).document(userId)

                val doc = docRef.get().await()
                imageUrl = doc.getString(field)
            } catch (_: Exception) {
                Toast.makeText(context, "Failed to load $label image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("verification/$userId/$field.jpg")
            val parsedUri = android.net.Uri.parse(uri.toString())

            storageRef.putFile(parsedUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        // Use correct Firestore collection based on user type
                        val collection = if (userType == "User") "users" else "caregivers"
                        val imageField = if (field == "license") "licenseImageUrl" else field

                        FirebaseFirestore.getInstance()
                            .collection(collection)
                            .document(userId!!)
                            .update(imageField, downloadUrl.toString())
                            .addOnSuccessListener {
                                imageUrl = downloadUrl.toString()
                                Toast.makeText(context, "$label uploaded successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to update $label in Firestore.", Toast.LENGTH_SHORT).show()
                            }

                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to upload $label image.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (imageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "$label Image",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (imageUrl == null) "Upload $label" else "Replace $label")
            }
        }
    }
}


private fun uploadValidIdImage(uriString: String, userId: String?, context: android.content.Context, onSuccess: (String) -> Unit) {
    if (userId == null) return

    val storageRef = FirebaseStorage.getInstance().reference.child("valid_ids/$userId.jpg")
    val uri = android.net.Uri.parse(uriString)

    storageRef.putFile(uri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                // Update the Firestore profile image URL
                FirebaseFirestore.getInstance()
                    .collection("caregivers")
                    .document(userId)
                    .update("validIdUrl", downloadUrl.toString())
                    .addOnSuccessListener {
                        onSuccess(downloadUrl.toString())
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to update Firestore record.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to upload image.", Toast.LENGTH_SHORT).show()
        }
}

@Composable
fun VerificationCard(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable { navController.navigate("verification_screen") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Verify Your Identity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    navController.navigate("verification_screen")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload Valid ID")
            }
        }
    }
}
