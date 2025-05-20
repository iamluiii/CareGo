package com.example.carego.screens.user.mainscreen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.carego.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableStateOf(0L) }
    val userDetails = remember { mutableStateMapOf<String, String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    // Dialog states
    var showPersonalDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Load user data
    LaunchedEffect(userId) {
        userId?.let {
            firestore.collection("users").document(it).get().addOnSuccessListener { doc ->
                profileImageUrl = doc.getString("profileImageUrl")
                userDetails["Name"] = (doc.getString("firstName") ?: "") + " " + (doc.getString("lastName") ?: "")
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

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
                    .background(
                        color = Color(0xFF6bdbe0),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            bottomStart = 45.dp,
                            bottomEnd = 45.dp,
                            topStart = 0.dp,
                            topEnd = 0.dp
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Profile",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
                    .size(150.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color(0xFF6bdbe0), CircleShape)
                    .clickable {
                        val now = System.currentTimeMillis()
                        if (now - lastClickTime > 2000) {
                            lastClickTime = now
                            // Add your image picker logic here
                        }
                    }
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Image(
                        painter = if (profileImageUrl != null)
                            rememberAsyncImagePainter(profileImageUrl)
                        else
                            rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Information Card
            // Personal Information Card
            InfoCard(
                title = "Personal Information",
                userDetails = userDetails,
                fields = listOf("Name", "Gender", "Birthday", "Username"), // Includes Username
                nonEditableFields = listOf("Username"),
                onEditClick = { showPersonalDialog = true }
            )

// Contact Information Card
            InfoCard(
                title = "Contact Information",
                userDetails = userDetails,
                fields = listOf("Email", "Contact Number", "Address", "PWD Type"), // Includes Email
                nonEditableFields = listOf("Email"),
                onEditClick = { showContactDialog = true }
            )

// Emergency Contact Card
            InfoCard(
                title = "Emergency Contact",
                userDetails = userDetails,
                fields = listOf("Emergency Contact Name", "Emergency Contact Number"),
                onEditClick = { showEmergencyDialog = true }
            )
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("LoginScreen") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFed3782)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Logout", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Delete Account", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

// Delete Account Confirmation Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = {
                        Text("Delete Account", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    },
                    text = {
                        Text("Are you sure you want to delete your account? This action cannot be undone.", fontSize = 16.sp)
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val user = FirebaseAuth.getInstance().currentUser
                                val userId = user?.uid

                                // Delete user data from Firestore
                                if (userId != null) {
                                    FirebaseFirestore.getInstance().collection("users").document(userId).delete()
                                }

                                // Delete user account


                                user?.delete()?.addOnCompleteListener { task ->
                                    mainHandler.post {
                                        if (task.isSuccessful) {
                                            navController.navigate("LoginScreen") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                            Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000))
                        ) {
                            Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDeleteDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }

    // Personal Information Dialog
    if (showPersonalDialog) {
        EditDialog(
            title = "Edit Personal Information",
            fields = listOf("Name", "Gender", "Birthday"),
            userDetails = userDetails,
            onDismiss = { showPersonalDialog = false },
            onSave = { updatedData ->
                firestore.collection("users").document(userId!!).update(updatedData.mapValues { it.value as Any })
                userDetails.putAll(updatedData.mapValues { it.value.toString() })
                showPersonalDialog = false
                Toast.makeText(context, "Personal information updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Contact Information Dialog
    if (showContactDialog) {
        EditDialog(
            title = "Edit Contact Information",
            fields = listOf("Contact Number", "Address", "PWD Type"),
            userDetails = userDetails,
            onDismiss = { showContactDialog = false },
            onSave = { updatedData ->
                firestore.collection("users").document(userId!!).update(updatedData.mapValues { it.value as Any })
                userDetails.putAll(updatedData.mapValues { it.value.toString() })
                showContactDialog = false
                Toast.makeText(context, "Contact information updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Emergency Contact Dialog
    if (showEmergencyDialog) {
        EditDialog(
            title = "Edit Emergency Contact",
            fields = listOf("Emergency Contact Name", "Emergency Contact Number"),
            userDetails = userDetails,
            onDismiss = { showEmergencyDialog = false },
            onSave = { updatedData ->
                firestore.collection("users").document(userId!!).update(updatedData.mapValues { it.value as Any })
                userDetails.putAll(updatedData.mapValues { it.value.toString() })
                showEmergencyDialog = false
                Toast.makeText(context, "Emergency contact updated", Toast.LENGTH_SHORT).show()
            }
        )
    }
}



@Composable
fun InfoCard(
    title: String,
    userDetails: Map<String, String>,
    fields: List<String>,
    nonEditableFields: List<String> = emptyList(),
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFed3782)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Title and Edit Button
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display each field
            for (field in fields) {
                ProfileField(field, userDetails[field] ?: "")
            }
        }
    }
}


@Composable
fun ProfileField(label: String, value: String) {
    Text("$label:", color = Color.White, fontWeight = FontWeight.Bold)
    Text(value, color = Color.White)
    Spacer(modifier = Modifier.height(8.dp))
}


@Composable
fun EditDialog(
    title: String,
    fields: List<String>,
    userDetails: Map<String, String>,
    nonEditableFields: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    val updatedValues = remember { mutableStateMapOf<String, String>() }

    // Pre-fill the current values
    LaunchedEffect(fields) {
        for (field in fields) {
            if (!nonEditableFields.contains(field)) {
                updatedValues[field] = userDetails[field] ?: ""
            }
        }
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column {
                for (field in fields) {
                    if (!nonEditableFields.contains(field)) {
                        Text(text = field, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = updatedValues[field] ?: "",
                            onValueChange = { updatedValues[field] = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = {
                    onSave(updatedValues.mapValues { it.value as Any })
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            androidx.compose.material3.Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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


