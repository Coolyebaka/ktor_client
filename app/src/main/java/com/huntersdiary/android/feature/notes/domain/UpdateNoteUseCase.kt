@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class UpdateNoteUseCase(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(
        id: String,
        date: LocalDate?,
        time: LocalTime?,
        location: String,
        target: String,
        text: String,
    ): Result<Note> {
        return repository.updateNote(
            id = id,
            date = date,
            time = time,
            location = location.trim().takeIf { it.isNotBlank() },
            target = target.trim().takeIf { it.isNotBlank() },
            text = text.trim().takeIf { it.isNotBlank() },
        )
    }
}
