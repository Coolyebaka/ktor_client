package com.huntersdiary.android.feature.notes.domain

class GetNotesUseCase(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(query: String?): Result<List<Note>> {
        return repository.getNotes(query = query?.trim()?.takeIf { it.isNotBlank() })
    }
}
