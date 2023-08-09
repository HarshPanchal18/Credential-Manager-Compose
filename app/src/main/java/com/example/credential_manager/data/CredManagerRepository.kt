package com.example.credential_manager.data

import android.app.Activity
import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CredManagerRepository {
    companion object {
        const val TAG: String = "CredManager"
    }

    /**
     * Create new credentials with credential manager
     *
     * @param activity - the activity context needed to show the credentials flow dialog to the user
     * @param username - username
     * @param password - password
     * @param coroutineScope - The coroutine scope to run the dialog on
     */
    suspend fun signIn(
        activity: Activity,
        credentialManager: CredentialManager,
        username: String,
        password: String,
        coroutineScope: CoroutineScope
    ): CredManagerResult {
        return if (isCredentialSaved(activity, credentialManager, coroutineScope) != null)
            getCredential(activity, credentialManager)
        else
            saveCredential(activity, credentialManager, username, password)
    }

    suspend fun isCredentialSaved(
        activity: Activity,
        credentialManager: CredentialManager,
        coroutineScope: CoroutineScope
    ): PasswordCredential? = suspendCoroutine { continuation ->
        coroutineScope.launch {
            try {
                val getCredRequest = GetCredentialRequest(listOf(GetPasswordOption()))
                val credentialResponse = credentialManager.getCredential(
                    request = getCredRequest,
                    activity = activity,
                )
                val credential = credentialResponse.credential as? PasswordCredential
                // Resumes the execution of the corresponding coroutine passing credential as the return value of the last suspension point.
                continuation.resume(credential)

            } catch (e: GetCredentialCancellationException) {
                continuation.resume(null)
                Log.d(TAG, e.toString())
            } catch (e: NoCredentialException) {
                continuation.resume(null)
                Log.d(TAG, e.toString())
            } catch (e: GetCredentialException) {
                continuation.resume(null)
                Log.d(TAG, e.toString())
            }
        }
    }

    private suspend fun saveCredential(
        activity: Activity,
        credentialManager: CredentialManager,
        username: String,
        password: String
    ): CredManagerResult {
        return try {
            val response = credentialManager.createCredential(
                request = CreatePasswordRequest(username, password),
                activity = activity,
            )

            Log.e(TAG, "Credentials successfully added: ${response.data}")
            CredManagerResult(credentials = PasswordCredential(username, password))
        } catch (e: CreateCredentialCancellationException) {
            Log.e(TAG, "User cancelled the save flow")
            CredManagerResult(error = Error("User cancelled the save flow"))
        } catch (e: CreateCredentialException) {
            Log.e(TAG, "Credentials cannot be saved", e)
            CredManagerResult(error = Error("Credentials cannot be saved"))
        }
    }

    private suspend fun getCredential(
        activity: Activity,
        credentialManager: CredentialManager
    ): CredManagerResult {
        return try {
            val getCredRequest = GetCredentialRequest(listOf(GetPasswordOption()))

            val credentialResponse = credentialManager.getCredential(
                request = getCredRequest,
                activity = activity
            )
            val credential = credentialResponse.credential as? PasswordCredential

            CredManagerResult(credential)
        } catch (e: GetCredentialCancellationException) {
            Log.e(TAG, e.message.toString())
            CredManagerResult(error = Error("User cancelled the request"))
        } catch (e: NoCredentialException) {
            Log.e(TAG, e.message.toString())
            CredManagerResult(error = Error("Credential not found"))
        } catch (e: GetCredentialException) {
            Log.e(TAG, e.message.toString())
            CredManagerResult(error = Error("Error fetching the credential"))
        }
    }
}
