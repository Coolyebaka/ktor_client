package com.huntersdiary.android.core.di

import com.huntersdiary.android.core.network.provideHttpClient
import com.huntersdiary.android.core.storage.DataStoreSearchHistoryRepository
import com.huntersdiary.android.core.storage.DataStoreThemeSettingsRepository
import com.huntersdiary.android.core.storage.SearchHistoryRepository
import com.huntersdiary.android.core.storage.ThemeSettingsRepository
import com.huntersdiary.android.core.storage.TokenStorage
import com.huntersdiary.android.core.ui.theme.ThemeViewModel
import com.huntersdiary.android.feature.auth.data.AuthApi
import com.huntersdiary.android.feature.auth.data.AuthRepositoryImpl
import com.huntersdiary.android.feature.auth.domain.AuthRepository
import com.huntersdiary.android.feature.auth.domain.LoginUseCase
import com.huntersdiary.android.feature.auth.domain.RegisterUseCase
import com.huntersdiary.android.feature.auth.presentation.AuthViewModel
import com.huntersdiary.android.feature.notes.data.NoteRepositoryImpl
import com.huntersdiary.android.feature.notes.data.LocalNoteStorage
import com.huntersdiary.android.feature.notes.data.NotesApi
import com.huntersdiary.android.feature.notes.domain.CreateNoteUseCase
import com.huntersdiary.android.feature.notes.domain.DeleteNoteUseCase
import com.huntersdiary.android.feature.notes.domain.GetNoteByIdUseCase
import com.huntersdiary.android.feature.notes.domain.GetNotesUseCase
import com.huntersdiary.android.feature.notes.domain.NoteRepository
import com.huntersdiary.android.feature.notes.domain.UpdateNoteUseCase
import com.huntersdiary.android.feature.notes.presentation.AddEditNoteViewModel
import com.huntersdiary.android.feature.notes.presentation.NoteDetailsViewModel
import com.huntersdiary.android.feature.notes.presentation.NotesListViewModel
import com.huntersdiary.android.feature.rules.data.RuleRepositoryImpl
import com.huntersdiary.android.feature.rules.data.LocalRuleStorage
import com.huntersdiary.android.feature.rules.data.RulesApi
import com.huntersdiary.android.feature.rules.domain.GetRuleByIdUseCase
import com.huntersdiary.android.feature.rules.domain.GetRulesUseCase
import com.huntersdiary.android.feature.rules.domain.RuleRepository
import com.huntersdiary.android.feature.rules.presentation.RuleDetailsViewModel
import com.huntersdiary.android.feature.rules.presentation.RulesListViewModel
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
    single { TokenStorage(androidContext(), get()) }
    single<SearchHistoryRepository> { DataStoreSearchHistoryRepository(androidContext(), get()) }
    single<ThemeSettingsRepository> { DataStoreThemeSettingsRepository(androidContext()) }
    single { provideHttpClient(get(), get()) }
    viewModel { ThemeViewModel(get()) }
    single { AuthApi(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    viewModel { AuthViewModel(get(), get(), get()) }

    single { NotesApi(get()) }
    single { LocalNoteStorage(androidContext(), get(), get()) }
    single<NoteRepository> { NoteRepositoryImpl(get(), get()) }
    factory { GetNotesUseCase(get()) }
    factory { GetNoteByIdUseCase(get()) }
    factory { CreateNoteUseCase(get()) }
    factory { UpdateNoteUseCase(get()) }
    factory { DeleteNoteUseCase(get()) }
    viewModel { NotesListViewModel(get(), get(), get()) }
    viewModel { (noteId: String) ->
        AddEditNoteViewModel(
            noteId = noteId.takeIf { it.isNotBlank() },
            getNoteByIdUseCase = get(),
            createNoteUseCase = get(),
            updateNoteUseCase = get(),
        )
    }
    viewModel { (noteId: String) ->
        NoteDetailsViewModel(
            noteId = noteId,
            getNoteByIdUseCase = get(),
            deleteNoteUseCase = get(),
        )
    }

    single { RulesApi(get()) }
    single { LocalRuleStorage(androidContext(), get()) }
    single<RuleRepository> { RuleRepositoryImpl(get(), get()) }
    factory { GetRulesUseCase(get()) }
    factory { GetRuleByIdUseCase(get()) }
    viewModel { RulesListViewModel(get(), get()) }
    viewModel { (ruleId: String) ->
        RuleDetailsViewModel(
            ruleId = ruleId,
            getRuleByIdUseCase = get(),
        )
    }
}
