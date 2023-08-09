package com.example.credential_manager.data

import androidx.credentials.PasswordCredential

data class CredManagerResult(val credentials: PasswordCredential? = null, val error: Error? = null)

data class Error(val errorMessage: String)
