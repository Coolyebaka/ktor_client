package com.huntersdiary.android.core.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.huntersdiary.android.core.ui.main.MainScreen
import com.huntersdiary.android.feature.auth.presentation.AuthViewModel
import com.huntersdiary.android.feature.auth.presentation.LoginScreen
import com.huntersdiary.android.feature.auth.presentation.RegisterScreen
import com.huntersdiary.android.feature.notes.presentation.AddEditNoteScreen
import com.huntersdiary.android.feature.notes.presentation.AddEditNoteViewModel
import com.huntersdiary.android.feature.notes.presentation.NoteDetailsScreen
import com.huntersdiary.android.feature.notes.presentation.NoteDetailsViewModel
import com.huntersdiary.android.feature.notes.presentation.NotesListScreen
import com.huntersdiary.android.feature.notes.presentation.NotesListViewModel
import com.huntersdiary.android.feature.rules.presentation.RuleDetailsScreen
import com.huntersdiary.android.feature.rules.presentation.RuleDetailsViewModel
import com.huntersdiary.android.feature.rules.presentation.RulesListScreen
import com.huntersdiary.android.feature.rules.presentation.RulesListViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private const val NOTE_CHANGED_ID_KEY = "note_changed_id"
private const val NOTE_CHANGED_DELETED_KEY = "note_changed_deleted"

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
) {
    val state by authViewModel.uiState.collectAsStateWithLifecycle()

    if (state.isCheckingAuth) {
        LoadingScreen()
        return
    }

    val navController = rememberNavController()
    val signalNoteChanged: (String, Boolean) -> Unit = { noteId, isDeleted ->
        runCatching {
            val mainBackStackEntry = navController.getBackStackEntry(AppRoute.Main.route)
            mainBackStackEntry.savedStateHandle[NOTE_CHANGED_ID_KEY] = noteId
            mainBackStackEntry.savedStateHandle[NOTE_CHANGED_DELETED_KEY] = isDeleted
        }
    }

    LaunchedEffect(state.isAuthenticated) {
        val targetRoute = if (state.isAuthenticated) AppRoute.Main.route else AppRoute.Login.route
        if (navController.currentDestination?.route != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (state.isAuthenticated) AppRoute.Main.route else AppRoute.Login.route,
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                state = state,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onLogin = { authViewModel.login(state.email, state.password) },
                onRegisterClick = { navController.navigate(AppRoute.Register.route) },
            )
        }
        composable(AppRoute.Register.route) {
            RegisterScreen(
                state = state,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onRegister = { authViewModel.register(state.email, state.password) },
                onLoginClick = { navController.popBackStack() },
            )
        }
        composable(AppRoute.Main.route) { mainBackStackEntry ->
            val notesViewModel: NotesListViewModel = koinViewModel()
            val notesState by notesViewModel.uiState.collectAsStateWithLifecycle()
            val rulesViewModel: RulesListViewModel = koinViewModel()
            val rulesState by rulesViewModel.uiState.collectAsStateWithLifecycle()
            val changedNoteId by mainBackStackEntry.savedStateHandle
                .getStateFlow<String?>(NOTE_CHANGED_ID_KEY, null)
                .collectAsStateWithLifecycle()
            val changedNoteDeleted by mainBackStackEntry.savedStateHandle
                .getStateFlow(NOTE_CHANGED_DELETED_KEY, false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(changedNoteId, changedNoteDeleted) {
                val noteId = changedNoteId
                if (noteId != null) {
                    notesViewModel.onNoteCrudChanged(
                        noteId = noteId,
                        isDeleted = changedNoteDeleted,
                    )
                    mainBackStackEntry.savedStateHandle[NOTE_CHANGED_ID_KEY] = null
                    mainBackStackEntry.savedStateHandle[NOTE_CHANGED_DELETED_KEY] = false
                }
            }

            MainScreen(
                isDarkTheme = isDarkTheme,
                onDarkThemeChange = onDarkThemeChange,
                onLogoutClick = authViewModel::logout,
                isRefreshing = notesState.isRefreshing || rulesState.isRefreshing,
                onRefreshAll = {
                    notesViewModel.refreshCurrent()
                    rulesViewModel.refreshCurrent()
                },
                notesContent = {
                    NotesListScreen(
                        state = notesState,
                        onQueryChange = notesViewModel::onQueryChange,
                        onSearch = notesViewModel::search,
                        onClearQuery = notesViewModel::clearQuery,
                        onRetry = notesViewModel::retry,
                        onHistoryClick = notesViewModel::onHistoryClick,
                        onRemoveHistoryItem = notesViewModel::removeHistoryItem,
                        onClearHistory = notesViewModel::clearHistory,
                        onAddClick = { navController.navigate(AppRoute.AddNote.route) },
                        onNoteClick = { noteId ->
                            notesViewModel.onSearchResultClick()
                            navController.navigate(AppRoute.NoteDetails.createRoute(noteId))
                        },
                    )
                },
                rulesContent = {
                    RulesListScreen(
                        state = rulesState,
                        onQueryChange = rulesViewModel::onQueryChange,
                        onSearch = rulesViewModel::search,
                        onClearQuery = rulesViewModel::clearQuery,
                        onRetry = rulesViewModel::retry,
                        onHistoryClick = rulesViewModel::onHistoryClick,
                        onRemoveHistoryItem = rulesViewModel::removeHistoryItem,
                        onClearHistory = rulesViewModel::clearHistory,
                        onRuleClick = { ruleId ->
                            rulesViewModel.onSearchResultClick()
                            navController.navigate(AppRoute.RuleDetails.createRoute(ruleId))
                        },
                    )
                },
            )
        }
        composable(AppRoute.AddNote.route) {
            val viewModel: AddEditNoteViewModel = koinViewModel {
                parametersOf("")
            }
            val noteState by viewModel.uiState.collectAsStateWithLifecycle()

            AddEditNoteScreen(
                state = noteState,
                onDateChange = viewModel::onDateChange,
                onTimeChange = viewModel::onTimeChange,
                onLocationChange = viewModel::onLocationChange,
                onTargetChange = viewModel::onTargetChange,
                onTextChange = viewModel::onTextChange,
                onSave = viewModel::save,
                onRetry = viewModel::retryLoad,
                onBackClick = { navController.popBackStack() },
                onSaved = { noteId ->
                    signalNoteChanged(noteId, false)
                    navController.popBackStack(AppRoute.Main.route, false)
                },
                onSaveHandled = viewModel::consumeSaveCompleted,
            )
        }
        composable(
            route = AppRoute.EditNote.route,
            arguments = listOf(
                navArgument(AppRoute.EditNote.ARG_NOTE_ID) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString(AppRoute.EditNote.ARG_NOTE_ID).orEmpty()
            val viewModel: AddEditNoteViewModel = koinViewModel {
                parametersOf(noteId)
            }
            val noteState by viewModel.uiState.collectAsStateWithLifecycle()

            AddEditNoteScreen(
                state = noteState,
                onDateChange = viewModel::onDateChange,
                onTimeChange = viewModel::onTimeChange,
                onLocationChange = viewModel::onLocationChange,
                onTargetChange = viewModel::onTargetChange,
                onTextChange = viewModel::onTextChange,
                onSave = viewModel::save,
                onRetry = viewModel::retryLoad,
                onBackClick = { navController.popBackStack() },
                onSaved = { noteId ->
                    signalNoteChanged(noteId, false)
                    navController.popBackStack(AppRoute.Main.route, false)
                },
                onSaveHandled = viewModel::consumeSaveCompleted,
            )
        }
        composable(
            route = AppRoute.NoteDetails.route,
            arguments = listOf(
                navArgument(AppRoute.NoteDetails.ARG_NOTE_ID) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString(AppRoute.NoteDetails.ARG_NOTE_ID).orEmpty()
            val viewModel: NoteDetailsViewModel = koinViewModel {
                parametersOf(noteId)
            }
            val noteState by viewModel.uiState.collectAsStateWithLifecycle()

            NoteDetailsScreen(
                state = noteState,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(AppRoute.EditNote.createRoute(id))
                },
                onDeleteClick = viewModel::deleteNote,
                onRetry = viewModel::retry,
                onRefresh = viewModel::refresh,
                onDeleted = {
                    signalNoteChanged(noteId, true)
                    navController.popBackStack()
                },
                onDeleteHandled = viewModel::consumeDeleteCompleted,
            )
        }
        composable(
            route = AppRoute.RuleDetails.route,
            arguments = listOf(
                navArgument(AppRoute.RuleDetails.ARG_RULE_ID) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val ruleId = backStackEntry.arguments?.getString(AppRoute.RuleDetails.ARG_RULE_ID).orEmpty()
            val viewModel: RuleDetailsViewModel = koinViewModel {
                parametersOf(ruleId)
            }
            val ruleState by viewModel.uiState.collectAsStateWithLifecycle()

            RuleDetailsScreen(
                state = ruleState,
                onBackClick = { navController.popBackStack() },
                onRetry = viewModel::retry,
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
