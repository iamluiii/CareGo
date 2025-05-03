package com.example.carego.screens.caregiver.mainscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberDismissState
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
import androidx.compose.ui.graphics.Color
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
import com.example.carego.screens.user.mainscreen.extractMunicipality
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CareGiverMainScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val savedAvailability = remember { mutableStateListOf<Appointment>() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var licenseType by remember { mutableStateOf("") }
    val confirmedAppointments = remember { mutableStateListOf<Appointment>() } // ⬅️ you forgot to declare this!
    val pendingAppointments = remember { mutableStateListOf<Appointment>() }
    val ongoingAppointments = remember { mutableStateListOf<Appointment>() }

    var showEditDialogForDate by remember { mutableStateOf<String?>(null) }
    var currentEditTimes by remember { mutableStateOf<List<String>>(emptyList()) }
    var showAddAvailabilityDialog by remember { mutableStateOf(false) }

    val hasLoadedProfile = remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null && !hasLoadedProfile.value) {
            hasLoadedProfile.value = true
            loadCaregiverProfile(
                onProfileLoaded = { loadedUsername, loadedFullName, loadedLicenseType ->
                    username = loadedUsername
                    fullName = loadedFullName
                    licenseType = loadedLicenseType
                }
            )
        }
    }
    val hasLoadedAppointments = remember { mutableStateOf(false) }
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

                    val jobs = mutableListOf<Job>()


                    coroutineScope.launch {
                        val jobs = snapshot.documents.map { doc ->
                            launch {
                                val status = doc.getString("status") ?: ""
                                val userId = doc.getString("userId") ?: ""
                                val caregiverUsername = doc.getString("caregiverUsername") ?: ""

                                val username = if (status.lowercase() == "pending" && userId.isNotEmpty()) {
                                    try {
                                        val userDoc = db.collection("users").document(userId).get().await()
                                        userDoc.getString("username") ?: "Unknown"
                                    } catch (e: Exception) {
                                        "Unknown"
                                    }
                                } else {
                                    caregiverUsername
                                }

                                val appointment = Appointment(
                                    id = doc.id,
                                    caregiverId = doc.getString("caregiverId") ?: "",
                                    userId = userId,
                                    username = username,
                                    date = doc.getString("date") ?: "",
                                    timeSlot = doc.getString("timeSlot") ?: "",
                                    status = status,
                                    municipality = doc.getString("municipality") ?: "",
                                    pwdType = doc.getString("pwdType") ?: ""
                                )

                                when (status.lowercase()) {
                                    "available" -> {
                                        if (savedAvailability.none { it.id == appointment.id }) {
                                            savedAvailability.add(appointment)
                                        }
                                    }
                                    "pending" -> pendingAppointments.add(appointment)

                                    "confirmed" -> {
                                        try {
                                            val userDoc = db.collection("users").document(appointment.userId).get().await()
                                            val firstName = userDoc.getString("firstName") ?: ""
                                            val lastName = userDoc.getString("lastName") ?: ""
                                            val middleName = userDoc.getString("middleName") ?: ""
                                            val fullName = listOf(lastName, firstName, middleName)
                                                .filter { it.isNotEmpty() && it.lowercase() != "null" }
                                                .joinToString(" ")

                                            val address = userDoc.getString("address") ?: ""
                                            val enriched = appointment.copy(
                                                patientName = fullName,
                                                location = address,
                                                time = appointment.timeSlot
                                            )

                                            if (confirmedAppointments.none {
                                                    it.date == enriched.date &&
                                                            it.time == enriched.time &&
                                                            it.patientName == enriched.patientName
                                                }) {
                                                confirmedAppointments.add(enriched)
                                            }
                                        } catch (_: Exception) {}
                                    }

                                    "ongoing" -> {
                                        try {
                                            val userDoc = db.collection("users").document(appointment.userId).get().await()
                                            val firstName = userDoc.getString("firstName") ?: ""
                                            val lastName = userDoc.getString("lastName") ?: ""
                                            val middleName = userDoc.getString("middleName") ?: ""
                                            val fullName = listOf(lastName, firstName, middleName)
                                                .filter { it.isNotEmpty() && it.lowercase() != "null" }
                                                .joinToString(" ")

                                            val address = userDoc.getString("address") ?: ""
                                            val enriched = appointment.copy(
                                                patientName = fullName,
                                                location = address,
                                                time = appointment.timeSlot
                                            )

                                            if (ongoingAppointments.none {
                                                    it.date == enriched.date &&
                                                            it.time == enriched.time &&
                                                            it.patientName == enriched.patientName
                                                }) {
                                                ongoingAppointments.add(enriched)
                                            }
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                        }

                        jobs.joinAll() // wait for all launched coroutines to finish
                    }

                    coroutineScope.launch {
                        jobs.joinAll()
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
        )
{

            CareGiverProfileRow(
                username = username,
                licenseType = licenseType,
                navController = navController
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showAddAvailabilityDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set Availability")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Available Dates:", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

            if (savedAvailability.isNotEmpty()) {
                val uniqueAvailability = savedAvailability
                    .distinctBy { it.id }
                    .sortedBy { it.date }

                uniqueAvailability.forEach { appointment ->
                    var showDeleteConfirm by remember { mutableStateOf(false) }

                    val dismissState = rememberDismissState(
                        confirmStateChange = { dismissValue ->
                            if (dismissValue == DismissValue.DismissedToStart) {
                                showDeleteConfirm = true
                            }
                            false
                        }
                    )

                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(DismissDirection.EndToStart),
                        background = { DismissBackground(dismissState) },
                        dismissContent = {
                            AvailabilityCard(appointment.date, appointment.timeSlot)
                        }
                    )

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = {
                                showDeleteConfirm = false
                                coroutineScope.launch { dismissState.reset() } // ✅ reset swipe
                            },
                            title = { Text("Delete Availability") },
                            text = { Text("Are you sure you want to delete ${appointment.date} - ${appointment.timeSlot}?") },
                            confirmButton = {
                                Button(onClick = {
                                    FirebaseFirestore.getInstance()
                                        .collection("appointments")
                                        .document(appointment.id)
                                        .delete()
                                    savedAvailability.remove(appointment)
                                    showDeleteConfirm = false
                                    coroutineScope.launch { dismissState.reset() } // ✅ reset swipe after delete too
                                }) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = {
                                    showDeleteConfirm = false
                                    coroutineScope.launch { dismissState.reset() } // ✅ reset swipe
                                }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (pendingAppointments.isNotEmpty()) {
                Text("Pending Bookings", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

                pendingAppointments.forEach { appointment ->
                    PendingBookingItem(appointment, navController)
                }
            }

    UpcomingAppointmentsList(confirmedAppointments, navController)
    OngoingAppointmentsList(ongoingAppointments, navController)


    Spacer(modifier = Modifier.height(32.dp))
        }
    }


    if (showAddAvailabilityDialog) {
        AvailabilityDatePicker(
            savedAvailability = savedAvailability,
            onDateSaved = {
                showAddAvailabilityDialog = false

            }
        )
    }


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

    Text(
        text = "Upcoming Appointments",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.SansSerif
    )

    if (appointments.isEmpty()) {
        Text(
            text = "No appointments yet.",
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
            fontFamily = FontFamily.SansSerif
        )
    } else {
        appointments.forEach { appointment ->
            var profileImageUrl by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(appointment.userId) {
                db.collection("users").document(appointment.userId)
                    .get()
                    .addOnSuccessListener { doc ->
                        profileImageUrl = doc.getString("profileImageUrl")
                    }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
                        Text("Username: ${appointment.username}", fontFamily = FontFamily.SansSerif)
                        Text("Date: ${appointment.date}", fontFamily = FontFamily.SansSerif)
                        Text("Time: ${appointment.timeSlot}", fontFamily = FontFamily.SansSerif)
                    }
                    Button(onClick = {
                        navController.navigate(Screen.ChatScreen.createRoute(appointment.id, "caregiver"))
                    }) {
                        Text("Chat")
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
            FirebaseFirestore.getInstance().collection("caregivers").document(it)
                .get()
                .addOnSuccessListener { doc ->
                    profileImageUrl = doc.getString("profileImageUrl")
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
                Text("Username: @$username", style = MaterialTheme.typography.titleMedium)
                Text("License Type: $licenseType", style = MaterialTheme.typography.bodyMedium)
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


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DismissBackground(dismissState: DismissState) {
    val direction = dismissState.dismissDirection ?: return
    if (direction != DismissDirection.EndToStart) return // ⬅️ only show for delete

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFCDD2))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(Icons.Default.Delete, contentDescription = null)
    }
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
    var municipality by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(appointment.userId) {
        if (appointment.userId.isNotEmpty()) {
            db.collection("users").document(appointment.userId)
                .get()
                .addOnSuccessListener { document ->
                    pwdType = document.getString("pwdType") ?: ""
                    val address = document.getString("address") ?: ""
                    municipality = extractMunicipality(address)
                    profileImageUrl = document.getString("profileImageUrl")
                }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUrl ?: R.drawable.defaultprofileicon),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Username: @${appointment.username}", fontFamily = FontFamily.SansSerif)
            }

            Text("PWD Type: $pwdType", fontFamily = FontFamily.SansSerif)
            Text("Municipality: $municipality", fontFamily = FontFamily.SansSerif)
            Text("Date: ${appointment.date} | Time: ${appointment.timeSlot}", fontFamily = FontFamily.SansSerif)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        navController.navigate(Screen.ChatScreen.createRoute(appointment.id, "caregiver"))
                    },
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Chat")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
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
fun OngoingAppointmentsList(appointments: List<Appointment>, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Text(
        text = "Ongoing Appointments",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.SansSerif,
        modifier = Modifier.padding(top = 16.dp)
    )

    if (appointments.isEmpty()) {
        Text(
            text = "No ongoing appointments yet.",
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
            fontFamily = FontFamily.SansSerif
        )
    } else {
        appointments.forEach { appointment ->
            var profileImageUrl by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(appointment.userId) {
                db.collection("users").document(appointment.userId)
                    .get()
                    .addOnSuccessListener { doc ->
                        profileImageUrl = doc.getString("profileImageUrl")
                    }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
                        Text("Username: ${appointment.username}", fontFamily = FontFamily.SansSerif)
                        Text("Date: ${appointment.date}", fontFamily = FontFamily.SansSerif)
                        Text("Time: ${appointment.timeSlot}", fontFamily = FontFamily.SansSerif)
                    }

                    Button(onClick = {
                        navController.navigate(Screen.ChatScreen.createRoute(appointment.id, "caregiver"))
                    }) {
                        Text("Chat")
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

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

