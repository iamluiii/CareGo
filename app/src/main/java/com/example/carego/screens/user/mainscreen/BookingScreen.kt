package com.example.carego.screens.user.mainscreen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    appointmentId: String,
    caregiverId: String,
    date: String,
    timeSlot: String,
    onBookingSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var caregiverInfo by remember { mutableStateOf<CaregiverInfo?>(null) }
    var isBooking by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("caregivers").document(caregiverId).get()
            .addOnSuccessListener { document ->
                caregiverInfo = CaregiverInfo(
                    id = caregiverId,
                    username = document.getString("username") ?: "",
                    firstName = document.getString("firstName") ?: "",
                    middleName = document.getString("middleName") ?: "",
                    lastName = document.getString("lastName") ?: "",
                    birthday = document.getString("birthday") ?: "",
                    gender = document.getString("gender") ?: "",
                    contactNumber = document.getString("contactNumber") ?: "",
                    email = document.getString("email") ?: "",
                    municipality = document.getString("municipality") ?: "",
                    license = document.getString("license") ?: "",
                    address = document.getString("address") ?: "",
                    password = document.getString("password") ?: "",
                    availabilities = emptyList()
                )
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details") },
                navigationIcon = {
                    IconButton(onClick = { onCancel() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            caregiverInfo?.let { info ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("First Name: ${info.firstName}")
                            Text("Middle Name: ${info.middleName ?: "-"}")
                            Text("Last Name: ${info.lastName}")
                            Text("Birthday: ${info.birthday}")
                            Text("Gender: ${info.gender}")
                            Text("Contact Number: ${info.contactNumber}")
                            Text("Email: ${info.email}")
                            Text("Municipality: ${info.municipality}")
                            Text("Address: ${info.address}")
                            Text("License: ${info.license}")
                            Text("Username: ${info.username}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Appointment Date: $date")
                            Text("Time Slot: $timeSlot")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (isBooking) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { onCancel() },
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    isBooking = true
                                    coroutineScope.launch {
                                        delay(2000)
                                        val success = bookAppointment(appointmentId)
                                        isBooking = false
                                        if (success) {
                                            Toast.makeText(context, "Booking Successful!", Toast.LENGTH_SHORT).show()
                                            onBookingSuccess()
                                        } else {
                                            Toast.makeText(context, "Booking Failed. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Book")
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Update Appointment in Firestore ---
suspend fun bookAppointment(appointmentId: String): Boolean {
    return try {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            db.collection("appointments")
                .document(appointmentId)
                .update(
                    mapOf(
                        "status" to "pending",
                        "userId" to currentUserId  // 🔥 SAVE USER ID
                    )
                )
                .await()
            true
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

