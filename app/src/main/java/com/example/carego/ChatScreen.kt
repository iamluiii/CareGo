package com.example.carego

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.carego.navigation.Screen
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
    var accountType by remember { mutableStateOf("") }

    var userId by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var showingUserDetails by remember { mutableStateOf(false) }
    var appointmentStatus by remember { mutableStateOf("") }

    val listState = rememberLazyListState() // ✅ For scrolling

    // Fetch user + appointment info and listen to messages
    LaunchedEffect(appointmentId) {
        db.collection("appointments").document(appointmentId)
            .get()
            .addOnSuccessListener { doc ->
                appointmentStatus = doc.getString("status") ?: ""

                val targetId = if (userType == "user") doc.getString("caregiverId")
                else doc.getString("userId")
                userId = targetId ?: return@addOnSuccessListener

                val targetCollection = if (userType == "user") "caregivers" else "users"

                db.collection(targetCollection).document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        username = userDoc.getString("username") ?: ""
                        profileImageUrl = userDoc.getString("profileImageUrl")

                        fullName = listOf(
                            userDoc.getString("lastName"),
                            userDoc.getString("firstName"),
                            userDoc.getString("middleName")
                        ).filterNot { it.isNullOrBlank() || it == "null" }
                            .joinToString(" ")

                        contact = userDoc.getString("contactNumber") ?: ""
                        accountType = if (userType == "user") {
                            userDoc.getString("license") ?: ""
                        } else {
                            userDoc.getString("pwdType") ?: ""
                        }

                        email = userDoc.getString("email") ?: ""
                        birthday = userDoc.getString("birthday") ?: ""
                        gender = userDoc.getString("gender") ?: ""
                        address = userDoc.getString("address") ?: ""
                    }
            }

        db.collection("chats").document(appointmentId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("ChatScreen", "Listen failed.", error)
                    return@addSnapshotListener
                }
                messages.clear()
                value?.forEach { doc ->
                    messages.add(doc.toObject(ChatMessage::class.java))
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
                    .padding(WindowInsets.statusBars.asPaddingValues()) // handles notch
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (showingUserDetails) {
                                showingUserDetails = false
                            } else {
                                navController.popBackStack() // ✅ this is where navController is used
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Image(
                            painter = if (profileImageUrl != null)
                                rememberAsyncImagePainter(profileImageUrl)
                            else
                                rememberAsyncImagePainter(R.drawable.defaultprofileicon),
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { showingUserDetails = true },
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "@$username", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
,
                bottomBar = {
            if (!showingUserDetails) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp)),
                        placeholder = { Text("Type a message...") },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xFFF5F5F5),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

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
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (showingUserDetails) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (userType == "user") "Caregiver Details" else "User Details",
                            style = MaterialTheme.typography.titleLarge
                        )

                        DetailCard("Full Name", fullName, Modifier.align(Alignment.CenterHorizontally))
                        DetailCard("Username", "@$username", Modifier.align(Alignment.CenterHorizontally))
                        DetailCard("Email", email, Modifier.align(Alignment.CenterHorizontally))
                        DetailCard("Contact Number", contact, Modifier.align(Alignment.CenterHorizontally))
                        DetailCard("Birthday", birthday, Modifier.align(Alignment.CenterHorizontally))
                        DetailCard("Gender", gender, Modifier.align(Alignment.CenterHorizontally))
                        DetailCard(
                            if (userType == "user") "License Type" else "PWD Type",
                            accountType,
                            Modifier.align(Alignment.CenterHorizontally)
                        )
                        DetailCard("Address", address, Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
            else {
                if (userType == "caregiver" && appointmentStatus == "pending") {
                    var showAcceptDialog by remember { mutableStateOf(false) }
                    var showDeclineDialog by remember { mutableStateOf(false) }

                    if (showAcceptDialog) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showAcceptDialog = false },
                            title = { Text("Confirm Acceptance") },
                            text = { Text("Are you sure you want to accept this booking?") },
                            confirmButton = {
                                Button(onClick = {
                                    db.collection("appointments").document(appointmentId)
                                        .update("status", "confirmed")
                                    appointmentStatus = "confirmed"
                                    showAcceptDialog = false
                                }) {
                                    Text("Yes")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showAcceptDialog = false }) {
                                    Text("No")
                                }
                            }
                        )
                    }

                    if (showDeclineDialog) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showDeclineDialog = false },
                            title = { Text("Confirm Decline") },
                            text = { Text("Are you sure you want to decline and cancel the chat?") },
                            confirmButton = {
                                Button(onClick = {
                                    val appointmentRef = db.collection("appointments").document(appointmentId)
                                    val chatRef = db.collection("chats").document(appointmentId)

                                    appointmentRef.update("status", "available").addOnSuccessListener {
                                        chatRef.collection("messages").get().addOnSuccessListener { snapshot ->
                                            val batch = db.batch()
                                            for (doc in snapshot.documents) {
                                                batch.delete(doc.reference)
                                            }
                                            batch.commit().addOnSuccessListener {
                                                Toast.makeText(context, "Booking declined.", Toast.LENGTH_SHORT).show()
                                                navController.navigate(
                                                    if (userType == "user")
                                                        Screen.UserMainScreen.route
                                                    else
                                                        Screen.CareGiverMainScreen.route
                                                ) {
                                                    popUpTo(0)
                                                }
                                            }
                                        }
                                    }

                                    showDeclineDialog = false
                                }) {
                                    Text("Yes")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showDeclineDialog = false }) {
                                    Text("No")
                                }
                            }
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp) // ✅ added spacing
                        ) {
                            Text(
                                text = "Please confirm this booking request:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = { showAcceptDialog = true }) {
                                    Text("Accept")
                                }

                                Button(onClick = { showDeclineDialog = true }) {
                                    Text("Decline")
                                }
                            }
                        }
                    }

                }

                Spacer(modifier = Modifier.height(8.dp)) // 🆕 space between top bar and messages

                val dateFormatter = remember {
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                }
                val timeFormatter = remember {
                    SimpleDateFormat("hh:mm a", Locale.getDefault())
                }
                val today = remember { Calendar.getInstance() }
                val yesterday = remember {
                    Calendar.getInstance().apply { add(Calendar.DATE, -1) }
                }

                val groupedMessages = messages.groupBy { msg ->
                    val cal = Calendar.getInstance().apply { time = msg.timestamp.toDate() }

                    when {
                        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"

                        cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                                cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"

                        else -> dateFormatter.format(cal.time)
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = false
                ) {
                    groupedMessages.forEach { (dateLabel, messageGroup) ->
                        item {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                color = Color.Gray
                            )
                        }

                        items(messageGroup) { message ->
                            val isCurrentUser = message.senderId == currentUserId

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                            ) {
                                Column(horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isCurrentUser) MaterialTheme.colorScheme.primary
                                                else Color.LightGray,
                                                shape = MaterialTheme.shapes.medium
                                            )
                                            .padding(10.dp)
                                            .widthIn(max = 240.dp)
                                    ) {
                                        Text(
                                            message.message,
                                            color = if (isCurrentUser) Color.White else Color.Black,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = timeFormatter.format(message.timestamp.toDate()),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }


            }
        }
    }
}

@Composable
fun DetailCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
