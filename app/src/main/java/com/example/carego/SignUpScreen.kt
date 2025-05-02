package com.example.carego

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
    var contactNumber by remember { mutableStateOf("09") }
    var selectedMunicipality by remember { mutableStateOf("") }
    var selectedBarangay by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("") }
    var emergencyName by remember { mutableStateOf("") }
    var emergencyNumber by remember { mutableStateOf("09") }

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

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = painterResource(id = R.drawable.caregologo),
                                        contentDescription = "User Logo",
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = {
                                        userType = "User"
                                        currentStep = 1
                                    }) {
                                        Text("User")
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = painterResource(id = R.drawable.caregiverlogo),
                                        contentDescription = "Caregiver Logo",
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = {
                                        userType = "Caregiver"
                                        currentStep = 1
                                    }) {
                                        Text("Caregiver")
                                    }
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
                            if (!username.matches(Regex("^[a-zA-Z0-9._]+$"))) {
                                Toast.makeText(context, "Username can only contain letters, numbers, dots, and underscores.", Toast.LENGTH_SHORT).show()
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
            value = contactNumber.removePrefix("09"),
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }.take(9)
                onContactNumberChange("09$filtered")
            },
            label = { Text("Contact Number") },
            leadingIcon = { Text("09") },
            isError = errors["contactNumber"] == true,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
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
                value = emergencyNumber?.removePrefix("09") ?: "",
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(9)
                    onEmergencyNumberChange?.invoke("09$filtered")
                },
                label = { Text("Emergency Contact Number") },
                leadingIcon = { Text("09") },
                isError = errors["emergencyNumber"] == true,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
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
            onValueChange = {
                val cleaned = it.filter { c -> c.isLetterOrDigit() || c == '.' || c == '_' }
                onUsernameChange(cleaned)
            }
            ,
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
    var termsChecked by remember { mutableStateOf(false) }
    var privacyChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Review & Submit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Please review your information before submitting.")

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Insert checkboxes with dialog
        TermsAndPrivacyCheckboxes(
            termsChecked = termsChecked,
            privacyChecked = privacyChecked,
            onTermsChecked = { termsChecked = it },
            onPrivacyChecked = { privacyChecked = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Back")
            }

            Button(
                onClick = onSubmit,
                enabled = termsChecked && privacyChecked && !isLoading,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Sign Up")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "A verification email will be sent. You must verify before you can log in.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
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
                options = listOf("Visual Disability",
                    "Hearing Disability",
                    "Speech and Language Disability",
                    "Physical Disability",
                    "Mental/Intellectual Disability",
                    "Psychosocial Disability",
                    "Disability Due to Chronic Illness",),
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
@Composable
fun TermsAndPrivacyCheckboxes(
    termsChecked: Boolean,
    privacyChecked: Boolean,
    onTermsChecked: (Boolean) -> Unit,
    onPrivacyChecked: (Boolean) -> Unit
) {
    val showTermsDialog = remember { mutableStateOf(false) }
    val showPrivacyDialog = remember { mutableStateOf(false) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    if (!termsChecked) showTermsDialog.value = true
                }
        ) {
            Checkbox(
                checked = termsChecked,
                onCheckedChange = {
                    if (!termsChecked) showTermsDialog.value = true
                    else onTermsChecked(false)
                }
            )
            Text("I agree to the Terms and Conditions")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    if (!privacyChecked) showPrivacyDialog.value = true
                }
        ) {
            Checkbox(
                checked = privacyChecked,
                onCheckedChange = {
                    if (!privacyChecked) showPrivacyDialog.value = true
                    else onPrivacyChecked(false)
                }
            )
            Text("I agree to the Data Privacy Policy")
        }

        if (showTermsDialog.value) {
            ScrollableAgreementDialog(
                title = "Terms and Conditions",
                content = "Welcome to CareGo – We Care As You Go.\n" +
                        "\n" +
                        "These Terms and Conditions govern your use of the CareGo mobile application and services. By using the app, you agree to these Terms. If you disagree, please discontinue use.\n" +
                        "\n" +
                        "1. Eligibility\n" +
                        "You must be 18+ or legally authorized to act for a PWD. Provide truthful information at all times.\n" +
                        "\n" +
                        "2. Nature of Service\n" +
                        "CareGo connects users to licensed healthcare professionals. We do not employ or manage them.\n" +
                        "\n" +
                        "3. User Responsibilities\n" +
                        "- Provide accurate information\n" +
                        "- Ensure a respectful environment\n" +
                        "- Use only for lawful purposes\n" +
                        "- Report issues promptly\n" +
                        "\n" +
                        "4. Healthcare Worker Responsibilities\n" +
                        "- Submit valid credentials\n" +
                        "- Follow ethical standards\n" +
                        "- Maintain client privacy\n" +
                        "\n" +
                        "5. Bookings & Cancellations\n" +
                        "- Subject to caregiver availability\n" +
                        "- Follow app’s Cancellation Policy\n" +
                        "- No-shows may incur charges\n" +
                        "\n" +
                        "6. Payments & Refunds\n" +
                        "- Secure in-app payments\n" +
                        "- Transparent fees\n" +
                        "- Refunds reviewed if requested within 7 days\n" +
                        "\n" +
                        "7. Background Checks\n" +
                        "- Performed on professionals for safety\n" +
                        "\n" +
                        "8. Privacy\n" +
                        "- Governed by our Privacy Policy\n" +
                        "\n" +
                        "9. Limitation of Liability\n" +
                        "CareGo is not liable for:\n" +
                        "- Injuries or damages\n" +
                        "- Provider mistakes\n" +
                        "- Technical disruptions\n" +
                        "\n" +
                        "10. Account Suspension\n" +
                        "Accounts may be suspended for violations or unsafe behavior.\n" +
                        "\n" +
                        "11. Updates\n" +
                        "Terms may be updated. Continued use means acceptance.\n" +
                        "\n" +
                        "12. Governing Law\n" +
                        "Under Philippine law. Disputes resolved in Pampanga courts.\n" +
                        "\n" +
                        "13. Contact\n" +
                        "Questions? Use the Contact section in the app.",
                onCancel = { showTermsDialog.value = false },
                onAgree = {
                    onTermsChecked(true)
                    showTermsDialog.value = false
                }
            )
        }

        if (showPrivacyDialog.value) {
            ScrollableAgreementDialog(
                title = "Data Privacy Policy",
                content = "Effective Date: [Insert Date]\n" +
                        "\n" +
                        "At CareGo, your privacy is our priority. We uphold your rights under the Data Privacy Act of 2012 (RA 10173) and ensure responsible handling of your personal data.\n" +
                        "\n" +
                        "1. Information We Collect\n" +
                        "a. Personal Info – Name, birthdate, contact, address, ID\n" +
                        "b. Health Info – Disabilities or medical info (with consent)\n" +
                        "c. Provider Info – License, certifications, experience\n" +
                        "d. Payment Info – Processed via third-party gateways\n" +
                        "e. Usage Data – Device, IP, crash logs, usage stats\n" +
                        "\n" +
                        "2. How We Use Your Data\n" +
                        "- To match you with licensed healthcare providers\n" +
                        "- Process bookings and payments\n" +
                        "- Communicate updates and alerts\n" +
                        "- Verify identities and ensure safety\n" +
                        "- Improve our platform and services\n" +
                        "\n" +
                        "3. Sharing of Information\n" +
                        "We never sell your data. We only share with:\n" +
                        "- Care providers (for service coordination)\n" +
                        "- Secure third-party processors (e.g., payment systems)\n" +
                        "- Legal authorities when required by law\n" +
                        "\n" +
                        "4. Data Retention\n" +
                        "We keep your data only as long as needed to provide services and comply with laws. You can request deletion with some limitations.\n" +
                        "\n" +
                        "5. Your Rights\n" +
                        "Under the Data Privacy Act, you can:\n" +
                        "- Access, update, correct, or delete your data\n" +
                        "- Withdraw consent anytime\n" +
                        "- File complaints with the National Privacy Commission\n" +
                        "\n" +
                        "6. Data Security\n" +
                        "We apply strict physical, technical, and organizational safeguards, including encryption, access control, and regular audits.\n" +
                        "\n" +
                        "7. Children's Privacy\n" +
                        "CareGo is for users 18+. Minors may use the app only through authorized guardians.\n" +
                        "\n" +
                        "8. Updates to Policy\n" +
                        "We’ll inform you of major changes via the app or email. Continued use means you accept the updated terms.\n" +
                        "\n" +
                        "9. Contact\n" +
                        "Need help? Contact our Data Protection Officer via [Insert Email]",
                onCancel = { showPrivacyDialog.value = false },
                onAgree = {
                    onPrivacyChecked(true)
                    showPrivacyDialog.value = false
                }
            )
        }
    }
}

@Composable
fun ScrollableAgreementDialog(
    title: String,
    content: String,
    onCancel: () -> Unit,
    onAgree: () -> Unit
) {
    val scrollState = rememberScrollState()
    var agreeEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.maxValue) {
        snapshotFlow { scrollState.value }
            .collect { value ->
                agreeEnabled = value >= scrollState.maxValue
            }
    }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(
                onClick = onAgree,
                enabled = agreeEnabled
            ) {
                Text("Agree", color = if (agreeEnabled) MaterialTheme.colorScheme.primary else Color.Gray)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .height(320.dp)
                    .verticalScroll(scrollState)
                    .padding(8.dp)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp
    )
}


