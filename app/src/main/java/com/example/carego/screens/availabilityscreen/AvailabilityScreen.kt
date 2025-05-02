package com.example.carego.screens.availabilityscreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AvailabilityScreen() {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    var availabilityMap by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    LaunchedEffect(Unit) {
        if (uid == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return@LaunchedEffect
        }

        try {
            val snapshot = db.collection("availability")
                .whereEqualTo("caregiverId", uid)
                .get()
                .await()

            val tempMap = mutableMapOf<String, MutableList<String>>()

            for (doc in snapshot.documents) {
                val date = doc.getString("date") ?: continue
                val time = doc.getString("timeSlot") ?: continue

                if (!tempMap.containsKey(date)) tempMap[date] = mutableListOf()
                if (!tempMap[date]!!.contains(time)) tempMap[date]!!.add(time)
            }

            availabilityMap = tempMap
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load availability", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Availability", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val dayFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

            items(daysInMonth) { index ->
                val day = index + 1
                val dateStr = String.format("%02d/%02d/%04d", currentMonth + 1, day, currentYear)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F1F1))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(day.toString(), style = MaterialTheme.typography.bodySmall)
                    availabilityMap[dateStr]?.forEach { time ->
                        Icon(
                            imageVector = when (time) {
                                "Morning" -> Icons.Default.WbSunny
                                "Afternoon" -> Icons.Default.WbCloudy
                                "Night" -> Icons.Default.Nightlight
                                "Whole Day" -> Icons.Default.EventAvailable
                                else -> Icons.Default.Help
                            },
                            contentDescription = time,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
