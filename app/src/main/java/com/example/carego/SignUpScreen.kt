package com.example.carego

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
            when (currentStep) {
                0 -> TopAppBarStep(title = "Create Your Account", navController = navController)
                1 -> TopAppBarStep(title = "Personal Information", navController = navController)
                2 -> TopAppBarStep(title = "Contact Information", navController = navController)
                3 -> TopAppBarStep(title = "Account Access", navController = navController)
                4 -> TopAppBarStep(title = "Review & Submit", navController = navController)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            when (currentStep) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 🔹 App logo at the top
                        Image(
                            painter = painterResource(id = R.drawable.caregologo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(bottom = 16.dp)
                        )

                        // 🔹 Title text
                        Text(
                            text = "SELECT TYPE OF USER:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF3E0AE2),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // 🔹 Row of selection buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SelectionButton(
                                label = "PWD",
                                iconResId = R.drawable.pwdicon,
                                isSelected = userType == "User",
                                onClick = {
                                    userType = "User"
                                    currentStep = 1
                                },
                                modifier = Modifier.weight(1f)
                            )

                            SelectionButton(
                                label = "Caregiver",
                                iconResId = R.drawable.caregivericon,
                                isSelected = userType == "Caregiver",
                                onClick = {
                                    userType = "Caregiver"
                                    currentStep = 1
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "CLICK APPROPRIATELY \n THEN PRESS YOUR OPTION",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
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
fun SelectionButton(
    label: String,
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFFED3782) else Color(0xFF6BDBE0)
    val backgroundColor = if (isSelected) Color(0xFFED3782).copy(alpha = 0.1f) else Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(3.dp, borderColor, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$label Icon",
                modifier = Modifier.size(70.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFFED3782) else Color.Gray,
            fontSize = 16.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val municipalities = LocationData.municipalityBarangayMap.keys.toList()
    val barangays = LocationData.municipalityBarangayMap[selectedMunicipality] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Contact Header
        Text(
            text = "Contact",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF3E0AE2),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            isError = errors["email"] == true,
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3E0AE2),
                unfocusedBorderColor = Color(0xFF3E0AE2),
                focusedLabelColor = Color(0xFF3E0AE2),
                unfocusedLabelColor = Color(0xFF3E0AE2)
            )
        )

        // Contact Number Field
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
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3E0AE2),
                unfocusedBorderColor = Color(0xFF3E0AE2),
                focusedLabelColor = Color(0xFF3E0AE2),
                unfocusedLabelColor = Color(0xFF3E0AE2)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Municipality Dropdown
        DropdownBox(
            label = "Municipality",
            options = municipalities,
            selectedOption = selectedMunicipality,
            onOptionSelected = {
                onMunicipalityChange(it)
                onBarangayChange("") // Reset barangay when municipality changes
            },
            error = errors["municipality"] == true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(20.dp)
        )

        // Barangay Dropdown
        DropdownBox(
            label = "Barangay",
            options = barangays,
            selectedOption = selectedBarangay,
            onOptionSelected = onBarangayChange,
            error = errors["barangay"] == true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(20.dp)
        )

        // Street Field
        OutlinedTextField(
            value = street,
            onValueChange = onStreetChange,
            label = { Text("Street") },
            isError = errors["street"] == true,
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3E0AE2),
                unfocusedBorderColor = Color(0xFF3E0AE2),
                focusedLabelColor = Color(0xFF3E0AE2),
                unfocusedLabelColor = Color(0xFF3E0AE2)
            )
        )

        // House Number Field
        OutlinedTextField(
            value = houseNumber,
            onValueChange = onHouseNumberChange,
            label = { Text("House Number") },
            isError = errors["houseNumber"] == true,
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3E0AE2),
                unfocusedBorderColor = Color(0xFF3E0AE2),
                focusedLabelColor = Color(0xFF3E0AE2),
                unfocusedLabelColor = Color(0xFF3E0AE2)
            )
        )

        if (!isCaregiver) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Emergency Contact", fontWeight = FontWeight.SemiBold, color = Color(0xFF3E0AE2))

            // Emergency Contact Name
            OutlinedTextField(
                value = emergencyName ?: "",
                onValueChange = {
                    onEmergencyNameChange?.invoke(it.filter { c -> c.isLetter() || c.isWhitespace() })
                },
                label = { Text("Emergency Contact Name") },
                isError = errors["emergencyName"] == true,
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3E0AE2),
                    unfocusedBorderColor = Color(0xFF3E0AE2),
                    focusedLabelColor = Color(0xFF3E0AE2),
                    unfocusedLabelColor = Color(0xFF3E0AE2)
                )
            )

            // Emergency Contact Number
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
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3E0AE2),
                    unfocusedBorderColor = Color(0xFF3E0AE2),
                    focusedLabelColor = Color(0xFF3E0AE2),
                    unfocusedLabelColor = Color(0xFF3E0AE2)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Styled Back and Next Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundedButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            RoundedButton(
                text = "Next",
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))


        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = {
                val cleaned = it.filter { c -> c.isLetterOrDigit() || c == '.' || c == '_' }
                onUsernameChange(cleaned)
            },
            label = { Text("Username") },
            isError = errors["username"] == true,
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3E0AE2),
                unfocusedBorderColor = Color(0xFF3E0AE2),
                focusedLabelColor = Color(0xFF3E0AE2),
                unfocusedLabelColor = Color(0xFF3E0AE2)
            )
        )

        // Password Field
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
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3E0AE2),
                unfocusedBorderColor = Color(0xFF3E0AE2),
                focusedLabelColor = Color(0xFF3E0AE2),
                unfocusedLabelColor = Color(0xFF3E0AE2)
            )
        )

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { if (!it.contains(" ")) onConfirmPasswordChange(it) },
            label = { Text("Confirm Password") },
            isError = errors["confirmPassword"] == true,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3E0AE2),
                unfocusedBorderColor = Color(0xFF3E0AE2),
                focusedLabelColor = Color(0xFF3E0AE2),
                unfocusedLabelColor = Color(0xFF3E0AE2)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Styled Back and Next Buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RoundedButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            RoundedButton(
                text = "Next",
                onClick = onNext,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
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
        // Logo
        Image(
            painter = painterResource(id = R.drawable.caregologo),
            contentDescription = "CareGo Logo",
            modifier = Modifier.size(150.dp).padding(bottom = 16.dp)
        )

        // Header
        Text(
            text = "Review & Submit",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF3E0AE2),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Review Instruction
        Text(
            text = "Please review your information before submitting.",
            color = Color(0xFF3E0AE2),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Terms and Privacy Checkboxes
        TermsAndPrivacyCheckboxes(
            termsChecked = termsChecked,
            privacyChecked = privacyChecked,
            onTermsChecked = { termsChecked = it },
            onPrivacyChecked = { privacyChecked = it },
            textColor = Color(0xFF3E0AE2)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Styled Back and Sign Up Buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RoundedButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            RoundedButton(
                text = "Sign Up",
                onClick = onSubmit,
                enabled = termsChecked && privacyChecked && !isLoading,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Verification Notice
        Text(
            text = "A verification email will be sent. You must verify before you can log in.",
            color = Color(0xFF3E0AE2),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
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
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    val days = (1..31).map { it.toString() }
    val years = (1970..2025).map { it.toString() }
    val scope = rememberCoroutineScope()
    var isButtonEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Create Your Account block inside TopAppBar area


        OutlinedTextField(
            value = lastName,
            onValueChange = { onLastNameChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
            label = { Text("Last Name") },
            isError = errors["lastName"] == true,
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors (focusedBorderColor = Color(0xFF3E0AE2),
            unfocusedBorderColor = Color(0xFF3E0AE2),
            focusedLabelColor = Color(0xFF3E0AE2),
            unfocusedLabelColor = Color(0xFF3E0AE2)
        )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { onFirstNameChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
            label = { Text("First Name") },
            isError = errors["firstName"] == true,
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
               focusedBorderColor = Color(0xFF3E0AE2),
                unfocusedBorderColor = Color(0xFF3E0AE2),
                focusedLabelColor = Color(0xFF3E0AE2),
                unfocusedLabelColor = Color(0xFF3E0AE2),
        )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = if (noMiddleName) "" else middleName,
            onValueChange = { onMiddleNameChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
            label = { Text("Middle Initial") },
            isError = errors["middleName"] == true,
            enabled = !noMiddleName,
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3E0AE2),
            unfocusedBorderColor = Color(0xFF3E0AE2),
            focusedLabelColor = Color(0xFF3E0AE2),
            unfocusedLabelColor = Color(0xFF3E0AE2),
        )
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = noMiddleName,
                onCheckedChange = onNoMiddleNameChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF3E0AE2),
                    uncheckedColor = Color(0xFF3E0AE2),
                    checkmarkColor = Color.White
                )
            )
            Text(
                text = "I don't have a Middle Name",
                color = Color(0xFF3E0AE2)
            )
        }



        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Birthday",
            color = Color(0xFF3E0AE2),
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DropdownBox("Month", months, birthMonth, onBirthMonthChange, modifier = Modifier.weight(1f))
            DropdownBox("Day", days, birthDay, onBirthDayChange, modifier = Modifier.weight(1f))
            DropdownBox("Year", years, birthYear, onBirthYearChange, modifier = Modifier.weight(1f))
        }


        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Gender",
            color = Color(0xFF3E0AE2),
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            listOf("Male", "Female", "Others").forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onGenderChange(option) }
                ) {
                    RadioButton(
                        selected = gender == option,
                        onClick = { onGenderChange(option) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF3E0AE2),
                            unselectedColor = Color(0xFF3E0AE2)
                        )
                    )
                    Text(
                        text = option,
                        color = Color(0xFF3E0AE2)
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(12.dp))

        if (userType == "User") {
            DropdownBox(
                label = "Type of PWD",
                options = listOf(
                    "Visual Disability", "Hearing Disability", "Speech and Language Disability",
                    "Physical Disability", "Mental/Intellectual Disability",
                    "Psychosocial Disability", "Disability Due to Chronic Illness", "Others"
                ),
                selectedOption = pwdType,
                onOptionSelected = onPWDTypeChange,
                error = errors["pwdType"] == true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )
        } else {
            DropdownBox(
                label = "Profession",
                options = listOf("Nurse", "Midwife", "Therapist", "Caregiver", "Paramedic"),
                selectedOption = profession,
                onOptionSelected = {
                    onProfessionChange(it)
                    onNoLicenseChange(false)
                },
                error = errors["profession"] == true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = noLicense,
                    onCheckedChange = {
                        onNoLicenseChange(it)
                        if (it) onProfessionChange("")
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF3E0AE2),
                        uncheckedColor = Color(0xFF3E0AE2),
                        checkmarkColor = Color.White
                    )
                )
                Text(
                    text = "I don’t have a license",
                    color = Color(0xFF3E0AE2)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isButtonEnabled) {
                    isButtonEnabled = false
                    scope.launch {
                        onNext()
                        delay(2000)
                        isButtonEnabled = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFED3782),
                contentColor = Color.White
            )
        ) {
            Text("Next", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
@Composable
fun RoundedButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) Color(0xFFED3782) else Color.LightGray,
            contentColor = Color.White
        )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    dropdownMaxHeight: Dp = 200.dp
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        text = label,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        color = if (error) MaterialTheme.colorScheme.error else Color(0xFF3E0AE2)
                    )
                },
                isError = error,
                shape = shape,
                singleLine = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3E0AE2),
                    unfocusedBorderColor = Color(0xFF3E0AE2),
                    cursorColor = Color(0xFF3E0AE2)
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth()
                    .heightIn(max = dropdownMaxHeight)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
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
    onPrivacyChecked: (Boolean) -> Unit,
    textColor: Color = Color(0xFF3E0AE2)
) {
    // Add missing dialog state
    val showTermsDialog = remember { mutableStateOf(false) }
    val showPrivacyDialog = remember { mutableStateOf(false) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    if (!termsChecked) showTermsDialog.value = true
                }
                .padding(bottom = 8.dp)
        ) {
            Checkbox(
                checked = termsChecked,
                onCheckedChange = {
                    if (!termsChecked) showTermsDialog.value = true
                    else onTermsChecked(false)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = textColor,
                    uncheckedColor = textColor,
                    checkmarkColor = Color.White
                )
            )
            Text(
                text = "I agree to the Terms and Conditions",
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
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
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = textColor,
                    uncheckedColor = textColor,
                    checkmarkColor = Color.White
                )
            )
            Text(
                text = "I agree to the Data Privacy Policy",
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
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

    // Enable the Agree button only when the user scrolls to the bottom
    LaunchedEffect(scrollState.maxValue) {
        snapshotFlow { scrollState.value }
            .collect { value ->
                agreeEnabled = value >= scrollState.maxValue
            }
    }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = onAgree,
                enabled = agreeEnabled,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFED3782),
                    contentColor = Color.White
                )
            ) {
                Text("Agree", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = Color(0xFF3E0AE2))
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E0AE2),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // ✅ Limit the height of the scrollable area
                    .border(2.dp, Color(0xFF3E0AE2), RoundedCornerShape(20.dp))
                    .padding(8.dp)
            ) {
                Row {
                    // Scrollable Text
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = content,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    // Slim Scroll Progress Bar
                    Box(
                        modifier = Modifier
                            .width(4.dp) // ✅ Slimmer width for the scrollbar
                            .fillMaxHeight()
                            .background(Color(0xFF3E0AE2).copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp) // ✅ Match the slimmer width
                                .fillMaxHeight(fraction = scrollState.value / scrollState.maxValue.toFloat().coerceAtLeast(0.01f))
                                .background(Color(0xFF3E0AE2), shape = RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp,
        containerColor = Color.White
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarStep(title: String, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF82E6E0),
                    shape = RoundedCornerShape(bottomEnd = 24.dp, bottomStart = 24.dp)
                )
                .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Sign Up",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 45.sp,
                    lineHeight = 45.sp,
                )
            }
        }
    }
}


