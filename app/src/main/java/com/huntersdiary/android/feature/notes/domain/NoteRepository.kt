@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface NoteRepository {
    suspend fun getNotes(query: String?): Result<List<Note>>

    suspend fun getNoteById(id: String): Result<Note>

    suspend fun createNote(
        date: LocalDate?,
        time: LocalTime?,
        location: String?,
        target: String?,
        text: String?,
    ): Result<Note>

    suspend fun updateNote(
        id: String,
        date: LocalDate?,
        time: LocalTime?,
        location: String?,
        target: String?,
        text: String?,
    ): Result<Note>

    suspend fun deleteNote(id: String): Result<Unit>
}
