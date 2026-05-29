@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.presentation

import com.huntersdiary.android.MainDispatcherRule
import com.huntersdiary.android.core.storage.SearchHistoryRepository
import com.huntersdiary.android.core.storage.SearchHistoryScope
import com.huntersdiary.android.feature.notes.domain.GetNoteByIdUseCase
import com.huntersdiary.android.feature.notes.domain.GetNotesUseCase
import com.huntersdiary.android.feature.notes.domain.Note
import com.huntersdiary.android.feature.notes.domain.NoteRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotesListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialLoadShowsLoadingThenSuccess() = runTest {
        val releaseRequest = CompletableDeferred<Unit>()
        val repository = FakeNoteRepository(
            results = mutableListOf(Result.success(listOf(sampleNote()))),
            beforeReturn = { releaseRequest.await() },
        )
        val viewModel = NotesListViewModel(
            getNotesUseCase = GetNotesUseCase(repository),
            getNoteByIdUseCase = GetNoteByIdUseCase(repository),
            searchHistoryRepository = FakeSearchHistoryRepository(),
        )

        assertTrue(viewModel.uiState.value.isLoading)

        releaseRequest.complete(Unit)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(sampleNote()), viewModel.uiState.value.notes)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun initialLoadHandlesEmptyResult() = runTest {
        val repository = FakeNoteRepository(mutableListOf(Result.success(emptyList())))
        val viewModel = NotesListViewModel(
            getNotesUseCase = GetNotesUseCase(repository),
            getNoteByIdUseCase = GetNoteByIdUseCase(repository),
            searchHistoryRepository = FakeSearchHistoryRepository(),
        )

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.notes.isEmpty())
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun initialLoadHandlesError() = runTest {
        val repository = FakeNoteRepository(mutableListOf(Result.failure(IllegalStateException("Ошибка поиска"))))
        val viewModel = NotesListViewModel(
            getNotesUseCase = GetNotesUseCase(
                repository,
            ),
            getNoteByIdUseCase = GetNoteByIdUseCase(repository),
            searchHistoryRepository = FakeSearchHistoryRepository(),
        )

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.notes.isEmpty())
        assertEquals("Ошибка поиска", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun retryUsesLastQuery() = runTest {
        val repository = FakeNoteRepository(
            results = mutableListOf(
                Result.failure(IllegalStateException("Ошибка поиска")),
                Result.failure(IllegalStateException("Ошибка поиска")),
                Result.success(listOf(sampleNote())),
            ),
        )
        val viewModel = NotesListViewModel(
            getNotesUseCase = GetNotesUseCase(repository),
            getNoteByIdUseCase = GetNoteByIdUseCase(repository),
            searchHistoryRepository = FakeSearchHistoryRepository(),
        )

        viewModel.onQueryChange("утка")
        viewModel.search()
        viewModel.retry()

        assertEquals(listOf(null, "утка", "утка"), repository.queries)
        assertEquals(listOf(sampleNote()), viewModel.uiState.value.notes)
    }

    private class FakeNoteRepository(
        private val results: MutableList<Result<List<Note>>>,
        private val beforeReturn: suspend () -> Unit = {},
    ) : NoteRepository {
        val queries = mutableListOf<String?>()

        override suspend fun getNotes(query: String?): Result<List<Note>> {
            queries.add(query)
            beforeReturn()
            return results.removeFirst()
        }

        override suspend fun getNoteById(id: String): Result<Note> = error("Not used")

        override suspend fun createNote(
            date: LocalDate?,
            time: LocalTime?,
            location: String?,
            target: String?,
            text: String?,
        ): Result<Note> = error("Not used")

        override suspend fun updateNote(
            id: String,
            date: LocalDate?,
            time: LocalTime?,
            location: String?,
            target: String?,
            text: String?,
        ): Result<Note> = error("Not used")

        override suspend fun deleteNote(id: String): Result<Unit> = error("Not used")
    }

    private class FakeSearchHistoryRepository : SearchHistoryRepository {
        private val history = MutableStateFlow(emptyList<String>())

        override fun observeHistory(scope: SearchHistoryScope): Flow<List<String>> = history

        override suspend fun addHistoryItem(scope: SearchHistoryScope, query: String) {
            history.value = listOf(query) + history.value
        }

        override suspend fun removeHistoryItem(scope: SearchHistoryScope, query: String) {
            history.value = history.value.filterNot { item -> item == query }
        }

        override suspend fun clearHistory(scope: SearchHistoryScope) {
            history.value = emptyList()
        }
    }

    private fun sampleNote() = Note(
        id = "note-id",
        date = LocalDate.parse("2026-05-28"),
        time = LocalTime.parse("12:00"),
        location = "Лес",
        target = "Утка",
        text = "Текст заметки",
        createdAt = Instant.parse("2026-05-28T13:00:00Z"),
        updatedAt = Instant.parse("2026-05-28T13:00:00Z"),
    )
}
