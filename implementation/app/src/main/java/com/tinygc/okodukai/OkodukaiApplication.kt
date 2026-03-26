package com.tinygc.okodukai

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.tinygc.okodukai.domain.repository.BillingRepository
import com.tinygc.okodukai.domain.usecase.setup.InitializeDefaultDataUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * おこづかいアプリのApplicationクラス
 */
@HiltAndroidApp
class OkodukaiApplication : Application() {

	@Inject
	lateinit var initializeDefaultDataUseCase: InitializeDefaultDataUseCase

	@Inject
	lateinit var billingRepository: BillingRepository

	override fun onCreate() {
		super.onCreate()

		MobileAds.initialize(this)

		CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
			initializeDefaultDataUseCase.seedIfEmpty()
			billingRepository.initialize()
		}
	}
}
