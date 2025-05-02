package com.example.carego.screens.availabilityscreen

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun AvailabilityDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<Pair<String, String>>) -> Unit,
    existingDates: List<String> = emptyList() // to prevent re-adding
) {
    val context = LocalContext.current
    val selectedSlots = remember { mutableStateMapOf<String, MutableSet<String>>() }
    val openDatePicker = remember { mutableStateOf(false) }

    val timeSlots = listOf("Morning", "Afternoon", "Night", "Whole Day")

    fun showDatePicker(context: Context) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                val date = String.format("%02d/%02d/%04d", month + 1, day, year)
                if (date !in existingDates) {
                    selectedSlots[date] = mutableSetOf()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    if (openDatePicker.value) {
        showDatePicker(context)
        openDatePicker.value = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Availability") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { openDatePicker.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text("Add Date")
                }

                selectedSlots.forEach { (date, slots) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("Date: $date", style = MaterialTheme.typography.titleSmall)
                        timeSlots.forEach { slot ->
                            val isWholeDaySelected = slots.contains("Whole Day")
                            val isOtherSelected = slots.any { it in listOf("Morning", "Afternoon", "Night") }

                            val isEnabled = when (slot) {
                                "Whole Day" -> !isOtherSelected
                                else -> !isWholeDaySelected
                            }

                            val isChecked = slots.contains(slot)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp)
                                    .toggleable(
                                        value = isChecked,
                                        enabled = isEnabled,
                                        onValueChange = {
                                            if (it) {
                                                if (slot == "Whole Day") {
                                                    slots.clear()
                                                    slots.add("Whole Day")
                                                } else {
                                                    slots.add(slot)
                                                }
                                            } else {
                                                slots.remove(slot)
                                            }
                                        }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null,
                                    enabled = isEnabled,
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                                Text(text = slot)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val uniqueSelections = selectedSlots.flatMap { (date, slots) ->
                        if ("Whole Day" in slots) listOf(date to "Whole Day")
                        else slots.map { date to it }
                    }
                    onConfirm(uniqueSelections)
                    onDismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
