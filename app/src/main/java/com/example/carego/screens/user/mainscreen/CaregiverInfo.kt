package com.example.carego.screens.user.mainscreen

data class CaregiverInfo(
    val id: String = "",
    val username: String = "",
    val firstName: String = "",
    val middleName: String? = "", // <-- change to nullable,    // ✅ fix: default to blank string
    val lastName: String = "",
    val birthday: String = "",
    val gender: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val municipality: String = "",
    val license: String? = "",        // ✅ fix: default to blank string
    val address: String = "",        // ✅ fix: default to blank string
    val password: String = "",       // ✅ fix: default to blank string
    val availabilities: List<Pair<String, String>> = emptyList()
)
