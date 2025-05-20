package com.example.carego.screens.caregiver.mainscreen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.carego.R
import com.example.carego.data.Appointment
import com.example.carego.navigation.Screen
import com.example.carego.screens.availabilityscreen.AvailabilityDatePicker
import com.example.carego.screens.user.mainscreen.BottomNavItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CareGiverMainScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val savedAvailability = remember { mutableStateListOf<Appointment>() }
    val confirmedAppointments = remember { mutableStateListOf<Appointment>() }
    val pendingAppointments = remember { mutableStateListOf<Appointment>() }
    val ongoingAppointments = remember { mutableStateListOf<Appointment>() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var licenseType by remember { mutableStateOf("") }
    var isVerified by remember { mutableStateOf(false) }
    var isVerificationPending by remember { mutableStateOf(false) }
    var showAddAvailabilityDialog by remember { mutableStateOf(false) }
    var showEditDialogForDate by remember { mutableStateOf<String?>(null) }
    var currentEditTimes by remember { mutableStateOf<List<String>>(emptyList()) }


    val hasLoadedProfile = remember { mutableStateOf(false) }
    val hasLoadedAppointments = remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(currentUserId) {
        if (currentUserId != null && !hasLoadedProfile.value) {
            hasLoadedProfile.value = true

            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("caregivers").document(currentUserId).get().await()

                username = snapshot.getString("username") ?: "Unknown"
                fullName = snapshot.getString("fullName") ?: "Unknown"
                licenseType = snapshot.getString("license") ?: "Unknown"
                isVerified = snapshot.getString("verificationStatus") == "verified"
                isVerificationPending = snapshot.getString("verificationStatus") == "pending"

            } catch (e: Exception) {
                // ✅ Use Main Dispatcher to show Toast
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load profile.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null && !hasLoadedAppointments.value) {
            hasLoadedAppointments.value = true
            val db = FirebaseFirestore.getInstance()

            db.collection("appointments")
                .whereEqualTo("caregiverId", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    savedAvailability.clear()
                    pendingAppointments.clear()
                    confirmedAppointments.clear()
                    ongoingAppointments.clear()

                    snapshot.documents.forEach { doc ->
                        val userId = doc.getString("userId") ?: ""
                        val appointment = Appointment(
                            id = doc.id,
                            caregiverId = doc.getString("caregiverId") ?: "",
                            userId = userId,
                            username = doc.getString("username") ?: "Unknown",
                            date = doc.getString("date") ?: "",
                            timeSlot = doc.getString("timeSlot") ?: "",
                            status = doc.getString("status") ?: "",
                            municipality = doc.getString("municipality") ?: "",
                            pwdType = doc.getString("pwdType") ?: ""
                        )




                        if (appointment.status.lowercase() in listOf("confirmed", "ongoing") && userId.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    val userDoc = db.collection("users").document(userId).get().await()
                                    val updatedUsername = userDoc.getString("username") ?: "Unknown"
                                    val updatedPwdType = userDoc.getString("pwdType") ?: "Unknown"

                                    // Update the appointment in place without duplicating
                                    val updatedAppointment = appointment.copy(
                                        username = updatedUsername,
                                        pwdType = updatedPwdType
                                    )

                                    if (appointment.status.lowercase() == "confirmed") {
                                        confirmedAppointments.removeIf { it.id == appointment.id }
                                        confirmedAppointments.add(updatedAppointment)
                                    }

                                    if (appointment.status.lowercase() == "ongoing") {
                                        ongoingAppointments.removeIf { it.id == appointment.id }
                                        ongoingAppointments.add(updatedAppointment)
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        when (appointment.status.lowercase()) {
                            "available" -> savedAvailability.add(appointment)
                            "pending" -> pendingAppointments.add(appointment)
                            "confirmed" -> confirmedAppointments.add(appointment)
                            "ongoing" -> ongoingAppointments.add(appointment)
                        }
                    }
                }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caregiver Dashboard") }
            )
        },
        bottomBar = {
            CareGiverBottomBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card
            CareGiverProfileRow(username = username, licenseType = licenseType, navController = navController)

            Spacer(modifier = Modifier.height(24.dp))

            // Verification Card

            if (!isVerified) {
                if (isVerificationPending) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Verification is Pending",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    CareGiverVerificationCard(navController)
                }

                // ⛔️ Instead of returning, just stop the rest of the content
                return@Column
            }

            // Availability Button
            Button(
                onClick = { showAddAvailabilityDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set Availability")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Available Dates Section
            Text("Your Available Dates:", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

            if (savedAvailability.isNotEmpty()) {
                savedAvailability
                    .distinctBy { it.id }
                    .sortedBy { it.date }
                    .forEach { appointment ->
                        AvailabilityCard(appointment.date, appointment.timeSlot)
                    }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pending Bookings Section
            if (pendingAppointments.isNotEmpty()) {
                Text("Pending Bookings", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

                pendingAppointments.forEach { appointment ->
                    PendingBookingItem(appointment, navController)
                }
            }
// Upcoming Appointments Section
            // Upcoming Appointments Section
            if (confirmedAppointments.isNotEmpty()) {
                UpcomingAppointmentsList(appointments = confirmedAppointments, navController = navController)
            }


            // Ongoing Appointments Section
            OngoingAppointmentsList(
                appointments = ongoingAppointments,
                navController = navController,
            )


            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Show Add Availability Dialog
    if (showAddAvailabilityDialog) {
        AvailabilityDatePicker(
            savedAvailability = savedAvailability,
            onDateSaved = {
                showAddAvailabilityDialog = false
            }
        )
    }

    // Show Edit Availability Dialog
    showEditDialogForDate?.let { date ->
        EditAvailabilityDialog(
            date = date,
            currentTimes = currentEditTimes,
            onDismiss = { showEditDialogForDate = null },
            onConfirm = { newTimes ->
                updateAvailability(date, currentEditTimes, newTimes, savedAvailability)
                showEditDialogForDate = null
            }
        )
    }
}





@Composable
fun UpcomingAppointmentsList(appointments: List<Appointment>, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Text(
        text = "Upcoming Appointments",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp)
    )

    if (appointments.isEmpty()) {
        Text(
            text = "No upcoming appointments yet.",
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    } else {
        appointments.forEach { appointment ->
            var profileImageUrl by remember { mutableStateOf<String?>(null) }
            var username by remember { mutableStateOf("Loading...") }
            var pwdType by remember { mutableStateOf("") }
            var showStartDialog by remember { mutableStateOf(false) }

            LaunchedEffect(appointment.userId) {
                try {
                    val userDoc = db.collection("users").document(appointment.userId).get().await()
                    profileImageUrl = userDoc.getString("profileImageUrl")
                    username = userDoc.getString("username") ?: "Unknown"
                    pwdType = userDoc.getString("pwdType") ?: "Unknown"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = if (profileImageUrl != null)
                                rememberAsyncImagePainter(profileImageUrl)
                            else
                                rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(username, style = MaterialTheme.typography.titleMedium)
                            Text(pwdType, style = MaterialTheme.typography.bodyMedium)
                            Text(appointment.date, style = MaterialTheme.typography.bodyMedium)
                            Text(appointment.timeSlot, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                navController.navigate(Screen.ChatScreen.createRoute(appointment.id, "caregiver"))
                            }
                        ) {
                            Text("Chat")
                        }
                        Button(
                            onClick = {
                                showStartDialog = true
                            }
                        ) {
                            Text("Start")
                        }
                    }

                    if (showStartDialog) {
                        AlertDialog(
                            onDismissRequest = { showStartDialog = false },
                            title = { Text("Start Appointment") },
                            text = { Text("Are you sure you want to start this appointment?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                db.collection("appointments")
                                                    .document(appointment.id)
                                                    .update("status", "Ongoing")
                                                    .await()

                                                // Show a confirmation toast
                                                Toast.makeText(context, "Appointment started.", Toast.LENGTH_SHORT).show()

                                                // Refresh the appointment list
                                                showStartDialog = false
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                Toast.makeText(context, "Failed to start appointment. Try again.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ) {
                                    Text("Yes, Start")
                                }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showStartDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun CareGiverProfileRow(username: String, licenseType: String, navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        userId?.let {
            try {
                val doc = FirebaseFirestore.getInstance().collection("caregivers").document(it).get().await()
                profileImageUrl = doc.getString("profileImageUrl")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("caregiver_profile") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (profileImageUrl != null)
                    rememberAsyncImagePainter(profileImageUrl)
                else
                    rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Username: @$username", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("License Type: $licenseType", fontSize = 14.sp)
            }
        }
    }
}


@Composable
fun EditAvailabilityDialog(
    date: String,
    currentTimes: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val allSlots = listOf("Morning", "Afternoon", "Night", "Whole Day")
    var selectedSlots by remember { mutableStateOf(currentTimes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val result = if ("Whole Day" in selectedSlots) listOf("Whole Day")
                else selectedSlots
                onConfirm(result)
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Edit Availability for $date") },
        text = {
            Column {
                allSlots.forEach { slot ->
                    val isWholeDaySelected = selectedSlots.contains("Whole Day")
                    val isOtherSelected = selectedSlots.any { it != "Whole Day" }

                    val enabled = when (slot) {
                        "Whole Day" -> !isOtherSelected
                        else -> !isWholeDaySelected
                    }

                    val isChecked = selectedSlots.contains(slot)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .toggleable(
                                value = isChecked,
                                enabled = enabled,
                                onValueChange = { checked ->
                                    selectedSlots = if (checked) {
                                        if (slot == "Whole Day") listOf("Whole Day")
                                        else selectedSlots + slot
                                    } else {
                                        selectedSlots - slot
                                    }
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = null,
                            enabled = enabled,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(slot)
                    }
                }
            }
        }
    )
}

@Composable
fun AvailabilityCard(date: String, displayTime: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("$date - $displayTime", fontFamily = FontFamily.SansSerif)
        }
    }
}

@Composable
fun PendingBookingItem(appointment: Appointment, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var pwdType by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf("Loading...") }

    // Fetch user details
    LaunchedEffect(appointment.userId) {
        if (appointment.userId.isNotEmpty()) {
            try {
                val userDoc = db.collection("users").document(appointment.userId).get().await()
                username = userDoc.getString("username") ?: "Unknown"
                pwdType = userDoc.getString("pwdType") ?: ""
                profileImageUrl = userDoc.getString("profileImageUrl")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = if (profileImageUrl != null)
                    rememberAsyncImagePainter(profileImageUrl)
                else
                    rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(username, fontFamily = FontFamily.SansSerif)
                Text(pwdType, fontFamily = FontFamily.SansSerif)
                Text(appointment.date, fontFamily = FontFamily.SansSerif)
                Text(appointment.timeSlot, fontFamily = FontFamily.SansSerif)
            }
            Button(
                onClick = {
                    navController.navigate(Screen.ChatScreen.createRoute(appointment.id, "caregiver"))
                },
                modifier = Modifier.height(36.dp)
            ) {
                Text("Chat")
            }
        }
    }
}


fun updateAvailability(
    date: String,
    currentTimes: List<String>,
    newTimes: List<String>,
    savedAvailability: SnapshotStateList<Appointment>
) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // Remove old time slots
    val toDelete = savedAvailability.filter {
        it.caregiverId == currentUserId && it.date == date && currentTimes.contains(it.timeSlot)
    }
    toDelete.forEach {
        db.collection("appointments").document(it.id).delete()
        savedAvailability.remove(it)
    }

    // Add new time slots only if they don't exist — confirmed from Firestore directly
    newTimes.forEach { timeSlot ->
        val existsInState = savedAvailability.any {
            it.caregiverId == currentUserId && it.date == date && it.timeSlot == timeSlot
        }

        if (!existsInState) {
            // Defensive Firestore query to double-check
            db.collection("appointments")
                .whereEqualTo("caregiverId", currentUserId)
                .whereEqualTo("date", date)
                .whereEqualTo("timeSlot", timeSlot)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        val newDoc = db.collection("appointments").document()
                        val newAppointment = Appointment(
                            id = newDoc.id,
                            caregiverId = currentUserId,
                            userId = "",
                            username = "",
                            date = date,
                            timeSlot = timeSlot,
                            status = "available",
                            municipality = "",
                            pwdType = ""
                        )
                        newDoc.set(newAppointment)
                        savedAvailability.add(newAppointment)
                    }
                }
        }
    }
}


@Composable
fun OngoingAppointmentsList(
    appointments: List<Appointment>,
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Text(
        text = "Ongoing Appointments",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    if (appointments.isEmpty()) {
        Text(
            text = "No ongoing appointments yet.",
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    } else {
        appointments.forEach { appointment ->
            var profileImageUrl by remember { mutableStateOf<String?>(null) }
            var username by remember { mutableStateOf("Loading...") }
            var pwdType by remember { mutableStateOf("") }

            LaunchedEffect(appointment.userId) {
                try {
                    val userDoc = db.collection("users").document(appointment.userId).get().await()
                    profileImageUrl = userDoc.getString("profileImageUrl")
                    username = userDoc.getString("username") ?: "Unknown"
                    pwdType = userDoc.getString("pwdType") ?: "Unknown"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = if (profileImageUrl != null)
                                rememberAsyncImagePainter(profileImageUrl)
                            else
                                rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(username, style = MaterialTheme.typography.titleMedium)
                            Text(pwdType, style = MaterialTheme.typography.bodyMedium)
                            Text(appointment.date, style = MaterialTheme.typography.bodyMedium)
                            Text(appointment.timeSlot, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                navController.navigate(Screen.ChatScreen.createRoute(appointment.id, "caregiver"))
                            }
                        ) {
                            Text("Chat")
                        }

                        // ✅ Mark as Done Button (No Feedback Logic Here)
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        db.collection("appointments")
                                            .document(appointment.id)
                                            .update("status", "done")
                                            .await()

                                        Toast.makeText(context, "Appointment marked as done.", Toast.LENGTH_SHORT).show()

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "Failed to mark as done. Try again.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Text("Mark as Done")
                        }
                    }
                }
            }
        }
    }
}





@Composable
fun CareGiverBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, Screen.CareGiverMainScreen.route),
        BottomNavItem("Chat", Icons.AutoMirrored.Filled.Chat, "chat_history"),
        BottomNavItem("Transactions", Icons.AutoMirrored.Filled.ReceiptLong, "transaction_history"),
        BottomNavItem("Settings", Icons.Default.Settings, "settings")
    )
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    androidx.compose.material3.NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Optional: pop up to main to avoid stacking
                            popUpTo("caregiver_main") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}
@Composable
fun CareGiverVerificationCard(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable { navController.navigate("caregiver_verification") },
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
                    navController.navigate("caregiver_verification")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload Valid ID")
            }
        }
    }
}


data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)
