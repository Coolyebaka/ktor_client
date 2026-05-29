@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.data

import com.huntersdiary.android.feature.notes.domain.Note
import com.huntersdiary.android.feature.notes.domain.NoteRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class NoteRepositoryImpl(
    private val api: NotesApi,
    private val localNoteStorage: LocalNoteStorage,
) : NoteRepository {
    override suspend fun getNotes(query: String?): Result<List<Note>> {
        return try {
            syncPendingNotes()
            val remoteNotes = api.getNotes(query = query).map { response -> response.toDomain() }
            if (query.isNullOrBlank()) {
                localNoteStorage.replaceSyncedNotes(remoteNotes)
            } else {
                remoteNotes.forEach { note -> localNoteStorage.upsert(note) }
            }
            Result.success(localNoteStorage.getNotes(query))
        } catch (exception: ResponseException) {
            Result.failure(NoteRequestException(exception.apiMessage()))
        } catch (exception: IOException) {
            Result.success(localNoteStorage.getNotes(query))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(NoteRequestException("Не удалось выполнить запрос"))
        }
    }

    override suspend fun getNoteById(id: String): Result<Note> {
        localNoteStorage.getNoteById(id)?.let { note ->
            if (!note.isSynced) return Result.success(note)
        }

        return try {
            val note = api.getNoteById(id = id).toDomain()
            localNoteStorage.upsert(note)
            Result.success(note)
        } catch (exception: ResponseException) {
            Result.failure(NoteRequestException(exception.apiMessage()))
        } catch (exception: IOException) {
            localNoteStorage.getNoteById(id)
                ?.let { note -> Result.success(note) }
                ?: Result.failure(NoteRequestException("Не удалось подключиться к серверу"))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(NoteRequestException("Не удалось выполнить запрос"))
        }
    }

    override suspend fun createNote(
        date: LocalDate?,
        time: LocalTime?,
        location: String?,
        target: String?,
        text: String?,
    ): Result<Note> {
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        return try {
            val note = api.createNote(
                toCreateNoteRequest(
                    date = date,
                    time = time,
                    location = location,
                    target = target,
                    text = text,
                    createdAt = now,
                    updatedAt = now,
                ),
            ).toDomain()
            localNoteStorage.upsert(note)
            Result.success(note)
        } catch (exception: ResponseException) {
            Result.failure(NoteRequestException(exception.apiMessage()))
        } catch (exception: IOException) {
            val localNote = Note(
                id = "local-${now.toEpochMilliseconds()}",
                date = date,
                time = time,
                location = location,
                target = target,
                text = text,
                createdAt = now,
                updatedAt = now,
                isSynced = false,
                pendingDelete = false,
            )
            localNoteStorage.upsert(localNote)
            Result.success(localNote)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(NoteRequestException("Не удалось выполнить запрос"))
        }
    }

    override suspend fun updateNote(
        id: String,
        date: LocalDate?,
        time: LocalTime?,
        location: String?,
        target: String?,
        text: String?,
    ): Result<Note> {
        val existingLocalNote = localNoteStorage.getNoteById(id)
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val createdAt = existingLocalNote?.createdAt ?: now
        if (existingLocalNote?.isSynced == false) {
            val updatedLocalNote = existingLocalNote.copy(
                date = date,
                time = time,
                location = location,
                target = target,
                text = text,
                updatedAt = now,
                isSynced = false,
                pendingDelete = false,
            )
            localNoteStorage.upsert(updatedLocalNote)
            return try {
                val syncedNote = if (updatedLocalNote.id.startsWith("local-")) {
                    api.createNote(
                        request = toCreateNoteRequest(
                            date = updatedLocalNote.date,
                            time = updatedLocalNote.time,
                            location = updatedLocalNote.location,
                            target = updatedLocalNote.target,
                            text = updatedLocalNote.text,
                            createdAt = updatedLocalNote.createdAt,
                            updatedAt = updatedLocalNote.updatedAt,
                        ),
                    ).toDomain()
                } else {
                    api.updateNote(
                        id = id,
                        request = toUpdateNoteRequest(
                            date = updatedLocalNote.date,
                            time = updatedLocalNote.time,
                            location = updatedLocalNote.location,
                            target = updatedLocalNote.target,
                            text = updatedLocalNote.text,
                            createdAt = updatedLocalNote.createdAt,
                            updatedAt = updatedLocalNote.updatedAt,
                        ),
                    ).toDomain()
                }
                if (updatedLocalNote.id.startsWith("local-")) {
                    localNoteStorage.delete(updatedLocalNote.id)
                }
                localNoteStorage.upsert(syncedNote)
                Result.success(syncedNote)
            } catch (exception: ResponseException) {
                Result.failure(NoteRequestException(exception.apiMessage()))
            } catch (exception: IOException) {
                Result.success(updatedLocalNote)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Result.failure(NoteRequestException("Не удалось выполнить запрос"))
            }
        }

        return try {
            val note = api.updateNote(
                id = id,
                request = toUpdateNoteRequest(
                    date = date,
                    time = time,
                    location = location,
                    target = target,
                    text = text,
                    createdAt = createdAt,
                    updatedAt = now,
                ),
            ).toDomain()
            localNoteStorage.upsert(note)
            Result.success(note)
        } catch (exception: ResponseException) {
            Result.failure(NoteRequestException(exception.apiMessage()))
        } catch (exception: IOException) {
            val localNote = Note(
                id = id,
                date = date,
                time = time,
                location = location,
                target = target,
                text = text,
                createdAt = createdAt,
                updatedAt = now,
                isSynced = false,
                pendingDelete = false,
            )
            localNoteStorage.upsert(localNote)
            Result.success(localNote)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(NoteRequestException("Не удалось выполнить запрос"))
        }
    }

    override suspend fun deleteNote(id: String): Result<Unit> {
        val existingLocalNote = localNoteStorage.getNoteById(id)
        if (existingLocalNote?.id?.startsWith("local-") == true) {
            localNoteStorage.delete(id)
            return Result.success(Unit)
        }

        return try {
            api.deleteNote(id = id)
            localNoteStorage.delete(id)
            Result.success(Unit)
        } catch (exception: ResponseException) {
            Result.failure(NoteRequestException(exception.apiMessage()))
        } catch (exception: IOException) {
            if (existingLocalNote == null) {
                Result.failure(NoteRequestException("Не удалось подключиться к серверу"))
            } else {
                localNoteStorage.upsert(
                    existingLocalNote.copy(
                        updatedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                        isSynced = false,
                        pendingDelete = true,
                    ),
                )
                Result.success(Unit)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(NoteRequestException("Не удалось выполнить запрос"))
        }
    }

    private suspend fun syncPendingNotes() {
        localNoteStorage.getAllNotes(includePendingDeletes = true)
            .filter { note -> !note.isSynced }
            .forEach { note ->
                val syncResult = runCatching {
                    if (note.pendingDelete) {
                        if (!note.id.startsWith("local-")) {
                            try {
                                api.deleteNote(id = note.id)
                            } catch (exception: ResponseException) {
                                if (exception.response.status.value != 404) throw exception
                            }
                        }
                        null
                    } else if (note.id.startsWith("local-")) {
                        api.createNote(
                            request = toCreateNoteRequest(
                                date = note.date,
                                time = note.time,
                                location = note.location,
                                target = note.target,
                                text = note.text,
                                createdAt = note.createdAt,
                                updatedAt = note.updatedAt,
                            ),
                        ).toDomain()
                    } else {
                        api.updateNote(
                            id = note.id,
                            request = toUpdateNoteRequest(
                                date = note.date,
                                time = note.time,
                                location = note.location,
                                target = note.target,
                                text = note.text,
                                createdAt = note.createdAt,
                                updatedAt = note.updatedAt,
                            ),
                        ).toDomain()
                    }
                }

                if (syncResult.isFailure) return@forEach

                if (note.pendingDelete) {
                    localNoteStorage.delete(note.id)
                    return@forEach
                }

                val syncedNote = syncResult.getOrNull() ?: return@forEach

                if (note.id.startsWith("local-")) {
                    localNoteStorage.delete(note.id)
                }
                localNoteStorage.upsert(syncedNote)
            }
    }

    private suspend fun ResponseException.apiMessage(): String {
        return when (response.status.value) {
            400, 422 -> "Проверьте поля заметки"
            401 -> "Войдите в аккаунт заново"
            404 -> "Заметка не найдена"
            else -> runCatching { response.body<NoteApiError>().message }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "Ошибка сервера"
        }
    }
}

class NoteRequestException(message: String) : Exception(message)
