package com.example.carego.screens.availabilityscreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carego.data.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AvailabilityDatePicker(
    savedAvailability: SnapshotStateList<Appointment>,
    onDateSaved: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var showDialog by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedSlots by remember { mutableStateOf(listOf<String>()) }

    val todayCalendar = Calendar.getInstance()
    var currentMonth by remember { mutableStateOf(todayCalendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(todayCalendar.get(Calendar.YEAR)) }

    val daysInMonth = remember(currentMonth, currentYear) {
        generateDaysInMonth(currentMonth, currentYear)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedDate.isNotEmpty() && selectedSlots.isNotEmpty()) {
                            val caregiverId = uid
                            // Load caregiver info first
                            if (caregiverId != null) {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("caregivers").document(caregiverId)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        val caregiverUsername = document.getString("username") ?: ""
                                        val licenseType = document.getString("license") ?: "No License"
                                        val municipality = document.getString("municipality") ?: ""

                                        selectedSlots.forEach { time ->
                                            val appointment = hashMapOf(
                                                "caregiverId" to caregiverId,
                                                "caregiverUsername" to caregiverUsername,
                                                "license" to licenseType,
                                                "municipality" to municipality,
                                                "date" to selectedDate,
                                                "timeSlot" to time,
                                                "status" to "available"
                                            )
                                            db.collection("appointments").add(appointment)
                                        }
                                        onDateSaved()
                                        showDialog = false
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to load caregiver info.", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Please select a date and at least one time slot.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Confirm")
                }
            }
,
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDialog = false
                        onDateSaved() // ✅ triggers parent to hide dialog and unblock UI
                    }
                ) {
                    Text("Cancel")
                }

            },
            title = { Text("Select Availability") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
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

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(daysInMonth) { day ->
                            val calendar = Calendar.getInstance()
                            calendar.set(currentYear, currentMonth, day)
                            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                            val formattedDate = dateFormat.format(calendar.time)

                            val maxSelectableDate = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, 14)
                            }

                            val isPastDate = calendar.before(todayCalendar)
                            val isBeyondTwoWeeks = calendar.after(maxSelectableDate)
                            val isAlreadySelected = savedAvailability.any { it.date == formattedDate }
                            val isDisabled = isPastDate || isAlreadySelected || isBeyondTwoWeeks


                            val isCurrentlySelected = selectedDate == formattedDate

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .background(
                                        when {
                                            isDisabled -> Color.LightGray
                                            isCurrentlySelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        },
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clickable(enabled = !isDisabled) {
                                        selectedDate = formattedDate
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    color = if (isDisabled) Color.Gray else Color.Black,
                                    fontWeight = if (isCurrentlySelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (selectedDate.isNotEmpty()) {
                        Text("Select Time", fontWeight = FontWeight.Medium)

                        val allSlots = listOf("Morning", "Afternoon", "Night", "Whole Day")

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
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
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
            }
        )
    }
}

fun generateDaysInMonth(month: Int, year: Int): List<Int> {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    return (1..maxDays).toList()
}

fun getMonthName(month: Int): String {
    return SimpleDateFormat("MMMM", Locale.getDefault()).apply {
        timeZone = Calendar.getInstance().timeZone
    }.format(Calendar.getInstance().apply { set(Calendar.MONTH, month) }.time)
}