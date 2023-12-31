package com.example.credential_manager.viewmodel

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.credential_manager.data.CredManagerRepository
import com.example.credential_manager.data.CredManagerResult
import com.example.credential_manager.data.CredManagerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CredManagerViewModel @Inject constructor(
    private val credManagerRepository: CredManagerRepository
) :
    ViewModel() {
    private val _uiState = MutableStateFlow(CredManagerUiState()) // It can change state and has access to all of the methods on StateFlow
    val uiState: StateFlow<CredManagerUiState> = _uiState.asStateFlow()
    var isCredentialSaved = mutableStateOf(false)
    private lateinit var credentialManager: CredentialManager

    // validating username-password fields
    fun isUsernameValid(username: String) {
        _uiState.update { currState ->
            currState.copy(isUsernameValid = username.isNotEmpty())
            // Making sure that currState holds true whenever someone enters anything into username
        }
    }

    fun isPasswordValid(password: String) {
        _uiState.update { currState ->
            currState.copy(isPasswordValid = password.isNotEmpty())
        }
    }

    fun createCredentialManager(activity: Activity) {
        credentialManager = CredentialManager.create(activity)
    }

    fun signInUser(activity: Activity, username: String, password: String) {
        viewModelScope.launch {
            val result = credManagerRepository.signIn(
                activity, credentialManager, username, password, viewModelScope
            )
            // Handle the result from saving the credentials
            handleResult(result)
        }
    }

    private fun handleResult(result: CredManagerResult) {
        when {
            result.credentials != null -> { // If result got the positive output
                _uiState.update { currState ->
                    currState.copy(
                        signedInPasswordCredential = result.credentials,
                        errorMessage = ""
                    )
                }
            }

            result.error != null -> { // If there is any error comes with result
                _uiState.update { currState ->
                    currState.copy(
                        signedInPasswordCredential = null,
                        errorMessage = result.error.errorMessage
                    )
                }
            }
        }
    }

    fun simulateLogout() {
        _uiState.update { currState -> // make credentials to null for logging out user
            currState.copy(signedInPasswordCredential = null, errorMessage = "")
        }
    }

    // In-memory flag to check if credentials are saved or not
    suspend fun isCredentialSaved(activity: Activity) {
        val credentials =
            credManagerRepository.isCredentialSaved(activity, credentialManager, viewModelScope)
        isCredentialSaved.value = credentials != null

        // Update the UI with the new state
        if (credentials == null) {
            _uiState.update { currState ->
                currState.copy(signedInPasswordCredential = null, errorMessage = "")
            }
        } else {
            _uiState.update { currState -> // Sign in with credentials
                currState.copy(signedInPasswordCredential = credentials)
            }
        }
    }
}
