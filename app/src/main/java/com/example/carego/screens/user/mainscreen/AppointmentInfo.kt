package com.example.carego.screens.user.mainscreen

data class AppointmentInfo(
    val id: String = "",
    val caregiverId: String = "", // 🔥
    val caregiverUsername: String = "",
    val license: String = "",
    val municipality: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val status: String = ""
)
