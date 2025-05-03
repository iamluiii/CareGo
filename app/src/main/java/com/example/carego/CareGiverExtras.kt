package com.example.carego

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.carego.navigation.Screen
import com.example.carego.screens.caregiver.mainscreen.CareGiverBottomBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ChatPreview(
    val appointmentId: String,
    val userId: String,
    val userName: String,
    val latestMessage: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var chatList by remember { mutableStateOf<List<ChatPreview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val list = mutableListOf<ChatPreview>()
        val caregiverId = currentUser?.uid
        println("Current caregiverId: $caregiverId")

        val appointmentsSnapshot = db.collection("appointments")
            .whereEqualTo("caregiverId", caregiverId)
            .get().await()

        println("Appointments found: ${appointmentsSnapshot.size()}")

        for (doc in appointmentsSnapshot.documents) {
            val appointmentId = doc.id
            val userId = doc.getString("userId") ?: continue

            val messagesSnapshot = try {
                db.collection("chats")
                    .document(appointmentId)
                    .collection("messages")
                    .orderBy("timestamp")
                    .get().await()
            } catch (e: Exception) {
                println("No messages found in chat for $appointmentId: ${e.message}")
                continue
            }

            if (!messagesSnapshot.isEmpty) {
                val latest = messagesSnapshot.documents.lastOrNull()
                val latestText = latest?.getString("message") ?: ""
                val timestamp = latest?.getTimestamp("timestamp")?.toDate()?.time ?: 0L

                val userDoc = db.collection("users").document(userId).get().await()
                val userName = userDoc.getString("username") ?: "Unknown"

                list.add(
                    ChatPreview(
                        appointmentId = appointmentId,
                        userId = userId,
                        userName = userName,
                        latestMessage = latestText,
                        timestamp = timestamp
                    )
                )
            }
        }

        chatList = list.sortedByDescending { it.timestamp }
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chat History") }) },
        bottomBar = { CareGiverBottomBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (chatList.isEmpty()) {
                Text(
                    text = "No chat history found.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(chatList) { chat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    navController.navigate(Screen.ChatScreen.createRoute(chat.appointmentId, "caregiver"))

                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = chat.userName, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = chat.latestMessage, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        bottomBar = { CareGiverBottomBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("This is the Settings screen.")
        }
    }
}