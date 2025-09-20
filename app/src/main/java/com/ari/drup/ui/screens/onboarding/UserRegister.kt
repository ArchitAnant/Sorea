package com.ari.drup.ui.screens.onboarding

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ari.drup.data.User
import com.ari.drup.regular_font
import com.ari.drup.ui.components.AvatarSelector
import com.ari.drup.ui.components.DatePickerModal
import com.ari.drup.ui.components.convertMillisToDate
import com.ari.drup.viewmodels.OnboardingViewModel
import com.ari.drup.viewmodels.regState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUserScreen(
    onRegisterClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var avatarId by remember { mutableIntStateOf(0) }
    var screenCount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (screenCount>0) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "",
                        tint = Color.White,
                        modifier = modifier
                            .padding(top = 30.dp, start = 30.dp)
                            .clickable {
                                if (screenCount > 0) {
                                    screenCount--;
                                }
                            }
                    )
                }

                Text(
                    text = when(screenCount){
                        0 -> "Add Username"
                        1 -> "Add Name"
                        2 -> "Select Gender"
                        3 -> "Select Age"
                        4 -> "Select Avatar"
                        else -> "Add Username"
                    },
                    fontFamily = regular_font,
                    color = Color.White,
                    fontSize = 30.sp,
                    modifier = modifier.padding(top = 30.dp, start = 20.dp)
                )
            }
        },
        bottomBar = {
            Column (
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)

                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Personal Information like Name, Email, Age and Gender are just for registration, It will not be visible on the platform.",
                    color = Color.White.copy(0.5f),
                    fontFamily = regular_font,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    fontSize = 12.sp
                )
                ForwardButton({
                    if (screenCount==0 && username.isNotEmpty()) {
                        screenCount++;
                    }
                    else if (screenCount==1 && name.isNotEmpty()) {
                        screenCount++;
                    }
                    else if (screenCount==2 && gender.isNotEmpty()) {
                        screenCount++;
                    }
                    else if (screenCount==3 && selectedDate!=null) {
                        screenCount++;
                    }
                    else if (screenCount==4) {
                        val newUser = User(
                                username,
                                name,
                                avatarId,
                                gender,
                                selectedDate
                            )
                        scope.launch {
                            snackbarHostState.showSnackbar("Proceeding to Register")
                            val newUser = User(
                                username,
                                name,
                                avatarId,
                                gender,
                                selectedDate
                            )
                        }
                        onRegisterClick(newUser)
                    }
                    else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please Fill a Value")
                        }
                    }

                })
            }
        },
        containerColor = Color.Black,
    ) { innerPadding ->
        Box(modifier= Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (screenCount<=1) {
                OnboardTextField(
                    when (screenCount) {
                        0 -> username
                        1 -> name
//                        2 -> gender
//                    4 -> avatarId.toString()
                        else -> username
                    },
                    {
                        if (screenCount == 0) {
                            username = it
                        }
                        if (screenCount == 1) {
                            name = it
                        }
//                        if (screenCount == 2) {
//                            gender = it
//                        }

                    },
                    when (screenCount) {
                        0 -> "Enter your username"
                        1 -> "Enter your name"
                        else -> "Enter your username"
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else if (screenCount==2){
                GenderSelection({
                    gender = it
                })
            }
            else if (screenCount==3) {
                var showModal by remember { mutableStateOf(false) }
                TextField(
                    value = selectedDate?.let { convertMillisToDate(it) } ?: "",
                    onValueChange = { },
                    placeholder = { Text(
                        text="MM/DD/YYYY",
                        color = Color.White.copy(0.3f),
                        fontFamily = regular_font,
                        fontSize = 18.sp
                    ) },
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = "Select date", tint = Color.White.copy(0.5f))
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White.copy(0.5f),
                        unfocusedLeadingIconColor = Color.White,
                        focusedLeadingIconColor = Color.White,

                        ),
                    modifier = modifier
                        .fillMaxWidth(0.85f)
                        .padding(innerPadding)
                        .pointerInput(selectedDate) {
                            awaitEachGesture {
                                // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                                // in the Initial pass to observe events before the text field consumes them
                                // in the Main pass.
                                awaitFirstDown(pass = PointerEventPass.Initial)
                                val upEvent =
                                    waitForUpOrCancellation(pass = PointerEventPass.Initial)
                                if (upEvent != null) {
                                    showModal = true
                                }
                            }
                        }
                )

                if (showModal) {
                    DatePickerModal(
                        onDateSelected = { selectedDate = it },
                        onDismiss = { showModal = false }
                    )
                }
            }
            else if (screenCount==4) {
                    AvatarSelector({
                        avatarId = it
                    }, modifier = Modifier.padding(innerPadding))
            }
        }
    }

}

@Composable
fun GenderSelection(
    currentSelectedGender: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("Male", "Female", "Other", "Do not specify")
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        options.forEach { option ->
            val isSelected = selectedOption == option
            OutlinedButton(
                onClick = {
                    selectedOption = option
                    currentSelectedGender(option)
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                border = BorderStroke(
                    2.dp,
                    if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = option,
                    fontFamily = regular_font,
                    fontSize = 18.sp,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}


@Composable
fun OnboardTextField(
    message : String,
    onMessageChange : (String) -> Unit,
    placeholderText:String,
    modifier: Modifier = Modifier
) {
    TextField(
        value = message,
        onValueChange = { onMessageChange(it) },
        placeholder = {
            Text(
                text=placeholderText,
                color = Color.White.copy(0.3f),
                fontFamily = regular_font,
                fontSize = 18.sp
            )
        },
        modifier = modifier.fillMaxWidth(0.85f),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedLabelColor = Color.White,
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.White.copy(0.5f),
            unfocusedLeadingIconColor = Color.White,
            focusedLeadingIconColor = Color.White,

            ),
        textStyle = TextStyle(
            fontFamily = regular_font,
            fontSize = 18.sp
        )
    )
}

@Composable
fun ForwardButton(
    onForwardClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            onForwardClick()
        },
        shape = CircleShape,
        modifier = Modifier.size(90.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "",
            tint = Color.Black,
            modifier = Modifier.size(25.dp)
        )
    }
}



@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun RegisterUserScreenPrev() {
//    RegisterUserScreen()//OnboardingViewModel(rememberNavController()))
//    AddUsername("",{})
}