package com.example.carego

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ChatMessage(
    val senderId: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    appointmentId: String,
    navController: NavController,
    userType: String
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var newMessage by remember { mutableStateOf("") }

    val currentUserId = auth.currentUser?.uid ?: ""
    var username by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    // Fetch user info and listen to messages
    LaunchedEffect(appointmentId) {
        db.collection("appointments").document(appointmentId)
            .get()
            .addOnSuccessListener { doc ->
                val targetId = if (userType == "user") doc.getString("caregiverId")
                else doc.getString("userId")

                targetId?.let { userId ->
                    val targetCollection = if (userType == "user") "caregivers" else "users"
                    db.collection(targetCollection).document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            username = userDoc.getString("username") ?: ""
                            profileImageUrl = userDoc.getString("profileImageUrl")
                        }
                }
            }

        db.collection("chats").document(appointmentId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error == null) {
                    messages.clear()
                    value?.forEach { doc ->
                        messages.add(doc.toObject(ChatMessage::class.java))
                    }
                }
            }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6bdbe0), shape = RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Profile Picture with Border
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

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "@$username",
                        color = Color(0xFF4102fb),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },

        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(Color.White, shape = RoundedCornerShape(30.dp))
                    .border(3.dp, Color(0xFFed3782), RoundedCornerShape(30.dp)),  // Bordered Input Box
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    placeholder = { Text("Type a message ...") },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )

                IconButton(onClick = {
                    if (newMessage.isNotBlank()) {
                        val messageObj = ChatMessage(
                            senderId = currentUserId,
                            message = newMessage,
                            timestamp = Timestamp.now()
                        )

                        db.collection("chats")
                            .document(appointmentId)
                            .collection("messages")
                            .add(messageObj)
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to send", Toast.LENGTH_SHORT).show()
                            }

                        newMessage = ""
                    }
                }) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color(0xFF4102fb),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

            // Group messages by date
            messages.groupBy { msg ->
                val msgDate = msg.timestamp.toDate()
                val cal = Calendar.getInstance().apply { time = msgDate }

                when {
                    cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"

                    cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"

                    else -> dateFormatter.format(msgDate)
                }
            }.forEach { (dateLabel, messageGroup) ->
                // Centered date separator
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = dateLabel,
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // Message bubbles
                items(messageGroup) { message ->
                    val isCurrentUser = message.senderId == currentUserId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                    ) {
// Incoming message with profile picture
                        if (!isCurrentUser) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)  // Consistent sizing as used in UserMainScreen
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
                                    modifier = Modifier.fillMaxSize()  // Ensures the image fills the entire box without cutting
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        // Message bubble with time inside
                        Column(
                            modifier = Modifier
                                .background(
                                    color = if (isCurrentUser) Color(0xFF4102fb) else Color(0xFF4102fb),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .widthIn(max = 240.dp)
                        ) {
                            Text(
                                text = message.message,
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif
                            )

                            // Timestamp inside the bubble
                            Text(
                                text = timeFormatter.format(message.timestamp.toDate()),
                                color = Color.LightGray,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .align(if (isCurrentUser) Alignment.End else Alignment.Start)
                            )
                        }
                    }
                }
            }
        }
    }

}








@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean,
    profileImageUrl: String?,
    showProfileImage: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isCurrentUser && showProfileImage) {
            Image(
                painter = if (profileImageUrl != null)
                    rememberAsyncImagePainter(profileImageUrl)
                else
                    rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .padding(end = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .background(
                    color = if (isCurrentUser) Color(0xFF4102fb) else Color(0xFF4102fb),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .widthIn(max = 240.dp)
        ) {
            Text(
                text = message.message,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
        }

        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }

    // Message timestamp
    Text(
        text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.timestamp.toDate()),
        color = Color.Gray,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}




@Composable
fun RescheduleDialog(
    appointmentId: String,
    userType: String,
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var dateAvailability by remember { mutableStateOf(mapOf<String, Set<String>>()) }
    var availableTimeSlots by remember { mutableStateOf(listOf("Morning", "Afternoon", "Night", "Whole Day")) }

    val todayCalendar = Calendar.getInstance()
    val maxDateCalendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 14) // 2-week limit
    }
    var currentMonth by remember { mutableStateOf(todayCalendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(todayCalendar.get(Calendar.YEAR)) }
    val daysInMonth = remember(currentMonth, currentYear) {
        generateDaysInMonth(currentMonth, currentYear).toList()
    }

    // Load date availability
    LaunchedEffect(appointmentId, userType) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        val query = if (userType == "user") {
            db.collection("appointments")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", "confirmed")
        } else {
            db.collection("appointments")
                .whereEqualTo("caregiverId", currentUserId)
                .whereEqualTo("status", "confirmed")
        }

        query.get().addOnSuccessListener { snapshot ->
            val availabilityMap = mutableMapOf<String, MutableSet<String>>()

            snapshot.documents.forEach { doc ->
                val date = doc.getString("date") ?: return@forEach
                val timeSlot = doc.getString("timeSlot") ?: return@forEach

                if (availabilityMap.containsKey(date)) {
                    availabilityMap[date]!!.add(timeSlot)
                } else {
                    availabilityMap[date] = mutableSetOf(timeSlot)
                }
            }

            dateAvailability = availabilityMap
        }
    }

    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedDate.isBlank() || selectedTime.isBlank()) {
                        showError = true
                        return@Button
                    }

                    // Validate if the selected date and time slot are already booked
                    val bookedSlots = dateAvailability[selectedDate] ?: emptySet()
                    if (bookedSlots.contains(selectedTime)) {
                        Toast.makeText(context, "You already have an appointment at this time", Toast.LENGTH_SHORT).show()
                        return@Button // ⛔️ Block reschedule if already booked
                    }

                    // Firestore update logic
                    db.collection("appointments").document(appointmentId)
                        .update("date", selectedDate, "timeSlot", selectedTime)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Appointment rescheduled successfully.", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to reschedule appointment.", Toast.LENGTH_SHORT).show()
                        }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Cancel")
            }
        },
        title = { Text("Reschedule Appointment") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Month Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        if (currentMonth == 0) {
                            currentMonth = 11
                            currentYear--
                        } else {
                            currentMonth--
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                    }

                    Text(
                        text = "${getMonthName(currentMonth)} $currentYear",
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(onClick = {
                        if (currentMonth == 11) {
                            currentMonth = 0
                            currentYear++
                        } else {
                            currentMonth++
                        }
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                    }
                }

                // Calendar Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .height(280.dp)
                        .padding(vertical = 8.dp)
                ) {
                    items(daysInMonth.size) { index ->
                        val day = daysInMonth[index]
                        val calendar = Calendar.getInstance()
                        calendar.set(currentYear, currentMonth, day)
                        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                        val formattedDate = dateFormat.format(calendar.time)

                        // Check if the date is outside the 2-week range
                        val isPastDate = calendar.before(todayCalendar)
                        val isBeyondLimit = calendar.after(maxDateCalendar)
                        val isSelected = selectedDate == formattedDate

                        CalendarDay(
                            day = day,
                            isPastDate = isPastDate || isBeyondLimit,
                            isSelected = isSelected,
                            onClick = {
                                if (!isPastDate && !isBeyondLimit) {
                                    selectedDate = formattedDate
                                    selectedTime = "" // Reset selected time
                                    showError = false
                                }
                            }
                        )
                    }
                }

                // Time Slot Selector
                if (selectedDate.isNotBlank()) {
                    Text("Available Time Slots:", style = MaterialTheme.typography.bodyMedium)

                    availableTimeSlots.forEach { slot ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedTime = slot
                                    showError = false
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTime == slot,
                                onClick = {
                                    selectedTime = slot
                                    showError = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(slot, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                // Error Message
                if (showError) {
                    Text(
                        text = "Please select a valid date and time.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}

@Composable
fun CalendarDay(
    day: Int,
    isPastDate: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .background(
                when {
                    isPastDate -> Color.LightGray
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                },
                shape = MaterialTheme.shapes.medium
            )
            .clickable(enabled = !isPastDate) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = if (isPastDate) Color.Gray else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}


fun getMonthName(month: Int): String {
    return SimpleDateFormat("MMMM", Locale.getDefault()).apply {
        timeZone = Calendar.getInstance().timeZone
    }.format(Calendar.getInstance().apply { set(Calendar.MONTH, month) }.time)
}

fun generateDaysInMonth(month: Int, year: Int): List<Int> {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    return (1..maxDays).toList()
}