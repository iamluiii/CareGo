package com.example.carego.screens.caregiver.mainscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PendingBookingDetailsScreen(appointmentId: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var userDetails by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var showDeclineDialog by remember { mutableStateOf(false) }

    LaunchedEffect(appointmentId) {
        db.collection("appointments").document(appointmentId).get()
            .addOnSuccessListener { appointment ->
                val userId = appointment.getString("userId") ?: return@addOnSuccessListener

                db.collection("users").document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        val data = mapOf(
                            "Full Name" to (userDoc.getString("fullName") ?: ""),
                            "Birthday" to (userDoc.getString("birthday") ?: ""),
                            "Gender" to (userDoc.getString("gender") ?: ""),
                            "Email" to (userDoc.getString("email") ?: ""),
                            "Contact Number" to (userDoc.getString("contactNumber") ?: ""),
                            "Address" to (userDoc.getString("address") ?: ""),
                            "Emergency Contact Name" to (userDoc.getString("emergencyName") ?: ""),
                            "Emergency Contact Number" to (userDoc.getString("emergencyContactNumber") ?: ""),
                            "Username" to (userDoc.getString("username") ?: ""),
                            "PWD Type" to (userDoc.getString("pwdType") ?: "")
                        )
                        userDetails = data
                        isLoading = false
                    }
            }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Booking Details", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            userDetails.forEach { (label, value) ->
                Text("$label: $value", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                updateAppointmentStatus(appointmentId, "Confirmed")
                                delay(2000)
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Text("Accept")
                    }

                    Button(
                        onClick = { showDeclineDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Decline")
                    }

                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Cancel")
                }
                if (showDeclineDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDeclineDialog = false },
                        title = { Text("Confirm Decline") },
                        text = { Text("Are you sure you want to decline this booking?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    scope.launch {
                                        updateAppointmentStatus(appointmentId, "Available")
                                        delay(2000)
                                        navController.popBackStack()
                                    }
                                    showDeclineDialog = false
                                }
                            ) {
                                Text("Yes, Decline")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showDeclineDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

            }
        }
    }
}

private fun updateAppointmentStatus(appointmentId: String, newStatus: String) {
    val db = FirebaseFirestore.getInstance()
    val updates = mutableMapOf<String, Any>(
        "status" to newStatus
    )

    // If status is set to available, remove user-related fields
    if (newStatus.lowercase() == "available") {
        updates["userId"] = FieldValue.delete()
        updates["username"] = FieldValue.delete()
        updates["patientName"] = FieldValue.delete()
        updates["location"] = FieldValue.delete()
        updates["time"] = FieldValue.delete()
        updates["pwdType"] = FieldValue.delete()
    }

    db.collection("appointments").document(appointmentId)
        .update(updates)
}

