package com.huntersdiary.android.feature.notes.domain

class DeleteNoteUseCase(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.deleteNote(id = id)
    }
}
