@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.data

import com.huntersdiary.android.feature.notes.domain.Note
import com.huntersdiary.android.feature.notes.domain.NoteRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.datetime.Instant

class NoteRepositoryImpl(
    private val api: NotesApi,
) : NoteRepository {
    override suspend fun getNotes(query: String?): Result<List<Note>> {
        return runNoteRequest {
            api.getNotes(query = query).map { response -> response.toDomain() }
        }
    }

    override suspend fun getNoteById(id: String): Result<Note> {
        return runNoteRequest {
            api.getNoteById(id = id).toDomain()
        }
    }

    override suspend fun createNote(
        dateTime: Instant,
        location: String,
        target: String,
        text: String,
    ): Result<Note> {
        return runNoteRequest {
            api.createNote(
                toCreateNoteRequest(
                    dateTime = dateTime,
                    location = location,
                    target = target,
                    text = text,
                ),
            ).toDomain()
        }
    }

    override suspend fun updateNote(
        id: String,
        dateTime: Instant,
        location: String,
        target: String,
        text: String,
    ): Result<Note> {
        return runNoteRequest {
            api.updateNote(
                id = id,
                request = toUpdateNoteRequest(
                    dateTime = dateTime,
                    location = location,
                    target = target,
                    text = text,
                ),
            ).toDomain()
        }
    }

    override suspend fun deleteNote(id: String): Result<Unit> {
        return runNoteRequest {
            api.deleteNote(id = id)
        }
    }

    private suspend fun <T> runNoteRequest(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: ResponseException) {
            Result.failure(NoteRequestException(exception.apiMessage()))
        } catch (exception: IOException) {
            Result.failure(NoteRequestException("Не удалось подключиться к серверу"))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(NoteRequestException("Не удалось выполнить запрос"))
        }
    }

    private suspend fun ResponseException.apiMessage(): String {
        return runCatching { response.body<NoteApiError>().message }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: when (response.status.value) {
                401 -> "Войдите в аккаунт заново"
                404 -> "Заметка не найдена"
                else -> "Ошибка сервера"
            }
    }
}

class NoteRequestException(message: String) : Exception(message)
