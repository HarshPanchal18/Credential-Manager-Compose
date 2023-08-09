package com.example.credential_manager.data

import androidx.credentials.PasswordCredential

data class CredManagerUiState(
    var isUsernameValid: Boolean = true,
    var isPasswordValid: Boolean = true,
    var errorMessage: String = "",
    var signedInPasswordCredential: PasswordCredential? = null,
    var signInSuccess: Boolean = false
)
