package com.example.carego.screens.caregiver.mainscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


data class FinishedAppointment(
    val patientName: String = "",
    val pwdType: String = "",
    val date: String = "",
    val time: String = "",
    val username: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val caregiverId = auth.currentUser?.uid ?: return

    var finishedAppointments by remember { mutableStateOf(listOf<FinishedAppointment>()) }

    LaunchedEffect(true) {
        db.collection("appointments")
            .whereEqualTo("caregiverId", caregiverId)
            .whereEqualTo("status", "Finished")
            .get()
            .addOnSuccessListener { result ->
                val tasks = result.map { doc ->
                    val userId = doc.getString("userId") ?: ""
                    val date = doc.getString("date") ?: ""
                    val time = doc.getString("timeSlot") ?: ""

                    db.collection("users").document(userId).get().continueWith { userTask ->
                        val userDoc = userTask.result
                        val firstName = userDoc?.getString("firstName") ?: ""
                        val lastName = userDoc?.getString("lastName") ?: ""
                        val middleName = userDoc?.getString("middleName") ?: ""
                        val fullName = listOf(lastName, firstName, middleName)
                            .filter { it.isNotEmpty() && it.lowercase() != "null" }
                            .joinToString(" ")

                        FinishedAppointment(
                            patientName = fullName,
                            pwdType = userDoc?.getString("pwdType") ?: "",
                            date = date,
                            time = time,
                            username = userDoc?.getString("username") ?: ""
                        )
                    }
                }

                // Wait for all user lookups to finish
                Tasks.whenAllSuccess<FinishedAppointment>(tasks).addOnSuccessListener { list ->
                    finishedAppointments = list.sortedByDescending { it.date }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History", style = MaterialTheme.typography.titleLarge) }
            )
        },
        bottomBar = {
            CareGiverBottomBar(navController)
        }
    )
{ innerPadding ->
        if (finishedAppointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No finished appointments yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(finishedAppointments) { appointment ->
                    HistoryCard(appointment)
                }
            }
        }
    }
}


@Composable
fun HistoryCard(appointment: FinishedAppointment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text("Patient: ${appointment.patientName}", fontWeight = FontWeight.Bold)
                Text("PWD Type: ${appointment.pwdType}")
                Text("Date: ${appointment.date}")
                Text("Time: ${appointment.time}")
                Text("Username: ${appointment.username}")
            }
        }
    }
}

