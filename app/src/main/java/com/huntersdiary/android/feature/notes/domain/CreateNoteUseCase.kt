@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class CreateNoteUseCase(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(
        date: LocalDate?,
        time: LocalTime?,
        location: String,
        target: String,
        text: String,
    ): Result<Note> {
        return repository.createNote(
            date = date,
            time = time,
            location = location.trim().takeIf { it.isNotBlank() },
            target = target.trim().takeIf { it.isNotBlank() },
            text = text.trim().takeIf { it.isNotBlank() },
        )
    }
}
