package com.example.carego.screens.user.mainscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.carego.FeedbackPopup
import com.example.carego.LocationData
import com.example.carego.R
import com.example.carego.navigation.Screen
import com.example.carego.screens.caregiver.mainscreen.BottomNavItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    val isLoading = remember { mutableStateOf(true) }
    val username = remember { mutableStateOf("") }
    val pwdType = remember { mutableStateOf("") }
    val profileImageUrl = remember { mutableStateOf<String?>(null) }
    val allAppointments = remember { mutableStateListOf<AppointmentInfo>() }
    val availableCaregivers = remember { mutableStateListOf<AppointmentInfo>() }
    val pendingBookings = remember { mutableStateListOf<AppointmentInfo>() }
    val confirmedAppointments = remember { mutableStateListOf<AppointmentInfo>() }
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val selectedDate = remember { mutableStateOf("") }
    val selectedTimeSlot = remember { mutableStateOf("") }
    val selectedLicenseType = remember { mutableStateOf("Any") }
    val selectedMunicipality = remember { mutableStateOf("") }

    val showFilterDialog = remember { mutableStateOf(false) }
    val showTimeSlotDialog = remember { mutableStateOf(false) }
    val showMunicipalityDialog = remember { mutableStateOf(false) }
    val showVerificationCard = remember { mutableStateOf(false) }
    val showVerificationPendingCard = remember { mutableStateOf(false) }
    val showFeedbackPopup = remember { mutableStateOf(false) }
    val currentAppointmentId = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()


    if (showFeedbackPopup.value) {
        FeedbackPopup(
            appointmentId = currentAppointmentId.value,
            onDismiss = { showFeedbackPopup.value = false },
            onFeedbackSubmitted = { finishedAppointmentId ->
                allAppointments.removeAll { it.id == finishedAppointmentId }
                showFeedbackPopup.value = false
            }
        )
    }

    LaunchedEffect(Unit) {
        currentUserId?.let { userId ->
            listenToAppointmentsRealtime(
                db = db,
                currentUserId = userId,
                coroutineScope = coroutineScope,
                allAppointments = allAppointments,
                applyFilter = {
                    applyFilter(
                        allAppointments = allAppointments,
                        availableCaregivers = availableCaregivers,
                        pendingBookings = pendingBookings,
                        confirmedAppointments = confirmedAppointments,
                        selectedDate = selectedDate.value,
                        selectedTimeSlot = selectedTimeSlot.value,
                        selectedLicenseType = selectedLicenseType.value,
                        selectedMunicipality = selectedMunicipality.value
                    )
                },
                onDone = { isLoading.value = false }
            )
        }
    }
    val userType = remember { mutableStateOf("") }


    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            try {
                val userDoc = db.collection("users").document(userId).get().await()
                userType.value = userDoc.getString("userType") ?: "user"  // Default to "user" if not found
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            try {
                val userDoc = db.collection("users").document(userId).get().await()
                username.value = userDoc.getString("username") ?: "Unknown"
                pwdType.value = userDoc.getString("pwdType") ?: "Unknown"
                profileImageUrl.value = userDoc.getString("profileImageUrl")

                val verificationStatus = userDoc.getString("verificationStatus") ?: "not_verified"

                // Show cards based on verification status
                showVerificationCard.value = verificationStatus == "not_verified"
                // Correct variable name
                showVerificationPendingCard.value = verificationStatus == "pending"

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    // Main screen layout
    Scaffold(
        topBar = {
            TopAppBar(

                title = {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .fillMaxWidth()
                    ) {
                        // Spacer Above the Row
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))  // Adjust the height as needed

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(R.drawable.caregologo),
                                    contentDescription = "Carego Logo",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(2.dp, Color(0xFFed3782), CircleShape)
                                        .padding(4.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = Color(0xFFed3782), fontWeight = FontWeight.Bold, fontSize = 28.sp)) {
                                            append("Care")
                                        }
                                        withStyle(SpanStyle(color = Color(0xFF3e0ae2), fontWeight = FontWeight.Bold, fontSize = 28.sp)) {
                                            append("go ")
                                        }
                                        withStyle(SpanStyle(color = Color(0xFFed3782), fontWeight = FontWeight.Bold, fontSize = 28.sp)) {
                                            append("Dash")
                                        }
                                        withStyle(SpanStyle(color = Color(0xFF3e0ae2), fontWeight = FontWeight.Bold, fontSize = 28.sp)) {
                                            append("board")
                                        }
                                    }
                                )
                            }
                        }
                    }

                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.White, // ✅ Set background to white
                    titleContentColor = Color.Black // ✅ Set text color to black for contrast
                )
            )
        },
        bottomBar = {
            UserBottomBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal =10.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (showVerificationCard.value) {
                UserVerificationCard(navController)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showVerificationPendingCard.value) {
                VerificationPendingCard()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // User Info Section
            UserHeaderSection(
                username = username.value,
                pwdType = pwdType.value,
                profileImageUrl = profileImageUrl.value,
                navController = navController
            )

            Spacer(modifier = Modifier.height(16.dp))



            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Caregivers",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFed3782),
                    modifier = Modifier.padding(start = 0.dp)  // Align to the left
                )
                IconButton(
                    onClick = { showFilterDialog.value = true },
                    modifier = Modifier.padding(end = 0.dp)  // Align to the right
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color(0xFFed3782)  // Updated color
                    )
                }
            }

            // Active Filter Chips
            if (selectedDate.value.isNotEmpty() || selectedTimeSlot.value.isNotEmpty() ||
                selectedLicenseType.value != "Any" || selectedMunicipality.value.isNotEmpty()
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (selectedDate.value.isNotEmpty()) FilterChip(selectedDate.value) {
                        selectedDate.value = ""
                        applyFilter(
                            allAppointments,
                            availableCaregivers,
                            pendingBookings,
                            confirmedAppointments,
                            selectedDate.value,
                            selectedTimeSlot.value,
                            selectedLicenseType.value,
                            selectedMunicipality.value
                        )
                    }
                    if (selectedTimeSlot.value.isNotEmpty()) FilterChip(selectedTimeSlot.value) {
                        selectedTimeSlot.value = ""
                        applyFilter(
                            allAppointments,
                            availableCaregivers,
                            pendingBookings,
                            confirmedAppointments,
                            selectedDate.value,
                            selectedTimeSlot.value,
                            selectedLicenseType.value,
                            selectedMunicipality.value
                        )
                    }
                    if (selectedLicenseType.value != "Any") FilterChip(selectedLicenseType.value) {
                        selectedLicenseType.value = "Any"
                        applyFilter(
                            allAppointments,
                            availableCaregivers,
                            pendingBookings,
                            confirmedAppointments,
                            selectedDate.value,
                            selectedTimeSlot.value,
                            selectedLicenseType.value,
                            selectedMunicipality.value
                        )
                    }
                    if (selectedMunicipality.value.isNotEmpty()) FilterChip(selectedMunicipality.value) {
                        selectedMunicipality.value = ""
                        applyFilter(
                            allAppointments,
                            availableCaregivers,
                            pendingBookings,
                            confirmedAppointments,
                            selectedDate.value,
                            selectedTimeSlot.value,
                            selectedLicenseType.value,
                            selectedMunicipality.value
                        )
                    }
                }
            }




// Sort available caregivers by latest to oldest
            val sortedAvailableCaregivers = availableCaregivers.sortedByDescending {
                dateFormat.parse(it.date).time
            }

            if (sortedAvailableCaregivers.isEmpty()) {
                Text("No available caregivers at this time.", modifier = Modifier.padding(8.dp))
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    sortedAvailableCaregivers.forEach { appointment ->
                        AvailableCaregiverItem(appointment, navController)
                    }
                }
            }







            val userAppointments = allAppointments.filter {
                it.status in listOf("ongoing", "confirmed", "pending")
            }.sortedWith(compareBy(
                // First sort by status priority
                { when (it.status) {
                    "ongoing" -> 0
                    "confirmed" -> 1
                    "pending" -> 2
                    else -> 3
                }},
                // Then sort by date (latest to oldest)
                { -dateFormat.parse(it.date).time }
            ))


            if (userAppointments.isNotEmpty()) {
                Text(
                    text = "Your Appointments",
                    fontSize = 18.sp,
                    color = Color(0xFFed3782),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .align(Alignment.Start)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    userAppointments.forEach { appointment ->
                        AppointmentItem(appointment, navController)
                    }
                }
            }

            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

// Filter and sort finished appointments
            val sortedFinishedAppointments = allAppointments.filter {
                it.status == "finished"
            }.sortedByDescending {
                dateFormat.parse(it.date).time
            }

            if (sortedFinishedAppointments.isNotEmpty()) {
                Text(
                    text = "Feedback",
                    fontSize = 18.sp,
                    color = Color(0xFFed3782),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .align(Alignment.Start)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    sortedFinishedAppointments.forEach { appointment ->
                        FeedbackAppointmentItem(
                            appointment = appointment,
                            showFeedbackPopup = showFeedbackPopup,
                            currentAppointmentId = currentAppointmentId
                        )
                    }
                }
            }
            }









            Spacer(modifier = Modifier.height(32.dp))
        }


        // Filter Dialogs
        if (showFilterDialog.value) {
            FilterDialog(
                selectedDate = selectedDate,
                selectedTimeSlot = selectedTimeSlot,
                selectedLicenseType = selectedLicenseType,
                selectedMunicipality = selectedMunicipality,
                showTimeSlotDialog = showTimeSlotDialog,
                showMunicipalityDialog = showMunicipalityDialog,
                onDismiss = {
                    showFilterDialog.value = false
                    applyFilter(
                        allAppointments,
                        availableCaregivers,
                        pendingBookings,
                        confirmedAppointments,
                        selectedDate.value,
                        selectedTimeSlot.value,
                        selectedLicenseType.value,
                        selectedMunicipality.value
                    )
                }
            )
        }

        if (showTimeSlotDialog.value) {
            AlertDialog(
                onDismissRequest = { showTimeSlotDialog.value = false },
                confirmButton = {
                    TextButton(onClick = { showTimeSlotDialog.value = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Select Time Slot") },
                text = {
                    Column {
                        listOf("Morning", "Afternoon", "Evening", "Night").forEach { slot ->
                            Text(
                                text = slot,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTimeSlot.value = slot
                                        showTimeSlotDialog.value = false
                                    }
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            )
                        }
                    }
                }
            )
        }

    }


fun applyFilter(
    allAppointments: SnapshotStateList<AppointmentInfo>,
    availableCaregivers: SnapshotStateList<AppointmentInfo>,
    pendingBookings: SnapshotStateList<AppointmentInfo>,
    confirmedAppointments: SnapshotStateList<AppointmentInfo>,
    selectedDate: String,
    selectedTimeSlot: String,
    selectedLicenseType: String,
    selectedMunicipality: String
) {
    availableCaregivers.clear()
    pendingBookings.clear()
    confirmedAppointments.clear()

    for (appointment in allAppointments) {
        when (appointment.status.lowercase()) {
            "available" -> {
                if ((selectedDate.isEmpty() || appointment.date == selectedDate) &&
                    (selectedTimeSlot.isEmpty() || appointment.timeSlot == selectedTimeSlot) &&
                    (selectedLicenseType == "Any" || appointment.license.isNotEmpty()) &&
                    (selectedMunicipality.isEmpty() || appointment.municipality == selectedMunicipality)
                ) {
                    availableCaregivers.add(appointment)
                }
            }
            "pending" -> pendingBookings.add(appointment)
            "confirmed", "ongoing" -> confirmedAppointments.add(appointment)
        }
    }
}





suspend fun listenToAppointmentsRealtime(
    db: FirebaseFirestore,
    currentUserId: String,
    coroutineScope: CoroutineScope,
    allAppointments: SnapshotStateList<AppointmentInfo>,
    applyFilter: () -> Unit,
    onDone: () -> Unit = {}
) {
    db.collection("appointments")
        .addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                onDone()
                return@addSnapshotListener
            }

            allAppointments.clear()
            val jobs = mutableListOf<Job>()

            for (doc in snapshot.documents) {
                val caregiverId = doc.getString("caregiverId") ?: continue
                val dateStr = doc.getString("date") ?: continue
                val timeSlot = doc.getString("timeSlot") ?: continue
                val status = doc.getString("status") ?: "available"
                val appointmentUserId = doc.getString("userId") ?: ""

                // Fetch salary rate
                val salaryRate = doc.getLong("salaryRate")?.toInt()?.toString() ?: "Not Set"

                // Only include this user's bookings or available appointments
                if (status.lowercase() == "available" || appointmentUserId == currentUserId) {
                    val job = coroutineScope.launch {
                        try {
                            val caregiverDoc = db.collection("caregivers").document(caregiverId).get().await()
                            val caregiverUsername = caregiverDoc.getString("username") ?: "Unknown"
                            val caregiverLicense = caregiverDoc.getString("license") ?: "Unknown"
                            val caregiverAddress = caregiverDoc.getString("address") ?: ""
                            val caregiverMunicipality = caregiverAddress.split(",").getOrNull(2)?.trim() ?: ""

                            val appointment = AppointmentInfo(
                                id = doc.id,
                                caregiverId = caregiverId,
                                userId = appointmentUserId,
                                caregiverUsername = caregiverUsername,
                                license = caregiverLicense,
                                municipality = caregiverMunicipality,
                                date = dateStr,
                                timeSlot = timeSlot,
                                status = status.lowercase(),
                                salaryRate = "PHP $salaryRate" // ✅ Include salary rate
                            )
                            allAppointments.add(appointment)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    jobs.add(job)
                }
            }

            coroutineScope.launch {
                try {
                    jobs.joinAll()
                    applyFilter()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    onDone()
                }
            }
        }
}




suspend fun cancelBooking(appointmentId: String) {
    try {
        FirebaseFirestore.getInstance()
            .collection("appointments")
            .document(appointmentId)
            .update("status", "available")
            .await()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// ✅ User Verification Card
@Composable
fun UserVerificationCard(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable { navController.navigate("user_verification") },
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
                Text("Upload PWD ID")
            }
        }
    }
}




// ✅ Verification Pending Card
@Composable
fun VerificationPendingCard() {
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
}



@Composable
fun AvailableCaregiverItem(appointment: AppointmentInfo, navController: NavHostController) {
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var averageRating by remember { mutableStateOf("0.0") }
    var salaryRate by remember { mutableStateOf("Not Set") }
    var caregiverUsername by remember { mutableStateOf("@Loading...") }

    LaunchedEffect(appointment.caregiverId) {
        try {
            val db = FirebaseFirestore.getInstance()

            // Fetch caregiver profile picture and username
            val caregiverDoc = db.collection("caregivers")
                .document(appointment.caregiverId)
                .get()
                .await()

            profileImageUrl = caregiverDoc.getString("profileImageUrl")
            caregiverUsername = "@" + (caregiverDoc.getString("username") ?: "Unknown")

            // Fetch ratings for this caregiver
            val ratingsSnapshot = db.collection("appointments")
                .whereEqualTo("caregiverId", appointment.caregiverId)
                .whereIn("status", listOf("finished", "Finished", "FINISHED"))
                .get()
                .await()

            // Calculate average rating
            val ratings = ratingsSnapshot.documents.mapNotNull { doc ->
                doc.getLong("rating")?.toDouble()
            }

            averageRating = if (ratings.isNotEmpty()) {
                String.format("%.1f", ratings.average())
            } else {
                "0.0"
            }

            // Fetch salary for this specific appointment
            val appointmentDoc = db.collection("appointments").document(appointment.id).get().await()
            val salary = appointmentDoc.getLong("salaryRate")
            salaryRate = if (salary != null) {
                "PHP ${salary.toInt()}"
            } else {
                "Not Set"
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Available Caregiver Card
    Card(
        modifier = Modifier
            .width(175.dp)
            .padding(8.dp)
            .clickable {
                val encodedDate = URLEncoder.encode(appointment.date, StandardCharsets.UTF_8.toString())
                val encodedTimeSlot = URLEncoder.encode(appointment.timeSlot, StandardCharsets.UTF_8.toString())

                navController.navigate(
                    Screen.BookingScreen.createRoute(
                        appointmentId = appointment.id,
                        caregiverId = appointment.caregiverId,
                        date = encodedDate,
                        timeSlot = encodedTimeSlot
                    )
                )
            }
        ,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4102fb))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Star Rating (Centered)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("★", fontSize = 18.sp, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
                Text(
                    text = " $averageRating",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6bdbe0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Profile Image
            // Caregiver Profile Image with Proper Sizing
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFFed3782), CircleShape)
            ) {
                Image(
                    painter = if (profileImageUrl != null)
                        rememberAsyncImagePainter(profileImageUrl)
                    else
                        rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                    contentDescription = "Caregiver Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }


            // Username with @ symbol
            Text(
                text = caregiverUsername,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF6bdbe0),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // License
            Text(
                text = appointment.license,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6bdbe0),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Available Date
            Text(
                text = "${appointment.date} (${appointment.timeSlot})",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6bdbe0),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Salary
            Text(
                text = salaryRate,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6bdbe0),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp)
            )
        }
    }
}


@Composable
fun FeedbackAppointmentItem(
    appointment: AppointmentInfo,
    showFeedbackPopup: MutableState<Boolean>,
    currentAppointmentId: MutableState<String>
) {
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var caregiverUsername by remember { mutableStateOf("@Loading...") }
    var licenseType by remember { mutableStateOf("Loading...") }
    var salaryRate by remember { mutableStateOf("Not Set") }
    var averageRating by remember { mutableStateOf("0.0") }

    LaunchedEffect(appointment.caregiverId) {
        try {
            val db = FirebaseFirestore.getInstance()

            // Fetch caregiver profile picture and username
            val caregiverDoc = db.collection("caregivers")
                .document(appointment.caregiverId)
                .get()
                .await()

            profileImageUrl = caregiverDoc.getString("profileImageUrl")
            caregiverUsername = "@" + (caregiverDoc.getString("username") ?: "Unknown")
            licenseType = caregiverDoc.getString("license") ?: "Unknown"

            // Fetch ratings for this caregiver
            val ratingsSnapshot = db.collection("appointments")
                .whereEqualTo("caregiverId", appointment.caregiverId)
                .whereIn("status", listOf("finished", "Finished", "FINISHED"))
                .get()
                .await()

            // Calculate average rating
            val ratings = ratingsSnapshot.documents.mapNotNull { doc ->
                doc.getLong("rating")?.toDouble()
            }

            averageRating = if (ratings.isNotEmpty()) {
                String.format("%.1f", ratings.average())
            } else {
                "0.0"
            }

            // Fetch salary for this specific appointment
            val appointmentDoc = db.collection("appointments").document(appointment.id).get().await()
            val salary = appointmentDoc.getLong("salaryRate")
            salaryRate = if (salary != null) {
                "PHP ${salary.toInt()}"
            } else {
                "Not Set"
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Feedback Card
    Card(
        modifier = Modifier
            .width(175.dp)
            .padding(8.dp)
            .clickable {
                showFeedbackPopup.value = true
                currentAppointmentId.value = appointment.id
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4102fb))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Star Rating (Centered)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("★", fontSize = 18.sp, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
                Text(
                    text = " $averageRating",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6bdbe0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFFed3782), CircleShape)
            ) {
                Image(
                    painter = if (profileImageUrl != null)
                        rememberAsyncImagePainter(profileImageUrl)
                    else
                        rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                    contentDescription = "Caregiver Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }


            // Username with @ symbol
            Text(
                text = caregiverUsername,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF6bdbe0),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // License
            Text(
                text = licenseType,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6bdbe0),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Available Date
            Text(
                text = "${appointment.date} (${appointment.timeSlot})",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6bdbe0),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Salary
            Text(
                text = salaryRate,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6bdbe0),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp)
            )
        }
    }
}






@Composable
fun AppointmentItem(
    appointment: AppointmentInfo,
    navController: NavHostController
) {
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var caregiverUsername by remember { mutableStateOf("Loading...") }
    var licenseType by remember { mutableStateOf("Loading...") }
    var salaryRate by remember { mutableStateOf("Not Set") }
    var averageRating by remember { mutableStateOf("0.0") }

    LaunchedEffect(appointment.caregiverId) {
        try {
            val db = FirebaseFirestore.getInstance()

            // Fetch caregiver profile picture and license type
            val caregiverDoc = db.collection("caregivers")
                .document(appointment.caregiverId)
                .get()
                .await()

            profileImageUrl = caregiverDoc.getString("profileImageUrl")
            caregiverUsername = "@" + (caregiverDoc.getString("username") ?: "Unknown")
            licenseType = caregiverDoc.getString("license") ?: "Unknown"

            // Fetch ratings for this caregiver
            val ratingsSnapshot = db.collection("appointments")
                .whereEqualTo("caregiverId", appointment.caregiverId)
                .whereIn("status", listOf("finished", "Finished", "FINISHED"))
                .get()
                .await()

            // Calculate average rating
            val ratings = ratingsSnapshot.documents.mapNotNull { doc ->
                doc.getLong("rating")?.toDouble()
            }

            averageRating = if (ratings.isNotEmpty()) {
                String.format("%.1f", ratings.average())
            } else {
                "0.0"
            }

            // Fetch salary for this specific appointment
            val appointmentDoc = db.collection("appointments").document(appointment.id).get().await()
            val salary = appointmentDoc.getLong("salaryRate")
            salaryRate = if (salary != null) {
                "PHP ${salary.toInt()}"
            } else {
                "Not Set"
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Appointment Card
    Card(
        modifier = Modifier
            .width(175.dp)
            .padding(8.dp)
            .clickable {
                navController.navigate(Screen.ChatScreen.createRoute(appointment.id, "user"))
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4102fb))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Star Rating (Centered)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("★", fontSize = 18.sp, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
                Text(
                    text = " $averageRating",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6bdbe0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFFed3782), CircleShape)
            ) {
                Image(
                    painter = if (profileImageUrl != null)
                        rememberAsyncImagePainter(profileImageUrl)
                    else
                        rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                    contentDescription = "Caregiver Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }


            // Username with @ symbol
            Text(
                text = caregiverUsername,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF6bdbe0),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // License
            Text(
                text = licenseType,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6bdbe0),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Available Date
            Text(
                text = "${appointment.date} (${appointment.timeSlot})",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6bdbe0),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Salary
            Text(
                text = salaryRate,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6bdbe0),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp)
            )

            // Status
            Text(
                text = appointment.status.capitalize(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6bdbe0),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp)
            )
        }
    }
}








@Composable
fun UserHeaderSection(
    username: String,
    pwdType: String,
    profileImageUrl: String?,
    navController: NavHostController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("profile") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6bdbe0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Smaller Profile Image
            // Profile Image with Border
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFFed3782), CircleShape)
            ) {
                Image(
                    painter = if (profileImageUrl != null)
                        rememberAsyncImagePainter(profileImageUrl)
                    else
                        rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }


            Spacer(modifier = Modifier.width(16.dp))

            // Username and PWD Type
            Column {
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "PWD Type: $pwdType",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}





@Composable
fun FilterChip(label: String, onRemove: () -> Unit) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                label,
                fontSize = 12.sp,
                color = Color(0xFFed3782)  // Same as 'Available Caregivers' text color
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFed3782), modifier = Modifier.size(16.dp))
            }
        },
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            containerColor = Color(0xFF6bdbe0)  // Correct background color
        ),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}


@Composable
fun FilterDialog(
    selectedDate: MutableState<String>,
    selectedTimeSlot: MutableState<String>,
    selectedLicenseType: MutableState<String>,
    selectedMunicipality: MutableState<String>,
    showTimeSlotDialog: MutableState<Boolean>,
    showMunicipalityDialog: MutableState<Boolean>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val municipalities = LocationData.municipalityBarangayMap.keys.toList().sorted()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Apply") }
        },
        title = { Text("Select Filters") },
        text = {
            Column {
                // Date Button
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

                // Time Slot Button
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

                // License Toggle Button
                Button(
                    onClick = {
                        selectedLicenseType.value = if (selectedLicenseType.value == "Licensed") "Any" else "Licensed"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(selectedLicenseType.value)
                }

                // Municipality Button
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

                // Municipality Dialog
                if (showMunicipalityDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showMunicipalityDialog.value = false },
                        confirmButton = {
                            TextButton(onClick = { showMunicipalityDialog.value = false }) {
                                Text("OK")
                            }
                        },
                        title = { Text("Select Municipality") },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                municipalities.forEach { municipality ->
                                    Text(
                                        text = municipality,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedMunicipality.value = municipality
                                                showMunicipalityDialog.value = false
                                            }
                                            .padding(vertical = 4.dp, horizontal = 8.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    )
}



@Composable
fun UserBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, Screen.UserMainScreen.route),
        BottomNavItem("Chat", Icons.AutoMirrored.Filled.Chat, "chat_history"),
        BottomNavItem("Transactions", Icons.AutoMirrored.Filled.ReceiptLong, "transaction_history"),
        BottomNavItem("Settings", Icons.Default.Settings, "settings")
    )
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    androidx.compose.material3.NavigationBar(
        containerColor = Color(0xFF4102fb)  // Bottom bar background color
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(item.icon, contentDescription = item.label, tint = Color(0xFFed3782))  // Icon color
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.UserMainScreen.route) { inclusive = false }
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
