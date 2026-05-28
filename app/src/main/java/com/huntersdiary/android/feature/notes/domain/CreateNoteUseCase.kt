@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.domain

import kotlinx.datetime.Instant

class CreateNoteUseCase(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(
        dateTime: Instant,
        location: String,
        target: String,
        text: String,
    ): Result<Note> {
        return repository.createNote(
            dateTime = dateTime,
            location = location.trim(),
            target = target.trim(),
            text = text.trim(),
        )
    }
}
