package com.example.credential_manager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.credential_manager.viewmodel.CredManagerViewModel

@Composable
fun UserInput(
    credManagerViewModel: CredManagerViewModel,
    modifier: Modifier = Modifier,
    onResetScroll: () -> Unit = {},
    onSignIn: (String, String) -> Unit
) {
    var usernameTextState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    var passwordTextState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    // To decide if the keyboard should be shown
    var usernameTextFieldFocusState by remember { mutableStateOf(false) }
    var passwordTextFieldFocusState by remember { mutableStateOf(false) }

    val isCredentialSaved by remember { credManagerViewModel.isCredentialSaved }
    val uiState = credManagerViewModel.uiState.collectAsState().value

    Surface(modifier = Modifier.padding(vertical = 16.dp)) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIcon(contentDescription = "App icon")
            if (uiState.signedInPasswordCredential == null) {
                if (!isCredentialSaved) {
                    UserInputText(
                        credManagerViewModel,
                        textFieldValue = usernameTextState,
                        onTextChanged = {
                            credManagerViewModel.isUsernameValid(it.text)
                            usernameTextState = it
                        },
                        keyboardShown = usernameTextFieldFocusState,
                        onTextFieldFocused = { focused ->
                            if (focused) onResetScroll.invoke()
                            usernameTextFieldFocusState = focused
                        },
                        label = "Username",
                        inputType = InputType.USERNAME
                    )
                    UserInputText(
                        credManagerViewModel,
                        textFieldValue = passwordTextState,
                        onTextChanged = {
                            credManagerViewModel.isPasswordValid(it.text)
                            passwordTextState = it
                        },
                        keyboardShown = passwordTextFieldFocusState,
                        onTextFieldFocused = { focused ->
                            if (focused) onResetScroll.invoke()
                            passwordTextFieldFocusState = focused
                        },
                        label = "Password",
                        inputType = InputType.PASSWORD,
                    )
                } else {
                    TextMessage(message = "Welcome Back to Cred Manager, Hit \'Sign In\' to Get Started!")
                }

                val buttonText = if (isCredentialSaved) "Sign In" else "Save credentials"
                RequestAccessButton(title = buttonText,
                    modifier = Modifier.fillMaxWidth(),
                    onMessageSent = {
                        if (usernameTextState.text.isEmpty() || passwordTextState.text.isEmpty()) return@RequestAccessButton
                        onSignIn.invoke(usernameTextState.text, passwordTextState.text)
                    })
                if (uiState.errorMessage.isNotEmpty()) TextFieldError(uiState.errorMessage)
            } else {
                TextFieldLogout("Signed in successfully! Click to Logout") {
                    usernameTextState = TextFieldValue()
                    passwordTextState = TextFieldValue()
                    credManagerViewModel.simulateLogout()
                }
            }
        }
    }
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInputText(
    credManagerViewModel: CredManagerViewModel,
    keyboardType: KeyboardType = KeyboardType.Text,
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    keyboardShown: Boolean,
    onTextFieldFocused: (Boolean) -> Unit,
    label: String,
    inputType: InputType
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = label
                keyboardShownProperty = keyboardShown
            },
        horizontalArrangement = Arrangement.End
    ) {
        Surface {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.wrapContentSize()) {
                    Column {
                        var lastFocusState by remember { mutableStateOf(false) }
                        val uiState by credManagerViewModel.uiState.collectAsState()

                        OutlinedTextField(
                            value = textFieldValue,
                            onValueChange = { onTextChanged(it) },
                            visualTransformation =
                            if (inputType == InputType.PASSWORD) PasswordVisualTransformation()
                            else VisualTransformation.None,
                            label = { UserInputTextLabel(label) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { state ->
                                    if (lastFocusState != state.isFocused)
                                        onTextFieldFocused(state.isFocused)
                                    lastFocusState = state.isFocused
                                },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = keyboardType,
                                imeAction = if (inputType == InputType.USERNAME) ImeAction.Next else ImeAction.Done
                            ),
                            singleLine = true,
                            maxLines = 1,
                            textStyle = LocalTextStyle.current.copy(LocalContentColor.current)
                        )

                        if (!uiState.isUsernameValid && inputType == InputType.USERNAME)
                            TextFieldError(message = "Username can't be blank")
                        if (!uiState.isPasswordValid && inputType == InputType.PASSWORD)
                            TextFieldError(message = "Password can't be blank")
                    }
                }
            }
        }
    }
}

@Composable
fun RequestAccessButton(
    title: String,
    modifier: Modifier,
    onMessageSent: () -> Unit
) {
    val disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    val buttonColors = ButtonDefaults.buttonColors(
        disabledContainerColor = Color.Transparent,
        disabledContentColor = disabledContentColor
    )

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        TextButton(
            onClick = { onMessageSent() },
            colors = buttonColors,
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}

@Composable
private fun UserInputTextLabel(label: String) {
    Text(text = label)
}

@Composable
fun TextFieldLogout(message: String, onLogout: () -> Unit) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
        modifier = Modifier.clickable { onLogout.invoke() }
    )
}

@Composable
fun TextFieldError(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.error)
    )
}

@Composable
fun TextMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp
        ),
        modifier = Modifier.padding(16.dp),
        textAlign = TextAlign.Center
    )
}

enum class InputType {
    USERNAME, PASSWORD
}
