package com.huntersdiary.android.core.di

import com.huntersdiary.android.core.network.provideHttpClient
import com.huntersdiary.android.core.storage.TokenStorage
import com.huntersdiary.android.feature.auth.data.AuthApi
import com.huntersdiary.android.feature.auth.data.AuthRepositoryImpl
import com.huntersdiary.android.feature.auth.domain.AuthRepository
import com.huntersdiary.android.feature.auth.domain.LoginUseCase
import com.huntersdiary.android.feature.auth.domain.RegisterUseCase
import com.huntersdiary.android.feature.auth.presentation.AuthViewModel
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
    single { TokenStorage(androidContext()) }
    single { provideHttpClient(get(), get()) }
    single { AuthApi(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
}
