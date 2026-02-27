package com.tinygc.okodukai

import android.app.Application
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

	override fun onCreate() {
		super.onCreate()

		CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
			initializeDefaultDataUseCase.seedIfEmpty()
		}
	}
}
