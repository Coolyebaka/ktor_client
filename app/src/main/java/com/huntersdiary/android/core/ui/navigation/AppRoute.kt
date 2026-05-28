package com.huntersdiary.android.core.ui.navigation

import android.net.Uri

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object NotesList : AppRoute("notes")
    data object AddNote : AppRoute("notes/add")
    data object NoteDetails : AppRoute("notes/details/{noteId}") {
        const val ARG_NOTE_ID = "noteId"

        fun createRoute(noteId: String): String {
            return "notes/details/${Uri.encode(noteId)}"
        }
    }
    data object EditNote : AppRoute("notes/edit/{noteId}") {
        const val ARG_NOTE_ID = "noteId"

        fun createRoute(noteId: String): String {
            return "notes/edit/${Uri.encode(noteId)}"
        }
    }
}
