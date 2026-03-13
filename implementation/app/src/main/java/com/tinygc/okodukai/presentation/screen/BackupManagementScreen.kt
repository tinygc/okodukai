package com.tinygc.okodukai.presentation.screen

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.tinygc.okodukai.presentation.viewmodel.BackupManagementViewModel

@Composable
fun BackupManagementScreen(
    paddingValues: PaddingValues,
    viewModel: BackupManagementViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
        .build()
    val signInClient = GoogleSignIn.getClient(context, signInOptions)

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.result
                val accountName = account?.email ?: account?.account?.name
                if (!accountName.isNullOrBlank()) {
                    viewModel.onAccountSelected(accountName)
                } else {
                    viewModel.onSignInFailed("アカウント情報を取得できませんでした")
                }
            } catch (_: ApiException) {
                viewModel.onSignInFailed("Googleサインインに失敗しました")
            }
        } else {
            viewModel.onSignInFailed("Googleサインインをキャンセルしました")
        }
    }

    LaunchedEffect(Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val accountName = account?.email ?: account?.account?.name
        if (!accountName.isNullOrBlank()) {
            viewModel.onAccountSelected(accountName)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "バックアップ",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            fontWeight = FontWeight.SemiBold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = uiState.accountName?.let { "接続中: $it" } ?: "Googleアカウント未接続",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { signInLauncher.launch(signInClient.signInIntent) },
                    enabled = !uiState.isWorking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Googleでサインイン")
                }
                OutlinedButton(
                    onClick = {
                        signInClient.signOut().addOnCompleteListener {
                            viewModel.onAccountCleared()
                        }
                    },
                    enabled = !uiState.isWorking && uiState.accountName != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("サインアウト")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Google Drive (AppData) にバックアップ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = viewModel::exportBackup,
                    enabled = !uiState.isWorking && uiState.accountName != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export")
                }
                OutlinedButton(
                    onClick = viewModel::importBackup,
                    enabled = !uiState.isWorking && uiState.accountName != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import（全置換）")
                }
            }
        }

        if (uiState.isWorking) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator()
                    Text("処理中です...")
                }
            }
        }

        uiState.errorMessage?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        uiState.successMessage?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        TextButton(onClick = onBack) {
            Text("戻る")
        }
    }
}
