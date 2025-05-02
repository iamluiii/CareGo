package com.example.carego.screens.user.mainscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.carego.R
import com.example.carego.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(
    onLogout: () -> Unit,
    navController: NavHostController
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val isLoading = remember { mutableStateOf(true) }
    val username = remember { mutableStateOf("") }
    val pwdType = remember { mutableStateOf("") }

    val allAppointments = remember { mutableStateListOf<AppointmentInfo>() }
    val availableCaregivers = remember { mutableStateListOf<AppointmentInfo>() }
    val pendingBookings = remember { mutableStateListOf<AppointmentInfo>() }
    val confirmedAppointments = remember { mutableStateListOf<AppointmentInfo>() }
    val coroutineScope = rememberCoroutineScope()

    val selectedDate = remember { mutableStateOf("") }
    val selectedTimeSlot = remember { mutableStateOf("") }
    val selectedLicenseType = remember { mutableStateOf("") }
    val selectedMunicipality = remember { mutableStateOf("") }

    val showFilterDialog = remember { mutableStateOf(false) }
    val showTimeSlotDialog = remember { mutableStateOf(false) }
    val showLicenseDialog = remember { mutableStateOf(false) }
    val showMunicipalityDialog = remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    fun applyFilter() {
        availableCaregivers.clear()
        pendingBookings.clear()
        confirmedAppointments.clear()

        val seenIds = mutableSetOf<String>()

        for (appointment in allAppointments) {
            if (!seenIds.add(appointment.id)) continue // ✅ Skip duplicates by ID

            when (appointment.status.lowercase()) {
                "available" -> {
                    if ((selectedDate.value.isEmpty() || appointment.date == selectedDate.value) &&
                        (selectedTimeSlot.value.isEmpty() || appointment.timeSlot == selectedTimeSlot.value) &&
                        (selectedLicenseType.value.isEmpty() || appointment.license == selectedLicenseType.value) &&
                        (selectedMunicipality.value.isEmpty() || appointment.municipality == selectedMunicipality.value)
                    ) {
                        availableCaregivers.add(appointment)
                    }
                }

                "pending" -> {
                    if (pendingBookings.none { it.id == appointment.id }) {
                        pendingBookings.add(appointment)
                    }
                }

                "confirmed" -> {
                    if (confirmedAppointments.none {
                            it.date == appointment.date &&
                                    it.timeSlot == appointment.timeSlot &&
                                    it.caregiverUsername == appointment.caregiverUsername
                        }) {
                        confirmedAppointments.add(appointment)
                    }
                }

            }
        }
    }

    fun listenToAppointmentsRealtime() {
        val currentUserId = auth.currentUser?.uid
        db.collection("appointments")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    isLoading.value = false
                    return@addSnapshotListener
                }

                allAppointments.clear()

                val jobs = mutableListOf<Job>()

                for (doc in snapshot.documents) {
                    val caregiverId = doc.getString("caregiverId") ?: continue
                    val dateStr = doc.getString("date") ?: continue
                    val timeSlot = doc.getString("timeSlot") ?: continue
                    val status = doc.getString("status") ?: "available"
                    val appointmentUserId = doc.getString("userId")

                    if ((status.lowercase() in listOf("pending", "booked", "confirmed")) &&
                        appointmentUserId != currentUserId
                    ) continue

                    val job = coroutineScope.launch {
                        val caregiverDoc = db.collection("caregivers").document(caregiverId).get().await()
                        val caregiverUsername = caregiverDoc.getString("username") ?: ""
                        val caregiverLicense = caregiverDoc.getString("license") ?: ""
                        val caregiverAddress = caregiverDoc.getString("address") ?: ""
                        val caregiverMunicipality = caregiverAddress.split(",").getOrNull(2)?.trim() ?: ""

                        val appointment = AppointmentInfo(
                            id = doc.id,
                            caregiverId = caregiverId,
                            caregiverUsername = caregiverUsername,
                            license = caregiverLicense,
                            municipality = caregiverMunicipality,
                            date = dateStr,
                            timeSlot = timeSlot,
                            status = status.lowercase()
                        )
                        allAppointments.add(appointment)
                    }

                    jobs.add(job)
                }

                coroutineScope.launch {
                    jobs.joinAll()
                    applyFilter()
                    isLoading.value = false
                }
            }
    }

    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { userId ->
            fetchUserInfo(db, userId, username, pwdType, isLoading)
        }
        listenToAppointmentsRealtime()
    }

    if (isLoading.value) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("User Dashboard") })
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserInfoSection(username.value, pwdType.value, navController)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Available Caregivers", style = MaterialTheme.typography.titleLarge)
                    Row {
                        IconButton(onClick = { showFilterDialog.value = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                }

                if (selectedDate.value.isNotEmpty() || selectedTimeSlot.value.isNotEmpty() ||
                    selectedLicenseType.value.isNotEmpty() || selectedMunicipality.value.isNotEmpty()
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (selectedDate.value.isNotEmpty()) FilterChip(selectedDate.value) {
                            selectedDate.value = ""
                        }
                        if (selectedTimeSlot.value.isNotEmpty()) FilterChip(selectedTimeSlot.value) {
                            selectedTimeSlot.value = ""
                        }
                        if (selectedLicenseType.value.isNotEmpty()) FilterChip(selectedLicenseType.value) {
                            selectedLicenseType.value = ""
                        }
                        if (selectedMunicipality.value.isNotEmpty()) FilterChip(selectedMunicipality.value) {
                            selectedMunicipality.value = ""
                        }
                    }
                }

                if (availableCaregivers.isEmpty()) {
                    Text("No available caregivers at this time.", modifier = Modifier.padding(8.dp))
                } else {
                    availableCaregivers.forEach { appointment ->
                        AvailableCaregiverItem(appointment, navController)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (pendingBookings.isNotEmpty()) {
                    Text("Pending Bookings", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
                    pendingBookings.forEach { appointment ->
                        PendingBookingItem(appointment, navController) { id ->
                            coroutineScope.launch {
                                delay(2000)
                                val success = cancelBooking(id)
                                if (success) listenToAppointmentsRealtime()
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (confirmedAppointments.isNotEmpty()) {
                    Text("Your Appointments", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
                    confirmedAppointments.forEachIndexed { index, appointment ->
                        AppointmentItem(appointment, navController)
                        if (index < confirmedAppointments.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (showFilterDialog.value) {
                FilterDialog(
                    selectedDate,
                    selectedTimeSlot,
                    selectedLicenseType,
                    selectedMunicipality,
                    showTimeSlotDialog,
                    showLicenseDialog,
                    showMunicipalityDialog
                ) {
                    showFilterDialog.value = false
                    applyFilter()
                }
            }

            if (showTimeSlotDialog.value) {
                SimpleListDialog(
                    "Select Time Slot",
                    listOf("Morning", "Afternoon", "Night", "Whole Day"),
                    selectedTimeSlot
                ) {
                    showTimeSlotDialog.value = false
                }
            }

            if (showLicenseDialog.value) {
                SimpleListDialog(
                    "Select License",
                    listOf("Licensed", "No License"),
                    selectedLicenseType
                ) {
                    showLicenseDialog.value = false
                }
            }

            if (showMunicipalityDialog.value) {
                SimpleListDialog(
                    "Select Municipality",
                    listOf(
                        "Angeles", "Apalit", "Arayat", "Bacolor", "Candaba", "Floridablanca",
                        "Guagua", "Lubao", "Mabalacat", "Macabebe", "Magalang", "Masantol",
                        "Mexico", "Minalin", "Porac", "San Fernando", "San Luis"
                    ),
                    selectedMunicipality
                ) {
                    showMunicipalityDialog.value = false
                }
            }
        }
    }
}






@Composable
fun AvailableCaregiverItem(appointment: AppointmentInfo, navController: NavHostController) {
    Card(
        elevation = CardDefaults.cardElevation(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val encodedDate = URLEncoder.encode(appointment.date, StandardCharsets.UTF_8.toString())
                val encodedTimeSlot = URLEncoder.encode(appointment.timeSlot, StandardCharsets.UTF_8.toString())
                navController.navigate(
                    Screen.BookingScreen.createRoute(
                        appointmentId = appointment.id,
                        caregiverId = appointment.caregiverId, // ✅ CAREGIVER ID only!!
                        date = encodedDate,
                        timeSlot = encodedTimeSlot
                    )
                )
            }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Caregiver Username: ${appointment.caregiverUsername}")
            Text("License: ${appointment.license}")
            Text("Municipality: ${appointment.municipality}")
            Text("Date: ${appointment.date} (${appointment.timeSlot})")
        }
    }
}


@Composable
fun PendingBookingItem(
    appointment: AppointmentInfo,
    navController: NavHostController,
    onCancelBooking: (String) -> Unit
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Caregiver Username: ${appointment.caregiverUsername}")
            Text("License: ${appointment.license}")
            Text("Municipality: ${appointment.municipality}")
            Text("Date: ${appointment.date} (${appointment.timeSlot})")

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pending Booking...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = {
                        navController.navigate(Screen.ChatScreen.createRoute(appointment.id, "user"))
                    },
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Chat")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showConfirmationDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel Booking")
            }
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmationDialog = false
                    onCancelBooking(appointment.id)
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("No")
                }
            },
            title = { Text("Cancel Booking") },
            text = { Text("Are you sure you want to cancel this booking?") }
        )
    }
}


suspend fun cancelBooking(appointmentId: String): Boolean {
    return try {
        val db = FirebaseFirestore.getInstance()
        db.collection("appointments")
            .document(appointmentId)
            .update("status", "Available")
            .await()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}




@Composable
fun AppointmentItem(appointment: AppointmentInfo, navController: NavHostController) {
    Card(
        elevation = CardDefaults.cardElevation(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Caregiver Username: ${appointment.caregiverUsername}")
            Text("License: ${appointment.license}")
            Text("Municipality: ${appointment.municipality}")
            Text("Date: ${appointment.date} (${appointment.timeSlot})")

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        navController.navigate(Screen.ChatScreen.createRoute(appointment.id, "user"))
                    }
                ) {
                    Text("Chat")
                }
            }
        }
    }
}



@Composable
fun UserInfoSection(
    username: String,
    pwdType: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        userId?.let {
            firestore.collection("users").document(it).get().addOnSuccessListener { doc ->
                profileImageUrl = doc.getString("profileImageUrl")
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("profile") },
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (profileImageUrl != null)
                    rememberAsyncImagePainter(profileImageUrl)
                else
                    rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text("Username: @$username", style = MaterialTheme.typography.titleMedium)
                Text("PWD Type: $pwdType", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}





@Composable
fun FilterChip(label: String, onRemove: () -> Unit) {
    AssistChip(
        onClick = {},
        label = { Text(label, fontSize = 12.sp) },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    )
}

@Composable
fun FilterDialog(
    selectedDate: MutableState<String>,
    selectedTimeSlot: MutableState<String>,
    selectedLicenseType: MutableState<String>,
    selectedMunicipality: MutableState<String>,
    showTimeSlotDialog: MutableState<Boolean>,
    showLicenseDialog: MutableState<Boolean>,
    showMunicipalityDialog: MutableState<Boolean>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Apply") }
        },
        title = { Text("Select Filters") },
        text = {
            Column {
                Button(
                    onClick = {
                        showDatePickerDialog(context) { date ->
                            selectedDate.value = date
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(if (selectedDate.value.isEmpty()) "Select Date" else selectedDate.value)
                }

                Button(
                    onClick = {
                        showTimeSlotDialog.value = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(if (selectedTimeSlot.value.isEmpty()) "Select Time Slot" else selectedTimeSlot.value)
                }

                Button(
                    onClick = {
                        showLicenseDialog.value = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(if (selectedLicenseType.value.isEmpty()) "Select License" else selectedLicenseType.value)
                }

                Button(
                    onClick = {
                        showMunicipalityDialog.value = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(if (selectedMunicipality.value.isEmpty()) "Select Municipality" else selectedMunicipality.value)
                }
            }
        }
    )
}


@Composable
fun SimpleListDialog(
    title: String,
    options: List<String>,
    selectedOption: MutableState<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(title) },
        text = {
            Box(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    options.forEach { option ->
                        Button(
                            onClick = {
                                selectedOption.value = option
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(option)
                        }
                    }
                }
            }
        }
    )
}
