@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.domain

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteUseCaseTest {
    @Test
    fun createNoteTrimsBlankFieldsBeforeRepositoryCall() = runTest {
        val repository = FakeNoteRepository()
        val useCase = CreateNoteUseCase(repository)

        val result = useCase(
            date = LocalDate.parse("2026-05-28"),
            time = LocalTime.parse("12:30"),
            location = "  Лес  ",
            target = "   ",
            text = "  Следы у ручья  ",
        )

        assertTrue(result.isSuccess)
        assertEquals("Лес", repository.createdLocation)
        assertEquals(null, repository.createdTarget)
        assertEquals("Следы у ручья", repository.createdText)
    }

    @Test
    fun updateNotePassesIdAndTrimmedFieldsToRepository() = runTest {
        val repository = FakeNoteRepository()
        val useCase = UpdateNoteUseCase(repository)

        val result = useCase(
            id = "note-id",
            date = null,
            time = null,
            location = "  Озеро  ",
            target = "  Утка  ",
            text = "",
        )

        assertTrue(result.isSuccess)
        assertEquals("note-id", repository.updatedId)
        assertEquals("Озеро", repository.updatedLocation)
        assertEquals("Утка", repository.updatedTarget)
        assertEquals(null, repository.updatedText)
    }

    @Test
    fun deleteNoteCallsRepositoryWithSelectedId() = runTest {
        val repository = FakeNoteRepository()
        val useCase = DeleteNoteUseCase(repository)

        val result = useCase("note-id")

        assertTrue(result.isSuccess)
        assertEquals("note-id", repository.deletedId)
    }

    private class FakeNoteRepository : NoteRepository {
        var createdLocation: String? = null
        var createdTarget: String? = null
        var createdText: String? = null
        var updatedId: String? = null
        var updatedLocation: String? = null
        var updatedTarget: String? = null
        var updatedText: String? = null
        var deletedId: String? = null

        override suspend fun getNotes(query: String?): Result<List<Note>> = error("Not used")

        override suspend fun getNoteById(id: String): Result<Note> = error("Not used")

        override suspend fun createNote(
            date: LocalDate?,
            time: LocalTime?,
            location: String?,
            target: String?,
            text: String?,
        ): Result<Note> {
            createdLocation = location
            createdTarget = target
            createdText = text
            return Result.success(sampleNote())
        }

        override suspend fun updateNote(
            id: String,
            date: LocalDate?,
            time: LocalTime?,
            location: String?,
            target: String?,
            text: String?,
        ): Result<Note> {
            updatedId = id
            updatedLocation = location
            updatedTarget = target
            updatedText = text
            return Result.success(sampleNote(id = id))
        }

        override suspend fun deleteNote(id: String): Result<Unit> {
            deletedId = id
            return Result.success(Unit)
        }
    }

}

private fun sampleNote(id: String = "note-id") = Note(
    id = id,
    date = LocalDate.parse("2026-05-28"),
    time = LocalTime.parse("12:30"),
    location = "Лес",
    target = "Утка",
    text = "Текст",
    createdAt = Instant.parse("2026-05-28T09:00:00Z"),
    updatedAt = Instant.parse("2026-05-28T09:00:00Z"),
)
