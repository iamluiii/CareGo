package com.example.carego.data


data class Appointment(
    val id: String = "",
    val caregiverId: String = "",
    val userId: String = "",
    var username: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val status: String = "",
    val municipality: String = "",
    var pwdType: String = "",
    val patientName: String = "",
    val location: String = "",
    val time: String = ""

)

