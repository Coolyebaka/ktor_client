@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.domain

import kotlinx.datetime.Instant

class UpdateNoteUseCase(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(
        id: String,
        dateTime: Instant,
        location: String,
        target: String,
        text: String,
    ): Result<Note> {
        return repository.updateNote(
            id = id,
            dateTime = dateTime,
            location = location.trim(),
            target = target.trim(),
            text = text.trim(),
        )
    }
}
