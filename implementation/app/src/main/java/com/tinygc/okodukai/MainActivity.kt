package com.tinygc.okodukai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tinygc.okodukai.domain.repository.BillingRepository
import com.tinygc.okodukai.presentation.MainScreen
import com.tinygc.okodukai.presentation.theme.OkodukaiTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var billingRepository: BillingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OkodukaiTheme {
                MainScreen(billingRepository = billingRepository)
            }
        }
    }
}
