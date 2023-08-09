package com.example.credential_manager.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.credential_manager.theme.CredentialManagerTheme
import com.example.credential_manager.viewmodel.CredManagerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val credManagerViewModel: CredManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CredentialManagerTheme {
                HomeScreen(credManagerViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    credManagerViewModel: CredManagerViewModel
) {
    val topAppbarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppbarState)
    val activity: Activity = LocalContext.current as Activity

    credManagerViewModel.createCredentialManager(activity)

    LaunchedEffect(null, block = {
        launch { credManagerViewModel.isCredentialSaved(activity) }
    })

    Column {
        CredManagerAppBar(onNavIconPressed = {},
            title = { Text("Credential Manager") })
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(100.dp))
                UserInput(
                    credManagerViewModel = credManagerViewModel,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onSignIn = { username, password ->
                        credManagerViewModel.signInUser(activity, username, password)
                    }
                )
            }
        }
    }
}
