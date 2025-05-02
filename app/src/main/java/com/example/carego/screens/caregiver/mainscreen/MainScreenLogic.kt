package com.example.carego.screens.caregiver.mainscreen

import androidx.compose.material.DismissValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun loadCaregiverProfile(onProfileLoaded: (String, String, String) -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    db.collection("caregivers").document(uid).get()
        .addOnSuccessListener { doc ->
            val loadedUsername = doc.getString("username") ?: ""
            val loadedFullName = "${doc.getString("firstName") ?: ""} ${doc.getString("lastName") ?: ""}"
            val loadedLicenseType = doc.getString("license") ?: "N/A"
            onProfileLoaded(loadedUsername, loadedFullName, loadedLicenseType)
        }
}


fun loadAvailability(savedAvailability: SnapshotStateList<Pair<String, String>>) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    db.collection("availability")
        .whereEqualTo("caregiverId", uid)
        .get()
        .addOnSuccessListener { result ->
            val refreshed = result.documents.mapNotNull { doc ->
                val date = doc.getString("date")
                val time = doc.getString("timeSlot")
                if (date != null && time != null) date to time else null
            }
            savedAvailability.clear()
            savedAvailability.addAll(refreshed)
        }
}


fun logoutAndNavigate(navController: NavController) {
    FirebaseAuth.getInstance().signOut()
    navController.navigate("choose_screen") {
        popUpTo("caregiver_main_screen") { inclusive = true }
    }
}

fun onDismissAvailability(
    dismissValue: DismissValue,
    date: String,
    times: List<String>,
    savedAvailability: SnapshotStateList<Pair<String, String>>,
    onEditRequest: (String, List<String>) -> Unit
): Boolean {
    return when (dismissValue) {
        DismissValue.DismissedToStart -> {
            deleteAvailability(date, times, savedAvailability)
            false
        }
        DismissValue.DismissedToEnd -> {
            onEditRequest(date, times)
            false
        }
        else -> false
    }
}

fun deleteAvailability(
    date: String,
    times: List<String>,
    savedAvailability: SnapshotStateList<Pair<String, String>>
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    times.forEach { time ->
        db.collection("availability")
            .whereEqualTo("caregiverId", uid)
            .whereEqualTo("date", date)
            .whereEqualTo("timeSlot", time)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { it.reference.delete() }
            }
    }
    loadAvailability(savedAvailability)
}

fun updateAvailability(
    date: String,
    oldTimes: List<String>,
    newTimes: List<String>,
    savedAvailability: SnapshotStateList<Pair<String, String>>
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    oldTimes.forEach { time ->
        db.collection("availability")
            .whereEqualTo("caregiverId", uid)
            .whereEqualTo("date", date)
            .whereEqualTo("timeSlot", time)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { it.reference.delete() }
            }
    }

    newTimes.forEach { time ->
        val data = hashMapOf(
            "caregiverId" to uid,
            "date" to date,
            "timeSlot" to time
        )
        db.collection("availability").add(data)
    }

    savedAvailability.removeAll { it.first == date }
    savedAvailability.addAll(newTimes.map { date to it })
}
