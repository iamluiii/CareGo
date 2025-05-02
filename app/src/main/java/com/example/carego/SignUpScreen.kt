package com.example.carego

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.carego.helpers.AddressData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(0) } // 0: Choose Type, 1: Personal, 2: Contact, 3: Access

    var userType by remember { mutableStateOf("User") }

    // Shared states
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var noMiddleName by remember { mutableStateOf(false) }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var noLicense by remember { mutableStateOf(false) }
    var pwdType by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var selectedMunicipality by remember { mutableStateOf("") }
    var selectedBarangay by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("") }
    var emergencyName by remember { mutableStateOf("") }
    var emergencyNumber by remember { mutableStateOf("") }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val errors = remember { mutableStateMapOf<String, Boolean>() }

    // Loading state for progress bar
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Handle errors and success messages using Toast
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotBlank()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    // Handle SignUp Success
    fun signUpUser() {
        isLoading = true
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val collectionName = if (userType == "Caregiver") "caregivers" else "users"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                result.user?.sendEmailVerification()

                val birthday = "$birthMonth/$birthDay/$birthYear"
                val address = "$street $houseNumber, $selectedBarangay, $selectedMunicipality, Pampanga"
                val middle = if (noMiddleName) null else middleName

                val firestoreData = mutableMapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "middleName" to middle,
                    "gender" to gender,
                    "birthday" to birthday,
                    "email" to email,
                    "contactNumber" to contactNumber,
                    "address" to address,
                    "username" to username,
                    "password" to password,
                    "profileImageUrl" to "",
                    "isVerified" to false
                )


                if (userType == "Caregiver") {
                    firestoreData["license"] = if (noLicense) "Unlicensed" else profession
                } else {
                    firestoreData["pwdType"] = pwdType
                    firestoreData["emergencyName"] = emergencyName
                    firestoreData["emergencyNumber"] = emergencyNumber
                }


                db.collection(collectionName).document(userId)
                    .set(firestoreData)
                    .addOnSuccessListener {
                        isLoading = false
                        Toast.makeText(context, "Account created. Please verify your email.", Toast.LENGTH_LONG).show()
                        navController.navigate("user_login_screen") {
                            popUpTo("signup_screen") { inclusive = true }
                        }

                    }
                    .addOnFailureListener { error ->
                        isLoading = false
                        errorMessage = "Failed to save data: ${error.message}"
                    }
            }
            .addOnFailureListener { error ->
                isLoading = false
                errorMessage = "Sign up failed: ${error.message}"
            }
    }

    // UI for the sign-up process
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sign Up",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = currentStep / 3f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            when (currentStep) {
                0 -> {
                    Text("Select Type", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Button(onClick = {
                            userType = "User"
                            currentStep = 1
                        }) {
                            Text("User")
                        }
                        Button(onClick = {
                            userType = "Caregiver"
                            currentStep = 1
                        }) {
                            Text("Caregiver")
                        }
                    }
                }

                1 -> {
                    // Personal Info Step
                    PersonalInfoStep(
                        userType = userType,
                        lastName = lastName,
                        onLastNameChange = { lastName = it; errors["lastName"] = false },
                        firstName = firstName,
                        onFirstNameChange = { firstName = it; errors["firstName"] = false },
                        middleName = middleName,
                        onMiddleNameChange = { middleName = it; errors["middleName"] = false },
                        noMiddleName = noMiddleName,
                        onNoMiddleNameChange = {
                            noMiddleName = it
                            if (it) middleName = ""
                            errors["middleName"] = false
                        },
                        birthMonth = birthMonth,
                        onBirthMonthChange = { birthMonth = it; errors["birthday"] = false },
                        birthDay = birthDay,
                        onBirthDayChange = { birthDay = it; errors["birthday"] = false },
                        birthYear = birthYear,
                        onBirthYearChange = { birthYear = it; errors["birthday"] = false },
                        gender = gender,
                        onGenderChange = { gender = it; errors["gender"] = false },
                        profession = profession,
                        onProfessionChange = { profession = it; errors["profession"] = false },
                        noLicense = noLicense,
                        onNoLicenseChange = {
                            noLicense = it
                            if (it) profession = ""
                            errors["profession"] = false
                        },
                        pwdType = pwdType,
                        onPWDTypeChange = { pwdType = it; errors["pwdType"] = false },
                        errors = errors,
                        onNext = {
                            val valid = validatePersonalInfoSimple(
                                context, errors,
                                lastName, firstName, middleName, noMiddleName,
                                birthMonth, birthDay, birthYear,
                                gender, pwdType,
                                userType
                            )
                            if (valid) currentStep = 2
                        }
                    )
                }

                2 -> {
                    // Contact Info Step
                    ContactInfoStepUnified(
                        isCaregiver = userType == "Caregiver",
                        email = email,
                        onEmailChange = { email = it; errors["email"] = false },
                        contactNumber = contactNumber,
                        onContactNumberChange = { contactNumber = it; errors["contactNumber"] = false },
                        selectedMunicipality = selectedMunicipality,
                        onMunicipalityChange = { selectedMunicipality = it; errors["municipality"] = false },
                        selectedBarangay = selectedBarangay,
                        onBarangayChange = { selectedBarangay = it; errors["barangay"] = false },
                        street = street,
                        onStreetChange = { street = it; errors["street"] = false },
                        houseNumber = houseNumber,
                        onHouseNumberChange = { houseNumber = it; errors["houseNumber"] = false },
                        emergencyName = if (userType == "User") emergencyName else null,
                        onEmergencyNameChange = if (userType == "User") ({ emergencyName = it; errors["emergencyName"] = false }) else null,
                        emergencyNumber = if (userType == "User") emergencyNumber else null,
                        onEmergencyNumberChange = if (userType == "User") ({ emergencyNumber = it; errors["emergencyNumber"] = false }) else null,
                        errors = errors,
                        onNext = {
                            val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                            val numberValid = contactNumber.length == 11

                            if (!emailValid) {
                                errors["email"] = true
                                Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
                                return@ContactInfoStepUnified
                            }

                            if (!numberValid) {
                                errors["contactNumber"] = true
                                Toast.makeText(context, "Contact number must be 11 digits", Toast.LENGTH_SHORT).show()
                                return@ContactInfoStepUnified
                            }

                            val db = FirebaseFirestore.getInstance()
                            val usersRef = db.collection("users")
                            val caregiversRef = db.collection("caregivers")

                            usersRef.whereEqualTo("email", email).get().addOnSuccessListener { userEmail ->
                                caregiversRef.whereEqualTo("email", email).get().addOnSuccessListener { caregiverEmail ->
                                    if (!userEmail.isEmpty || !caregiverEmail.isEmpty) {
                                        Toast.makeText(context, "Email already in use", Toast.LENGTH_SHORT).show()
                                        return@addOnSuccessListener
                                    }

                                    usersRef.whereEqualTo("contactNumber", contactNumber).get().addOnSuccessListener { userContact ->
                                        caregiversRef.whereEqualTo("contactNumber", contactNumber).get().addOnSuccessListener { caregiverContact ->
                                            if (!userContact.isEmpty || !caregiverContact.isEmpty) {
                                                Toast.makeText(context, "Contact number already in use", Toast.LENGTH_SHORT).show()
                                                return@addOnSuccessListener
                                            }

                                            val valid = if (userType == "User") {
                                                validateContactInfoSimple(
                                                    context, errors,
                                                    email, contactNumber,
                                                    selectedMunicipality, selectedBarangay,
                                                    street, houseNumber,
                                                    emergencyName ?: "", emergencyNumber ?: ""
                                                )
                                            } else {
                                                validateContactInfoForCareGiver(
                                                    context, errors,
                                                    email, contactNumber,
                                                    selectedMunicipality, selectedBarangay,
                                                    street, houseNumber
                                                )
                                            }

                                            if (valid) currentStep = 3
                                        }
                                    }
                                }
                            }
                        }
,
                                onBack = { currentStep = 1 }
                    )
                }

                3 -> {
                    // Account Access Step
                    AccountAccessStepUnified(
                        username = username,
                        onUsernameChange = { username = it; errors["username"] = false },
                        password = password,
                        onPasswordChange = { password = it; errors["password"] = false },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it; errors["confirmPassword"] = false },
                        showPassword = showPassword,
                        onTogglePasswordVisibility = { showPassword = !showPassword },
                        errors = errors,
                        onBack = { currentStep = 2 },
                        onNext = {
                            errors["username"] = username.isBlank()
                            errors["password"] = password.isBlank()
                            errors["confirmPassword"] = confirmPassword.isBlank()

                            if (errors["username"] == true || errors["password"] == true || errors["confirmPassword"] == true) {
                                Toast.makeText(context, "Please complete all fields.", Toast.LENGTH_SHORT).show()
                                return@AccountAccessStepUnified
                            }

                            if (username.contains(" ")) {
                                Toast.makeText(context, "Username cannot contain spaces.", Toast.LENGTH_SHORT).show()
                                return@AccountAccessStepUnified
                            }

                            if (password.contains(" ")) {
                                Toast.makeText(context, "Password cannot contain spaces.", Toast.LENGTH_SHORT).show()
                                return@AccountAccessStepUnified
                            }

                            if (password != confirmPassword) {
                                errors["confirmPassword"] = true
                                Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                                return@AccountAccessStepUnified
                            }

                            val db = FirebaseFirestore.getInstance()
                            val users = db.collection("users")
                            val caregivers = db.collection("caregivers")

                            users.whereEqualTo("username", username).get().addOnSuccessListener { userMatch ->
                                caregivers.whereEqualTo("username", username).get().addOnSuccessListener { caregiverMatch ->
                                    if (!userMatch.isEmpty || !caregiverMatch.isEmpty) {
                                        Toast.makeText(context, "Username already exists.", Toast.LENGTH_SHORT).show()
                                        return@addOnSuccessListener
                                    }

                                    users.whereEqualTo("email", email).get().addOnSuccessListener { emailMatch1 ->
                                        caregivers.whereEqualTo("email", email).get().addOnSuccessListener { emailMatch2 ->
                                            if (!emailMatch1.isEmpty || !emailMatch2.isEmpty) {
                                                Toast.makeText(context, "Email already exists.", Toast.LENGTH_SHORT).show()
                                                return@addOnSuccessListener
                                            }

                                            currentStep = 4
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                4 -> {
                    // Final Submit Step
                    FinalSubmitStepUnified(
                        userType = userType,
                        isLoading = isLoading,
                        onBack = { currentStep = 3 },
                        onSubmit = { signUpUser() }
                    )
                }
            }
        }
    }
}


@Composable
fun ContactInfoStepUnified(
    isCaregiver: Boolean,
    email: String,
    onEmailChange: (String) -> Unit,
    contactNumber: String,
    onContactNumberChange: (String) -> Unit,
    selectedMunicipality: String,
    onMunicipalityChange: (String) -> Unit,
    selectedBarangay: String,
    onBarangayChange: (String) -> Unit,
    street: String,
    onStreetChange: (String) -> Unit,
    houseNumber: String,
    onHouseNumberChange: (String) -> Unit,
    emergencyName: String?,
    onEmergencyNameChange: ((String) -> Unit)?,
    emergencyNumber: String?,
    onEmergencyNumberChange: ((String) -> Unit)?,
    errors: Map<String, Boolean>,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            isError = errors["email"] == true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contactNumber,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }.take(11)
                onContactNumberChange(filtered)
            },
            label = { Text("Contact Number") },
            isError = errors["contactNumber"] == true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Address", fontWeight = FontWeight.SemiBold)

        DropdownBox(
            label = "Municipality",
            options = AddressData.barangays.keys.sorted(),
            selectedOption = selectedMunicipality,
            onOptionSelected = {
                onMunicipalityChange(it)
                onBarangayChange("")
            },
            error = errors["municipality"] == true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        DropdownBox(
            label = "Barangay",
            options = AddressData.barangays[selectedMunicipality] ?: emptyList(),
            selectedOption = selectedBarangay,
            onOptionSelected = onBarangayChange,
            error = errors["barangay"] == true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        OutlinedTextField(
            value = street,
            onValueChange = onStreetChange,
            label = { Text("Street") },
            isError = errors["street"] == true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        OutlinedTextField(
            value = houseNumber,
            onValueChange = onHouseNumberChange,
            label = { Text("House Number") },
            isError = errors["houseNumber"] == true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        if (!isCaregiver) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Emergency Contact", fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = emergencyName ?: "",
                onValueChange = { input -> onEmergencyNameChange?.invoke(input.filter { it.isLetter() || it.isWhitespace() }) },
                label = { Text("Emergency Contact Name") },
                isError = errors["emergencyName"] == true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = emergencyNumber ?: "",
                onValueChange = { input -> onEmergencyNumberChange?.invoke(input.filter { it.isDigit() }.take(11)) },
                label = { Text("Emergency Contact Number") },
                isError = errors["emergencyNumber"] == true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                Text("Next")
            }
        }
    }
}
@Composable
fun AccountAccessStepUnified(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    errors: Map<String, Boolean>,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { if (!it.contains(" ")) onUsernameChange(it) },
            label = { Text("Username") },
            isError = errors["username"] == true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { if (!it.contains(" ")) onPasswordChange(it) },
            label = { Text("Password") },
            isError = errors["password"] == true,
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { if (!it.contains(" ")) onConfirmPasswordChange(it) },
            label = { Text("Confirm Password") },
            isError = errors["confirmPassword"] == true,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                Text("Next")
            }
        }
    }
}
@Composable
fun FinalSubmitStepUnified(
    userType: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Review & Submit",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text("Please review your information before submitting.", fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // You can add summary display here if needed

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (!isLoading) onSubmit()
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sign Up")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "A verification email will be sent. You must verify before you can log in.",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

fun createUserOrCaregiverAccount(
    context: Context,
    navController: NavController,
    isCaregiver: Boolean,
    userId: String,
    data: Map<String, Any?>,
    onLoadingChange: (Boolean) -> Unit
) {
    onLoadingChange(true)

    val db = FirebaseFirestore.getInstance()
    val collectionName = if (isCaregiver) "caregivers" else "users"

    val finalData = mutableMapOf<String, Any?>(
        "username" to data["username"]!!,
        "password" to data["password"]!!,
        "email" to data["email"]!!,
        "contactNumber" to data["contactNumber"]!!,
        "firstName" to data["firstName"]!!,
        "lastName" to data["lastName"]!!,
        "middleName" to data["middleName"],
        "gender" to data["gender"]!!,
        "birthday" to "${data["birthMonth"]}/${data["birthDay"]}/${data["birthYear"]}",
        "address" to "${data["street"]}, ${data["houseNumber"]}, ${data["barangay"]}, ${data["municipality"]}, Pampanga",
        "profileImageUrl" to "",
        "isVerified" to false
    )

    if (isCaregiver) {
        finalData["license"] = if (data["noLicense"] == true) "Unlicensed" else data["profession"]!!
    } else {
        finalData["pwdType"] = data["pwdType"]!!
        finalData["emergencyName"] = data["emergencyName"]!!
        finalData["emergencyNumber"] = data["emergencyNumber"]!!
    }

    db.collection(collectionName).document(userId)
        .set(finalData)
        .addOnSuccessListener {
            Toast.makeText(context, "Account created successfully! Please log in.", Toast.LENGTH_SHORT).show()

            // Return to login screen (don't stay logged in)
            navController.navigate("user_login_screen") {
                popUpTo("signup_screen") { inclusive = true }
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to save data.", Toast.LENGTH_SHORT).show()
        }
        .addOnCompleteListener {
            onLoadingChange(false)
        }
}


@Composable
fun PersonalInfoStep(
    userType: String,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    middleName: String,
    onMiddleNameChange: (String) -> Unit,
    noMiddleName: Boolean,
    onNoMiddleNameChange: (Boolean) -> Unit,
    birthMonth: String,
    onBirthMonthChange: (String) -> Unit,
    birthDay: String,
    onBirthDayChange: (String) -> Unit,
    birthYear: String,
    onBirthYearChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    profession: String,
    onProfessionChange: (String) -> Unit,
    noLicense: Boolean,
    onNoLicenseChange: (Boolean) -> Unit,
    pwdType: String,
    onPWDTypeChange: (String) -> Unit,
    errors: Map<String, Boolean>,
    onNext: () -> Unit
) {
    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val days = (1..31).map { it.toString() }
    val years = (1970..2025).map { it.toString() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = lastName,
            onValueChange = { onLastNameChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
            label = { Text("Last Name") },
            isError = errors["lastName"] == true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = { onFirstNameChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
            label = { Text("First Name") },
            isError = errors["firstName"] == true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        OutlinedTextField(
            value = if (noMiddleName) "" else middleName,
            onValueChange = { onMiddleNameChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
            label = { Text("Middle Name") },
            isError = errors["middleName"] == true,
            enabled = !noMiddleName,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = noMiddleName, onCheckedChange = onNoMiddleNameChange)
            Text("I don't have a middle name")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Birthday", fontWeight = FontWeight.SemiBold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DropdownBox(
                label = "Month",
                options = months,
                selectedOption = birthMonth,
                onOptionSelected = onBirthMonthChange,
                error = errors["birthday"] == true,
                modifier = Modifier.weight(1f)
            )
            DropdownBox(
                label = "Day",
                options = days,
                selectedOption = birthDay,
                onOptionSelected = onBirthDayChange,
                error = errors["birthday"] == true,
                modifier = Modifier.weight(1f)
            )
            DropdownBox(
                label = "Year",
                options = years,
                selectedOption = birthYear,
                onOptionSelected = onBirthYearChange,
                error = errors["birthday"] == true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Gender", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            listOf("Male", "Female", "Others").forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onGenderChange(option) }
                ) {
                    RadioButton(selected = gender == option, onClick = { onGenderChange(option) })
                    Text(option)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (userType == "User") {
            DropdownBox(
                label = "Type of PWD",
                options = listOf("Visual", "Hearing", "Mobility", "Cognitive", "Others"),
                selectedOption = pwdType,
                onOptionSelected = onPWDTypeChange,
                error = errors["pwdType"] == true
            )
        } else {
            DropdownBox(
                label = "Profession",
                options = listOf("Nurse", "Doctor", "Midwife", "Therapist", "Caregiver", "Paramedic", "Dentist"),
                selectedOption = profession,
                onOptionSelected = {
                    onProfessionChange(it)
                    onNoLicenseChange(false)
                },
                error = errors["profession"] == true
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = noLicense, onCheckedChange = {
                    onNoLicenseChange(it)
                    if (it) onProfessionChange("")
                })
                Text("I don't have a license")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Next")
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownBox(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    error: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                label = { Text(label) },
                readOnly = true,
                isError = error,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onOptionSelected(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (error) {
            Text(
                text = "This field is required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
fun validatePersonalInfoSimple(
    context: Context,
    errors: MutableMap<String, Boolean>,
    lastName: String,
    firstName: String,
    middleName: String,
    noMiddleName: Boolean,
    birthMonth: String,
    birthDay: String,
    birthYear: String,
    gender: String,
    pwdType: String,
    userType: String  // 🔁 add this
): Boolean {
    var isValid = true

    val middleValid = noMiddleName || middleName.isNotBlank()
    val birthdayValid = birthMonth.isNotBlank() && birthDay.isNotBlank() && birthYear.isNotBlank()

    errors["lastName"] = lastName.isBlank()
    errors["firstName"] = firstName.isBlank()
    errors["middleName"] = !middleValid
    errors["birthday"] = !birthdayValid
    errors["gender"] = gender.isBlank()
    if (userType == "User") {
        errors["pwdType"] = pwdType.isBlank()
    }

    if (errors["lastName"] == true || errors["firstName"] == true || errors["middleName"] == true || errors["birthday"] == true) {
        Toast.makeText(context, "Please complete all fields.", Toast.LENGTH_SHORT).show()
        isValid = false
    }

    if (gender.isBlank()) {
        Toast.makeText(context, "Please select a gender.", Toast.LENGTH_SHORT).show()
        isValid = false
    }

    if (userType == "User" && pwdType.isBlank()) {
        Toast.makeText(context, "Please select a type of PWD.", Toast.LENGTH_SHORT).show()
        isValid = false
    }

    return isValid
}

fun validateContactInfoSimple(
    context: Context,
    errors: MutableMap<String, Boolean>,
    email: String,
    contactNumber: String,
    municipality: String,
    barangay: String,
    street: String,
    houseNumber: String,
    emergencyName: String,
    emergencyNumber: String
): Boolean {
    var isValid = true

    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()

    errors["email"] = email.isBlank() || !email.matches(emailPattern)
    errors["contactNumber"] = contactNumber.length != 11
    errors["municipality"] = municipality.isBlank()
    errors["barangay"] = barangay.isBlank()
    errors["street"] = street.isBlank()
    errors["houseNumber"] = houseNumber.isBlank()
    errors["emergencyName"] = emergencyName.isBlank()
    errors["emergencyNumber"] = emergencyNumber.length != 11

    if (errors.values.any { it }) {
        Toast.makeText(context, "Please complete all fields correctly.", Toast.LENGTH_SHORT).show()
        isValid = false
    }

    return isValid
}
fun validateContactInfoForCareGiver(
    context: Context,
    errors: MutableMap<String, Boolean>,
    email: String,
    contactNumber: String,
    municipality: String,
    barangay: String,
    street: String,
    houseNumber: String
): Boolean {
    var isValid = true

    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()

    errors["email"] = email.isBlank() || !email.matches(emailPattern)
    errors["contactNumber"] = contactNumber.length != 11
    errors["municipality"] = municipality.isBlank()
    errors["barangay"] = barangay.isBlank()
    errors["street"] = street.isBlank()
    errors["houseNumber"] = houseNumber.isBlank()

    if (errors.values.any { it }) {
        Toast.makeText(context, "Please complete all fields correctly.", Toast.LENGTH_SHORT).show()
        isValid = false
    }

    return isValid
}
