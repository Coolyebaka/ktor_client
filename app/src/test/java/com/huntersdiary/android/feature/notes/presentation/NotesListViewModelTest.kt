@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.presentation

import com.huntersdiary.android.MainDispatcherRule
import com.huntersdiary.android.core.storage.SearchHistoryRepository
import com.huntersdiary.android.core.storage.SearchHistoryScope
import com.huntersdiary.android.feature.notes.domain.GetNotesUseCase
import com.huntersdiary.android.feature.notes.domain.Note
import com.huntersdiary.android.feature.notes.domain.NoteRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
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
        val viewModel = NotesListViewModel(
            getNotesUseCase = GetNotesUseCase(FakeNoteRepository(mutableListOf(Result.success(emptyList())))),
            searchHistoryRepository = FakeSearchHistoryRepository(),
        )

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.notes.isEmpty())
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun initialLoadHandlesError() = runTest {
        val viewModel = NotesListViewModel(
            getNotesUseCase = GetNotesUseCase(
                FakeNoteRepository(mutableListOf(Result.failure(IllegalStateException("Ошибка поиска")))),
            ),
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
            dateTime: Instant,
            location: String,
            target: String,
            text: String,
        ): Result<Note> = error("Not used")

        override suspend fun updateNote(
            id: String,
            dateTime: Instant,
            location: String,
            target: String,
            text: String,
        ): Result<Note> = error("Not used")

        override suspend fun deleteNote(id: String): Result<Unit> = error("Not used")
    }

    private class FakeSearchHistoryRepository : SearchHistoryRepository {
        private val history = MutableStateFlow(emptyList<String>())

        override fun observeHistory(scope: SearchHistoryScope): Flow<List<String>> = history

        override suspend fun addHistoryItem(scope: SearchHistoryScope, query: String) {
            history.value = listOf(query) + history.value
        }

        override suspend fun clearHistory(scope: SearchHistoryScope) {
            history.value = emptyList()
        }
    }

    private fun sampleNote() = Note(
        id = "note-id",
        dateTime = Instant.parse("2026-05-28T12:00:00Z"),
        location = "Лес",
        target = "Утка",
        text = "Текст заметки",
        createdAt = Instant.parse("2026-05-28T13:00:00Z"),
        updatedAt = Instant.parse("2026-05-28T13:00:00Z"),
    )
}
