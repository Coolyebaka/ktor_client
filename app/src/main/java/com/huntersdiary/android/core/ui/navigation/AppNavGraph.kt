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
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.huntersdiary.android.feature.auth.presentation.AuthViewModel
import com.huntersdiary.android.feature.auth.presentation.LoginScreen
import com.huntersdiary.android.feature.auth.presentation.RegisterScreen
import com.huntersdiary.android.feature.notes.presentation.AddEditNoteScreen
import com.huntersdiary.android.feature.notes.presentation.AddEditNoteViewModel
import com.huntersdiary.android.feature.notes.presentation.NoteDetailsScreen
import com.huntersdiary.android.feature.notes.presentation.NoteDetailsViewModel
import com.huntersdiary.android.feature.notes.presentation.NotesListScreen
import com.huntersdiary.android.feature.notes.presentation.NotesListViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
) {
    val state by authViewModel.uiState.collectAsStateWithLifecycle()

    if (state.isCheckingAuth) {
        LoadingScreen()
        return
    }

    val navController = rememberNavController()

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated && navController.currentDestination?.route != AppRoute.NotesList.route) {
            navController.navigate(AppRoute.NotesList.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (state.isAuthenticated) AppRoute.NotesList.route else AppRoute.Login.route,
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                state = state,
                onLogin = authViewModel::login,
                onRegisterClick = { navController.navigate(AppRoute.Register.route) },
            )
        }
        composable(AppRoute.Register.route) {
            RegisterScreen(
                state = state,
                onRegister = authViewModel::register,
                onLoginClick = { navController.popBackStack() },
            )
        }
        composable(AppRoute.NotesList.route) {
            val viewModel: NotesListViewModel = koinViewModel()
            val notesState by viewModel.uiState.collectAsStateWithLifecycle()

            NotesListScreen(
                state = notesState,
                onQueryChange = viewModel::onQueryChange,
                onSearch = viewModel::search,
                onClearQuery = viewModel::clearQuery,
                onRetry = viewModel::retry,
                onRefresh = viewModel::refreshCurrent,
                onAddClick = { navController.navigate(AppRoute.AddNote.route) },
                onNoteClick = { noteId ->
                    navController.navigate(AppRoute.NoteDetails.createRoute(noteId))
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
                onDateTimeChange = viewModel::onDateTimeChange,
                onLocationChange = viewModel::onLocationChange,
                onTargetChange = viewModel::onTargetChange,
                onTextChange = viewModel::onTextChange,
                onSave = viewModel::save,
                onRetry = viewModel::retryLoad,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
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
                onDateTimeChange = viewModel::onDateTimeChange,
                onLocationChange = viewModel::onLocationChange,
                onTargetChange = viewModel::onTargetChange,
                onTextChange = viewModel::onTextChange,
                onSave = viewModel::save,
                onRetry = viewModel::retryLoad,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
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
                onDeleted = { navController.popBackStack() },
                onDeleteHandled = viewModel::consumeDeleteCompleted,
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
