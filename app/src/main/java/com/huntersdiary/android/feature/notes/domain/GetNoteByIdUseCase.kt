package com.huntersdiary.android.feature.notes.domain

class GetNoteByIdUseCase(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(id: String): Result<Note> {
        return repository.getNoteById(id = id)
    }
}
