package com.example.carego.screens.user.mainscreen

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun fetchUserInfo(
    db: FirebaseFirestore,
    userId: String,
    username: MutableState<String>,
    pwdType: MutableState<String>,
    isLoading: MutableState<Boolean>
) {
    db.collection("users").document(userId)
        .get()
        .addOnSuccessListener { document ->
            username.value = document.getString("username") ?: ""
            pwdType.value = document.getString("pwdType") ?: ""
            isLoading.value = false
        }
        .addOnFailureListener {
            isLoading.value = false
        }
}

fun fetchAvailableCaregivers(
    db: FirebaseFirestore,
    caregivers: SnapshotStateList<CaregiverInfo>
) {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val today = Date()

    db.collection("availability").get().addOnSuccessListener { result ->
        caregivers.clear()

        val caregiverAvailabilityMap = mutableMapOf<String, MutableList<Pair<String, String>>>()

        for (document in result) {
            val caregiverId = document.getString("caregiverId") ?: continue
            val dateString = document.getString("date") ?: ""
            val timeSlot = document.getString("timeSlot") ?: ""

            try {
                val availabilityDate = dateFormat.parse(dateString)

                if (availabilityDate != null && availabilityDate.before(today)) {
                    db.collection("availability").document(document.id).delete()
                } else {
                    val list = caregiverAvailabilityMap.getOrPut(caregiverId) { mutableListOf() }
                    list.add(dateString to timeSlot)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        for ((caregiverId, availabilities) in caregiverAvailabilityMap) {
            db.collection("caregivers").document(caregiverId).get().addOnSuccessListener { caregiverDoc ->
                val address = caregiverDoc.getString("address") ?: ""
                val municipality = extractMunicipality(address)

                caregivers.add(
                    CaregiverInfo(
                        id = caregiverId,
                        username = caregiverDoc.getString("username") ?: "",
                        license = caregiverDoc.getString("license") ?: "",
                        municipality = municipality,
                        firstName = caregiverDoc.getString("firstName") ?: "",
                        middleName = caregiverDoc.getString("middleName") ?: "",
                        lastName = caregiverDoc.getString("lastName") ?: "",
                        birthday = caregiverDoc.getString("birthday") ?: "",
                        gender = caregiverDoc.getString("gender") ?: "",
                        contactNumber = caregiverDoc.getString("contactNumber") ?: "",
                        email = caregiverDoc.getString("email") ?: "",
                        availabilities = availabilities
                    )
                )
            }
        }
    }
}

fun fetchSingleCaregiverAvailabilities(
    db: FirebaseFirestore,
    caregiverId: String,
    onResult: (List<Pair<String, String>>) -> Unit
) {
    db.collection("availability")
        .whereEqualTo("caregiverId", caregiverId)
        .get()
        .addOnSuccessListener { result ->
            val availabilities = result.map { doc ->
                (doc.getString("date") ?: "") to (doc.getString("timeSlot") ?: "")
            }
            onResult(availabilities)
        }
}

fun fetchAppointments(
    db: FirebaseFirestore,
    userId: String,
    appointments: SnapshotStateList<AppointmentInfo>
) {
    db.collection("appointments").whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { result ->
            appointments.clear()
            for (document in result) {
                val caregiverId = document.getString("caregiverId") ?: continue
                val date = document.getString("date") ?: ""
                val timeSlot = document.getString("timeSlot") ?: ""
                db.collection("caregivers").document(caregiverId).get().addOnSuccessListener { caregiverDoc ->
                    val address = caregiverDoc.getString("address") ?: ""
                    val municipality = extractMunicipality(address)
                    appointments.add(
                        AppointmentInfo(
                            caregiverUsername = caregiverDoc.getString("username") ?: "",
                            license = caregiverDoc.getString("license") ?: "",
                            municipality = municipality,
                            date = date,
                            timeSlot = timeSlot
                        )
                    )
                }
            }
        }
}

fun extractMunicipality(address: String): String {
    val parts = address.split(",").map { it.trim() }
    return if (parts.size >= 3) parts[parts.size - 2] else ""
}

fun showDatePickerDialog(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%02d/%02d/%d", month + 1, dayOfMonth, year)
            onDateSelected(selectedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
