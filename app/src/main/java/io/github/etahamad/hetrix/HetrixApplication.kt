package io.github.etahamad.hetrix

import android.app.Application
import io.github.etahamad.hetrix.data.api.AuthInterceptor
import io.github.etahamad.hetrix.data.api.HetrixApiService
import io.github.etahamad.hetrix.data.api.NetworkUtils
import io.github.etahamad.hetrix.data.local.EncryptedTokenStorage
import io.github.etahamad.hetrix.data.local.TokenStorage
import io.github.etahamad.hetrix.data.repository.MonitorRepository
import io.github.etahamad.hetrix.data.repository.MonitorRepositoryImpl
import io.github.etahamad.hetrix.ui.util.HetrixViewModelFactory

/**
 * Application entry point for dependency initialization and app-wide service container.
 */
class HetrixApplication : Application() {

    lateinit var tokenStorage: TokenStorage
        private set

    lateinit var apiService: HetrixApiService
        private set

    lateinit var monitorRepository: MonitorRepository
        private set

    lateinit var viewModelFactory: HetrixViewModelFactory
        private set

    override fun onCreate() {
        super.onCreate()

        tokenStorage = EncryptedTokenStorage(applicationContext)

        val authInterceptor = AuthInterceptor(tokenStorage)
        val okHttpClient = NetworkUtils.createOkHttpClient(
            authInterceptor = authInterceptor,
            isDebug = BuildConfig.DEBUG
        )

        apiService = NetworkUtils.createApiService(okHttpClient)

        monitorRepository = MonitorRepositoryImpl(
            apiService = apiService,
            tokenStorage = tokenStorage
        )

        viewModelFactory = HetrixViewModelFactory(monitorRepository)
    }
}
