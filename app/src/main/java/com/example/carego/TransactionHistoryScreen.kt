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
import androidx.navigation.NavHostController
import com.example.carego.screens.user.mainscreen.UserBottomBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


data class FinishedAppointment(
    val patientName: String = "",
    val licenseType: String = "",
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
    val userId = auth.currentUser?.uid ?: return

    var finishedAppointments by remember { mutableStateOf(listOf<FinishedAppointment>()) }
    var isCaregiver by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(true) {
        val caregiverDoc = db.collection("caregivers").document(userId).get().await()
        isCaregiver = caregiverDoc.exists()

        val queryField = if (isCaregiver == true) "caregiverId" else "userId"
        val appointmentsSnapshot = db.collection("appointments")
            .whereEqualTo(queryField, userId)
            .whereEqualTo("status", "Finished")
            .get()
            .await()

        val results = appointmentsSnapshot.documents.mapNotNull { doc ->
            val partnerId = if (isCaregiver == true) doc.getString("userId") else doc.getString("caregiverId")
            if (partnerId.isNullOrEmpty()) return@mapNotNull null

            val date = doc.getString("date") ?: ""
            val time = doc.getString("timeSlot") ?: ""

            val partnerDoc = if (isCaregiver == true)
                db.collection("users").document(partnerId).get().await()
            else
                db.collection("caregivers").document(partnerId).get().await()

            val firstName = partnerDoc.getString("firstName") ?: ""
            val lastName = partnerDoc.getString("lastName") ?: ""
            val middleName = partnerDoc.getString("middleName") ?: ""
            val fullName = listOf(lastName, firstName, middleName)
                .filter { it.isNotEmpty() && it.lowercase() != "null" }
                .joinToString(" ")

            FinishedAppointment(
                patientName = fullName,
                pwdType = if (isCaregiver == true) partnerDoc.getString("pwdType") ?: "" else "",
                licenseType = if (isCaregiver == false) partnerDoc.getString("license") ?: "" else "",
                date = date,
                time = time,
                username = partnerDoc.getString("username") ?: ""
            )
        }

        finishedAppointments = results.sortedByDescending { it.date }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Transaction History") })
        },
        bottomBar = {
            val navHostController = navController as? NavHostController
            if (navHostController != null) {
                if (isCaregiver == true) {
                    CareGiverBottomBar(navHostController)
                } else if (isCaregiver == false) {
                    UserBottomBar(navHostController)
                }
            }
        }
    ) { innerPadding ->
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
                    HistoryCard(appointment, isCaregiver == true)
                }
            }
        }
    }
}


@Composable
fun HistoryCard(appointment: FinishedAppointment, isCaregiver: Boolean) {
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
                Text("Name: ${appointment.patientName}", fontWeight = FontWeight.Bold)
                if (isCaregiver) {
                    Text("PWD Type: ${appointment.pwdType}")
                } else {
                    Text("License: ${appointment.licenseType}")
                }
                Text("Date: ${appointment.date}")
                Text("Time: ${appointment.time}")
                Text("Username: ${appointment.username}")
            }
        }
    }
}



