@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.huntersdiary.android.core.storage.TokenStorage
import com.huntersdiary.android.feature.notes.domain.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.localNotesDataStore by preferencesDataStore(name = "local_notes")

class LocalNoteStorage(
    private val context: Context,
    private val json: Json,
    private val tokenStorage: TokenStorage,
) {
    suspend fun getAllNotes(includePendingDeletes: Boolean = false): List<Note> {
        return readNotes()
            .filter { note -> includePendingDeletes || !note.pendingDelete }
            .sortedWith(
                compareByDescending<Note> { note -> note.date?.toString().orEmpty() }
                    .thenByDescending { note -> note.time?.toString().orEmpty() },
            )
    }

    suspend fun getNotes(query: String?): List<Note> {
        return getAllNotes()
            .filter { note -> query.isNullOrBlank() || note.matchesQuery(query) }
    }

    suspend fun getNoteById(id: String): Note? {
        return readNotes().firstOrNull { note -> note.id == id }
    }

    suspend fun replaceSyncedNotes(notes: List<Note>) {
        val unsyncedNotes = readNotes().filter { note -> !note.isSynced }
        val unsyncedIds = unsyncedNotes.map { note -> note.id }.toSet()
        val syncedNotes = notes
            .filterNot { note -> note.id in unsyncedIds }
            .map { note -> note.copy(isSynced = true, pendingDelete = false) }
        writeNotes(syncedNotes + unsyncedNotes)
    }

    suspend fun upsert(note: Note) {
        val notes = readNotes().filterNot { existing -> existing.id == note.id } + note
        writeNotes(notes)
    }

    suspend fun delete(id: String) {
        writeNotes(readNotes().filterNot { note -> note.id == id })
    }

    private suspend fun readNotes(): List<Note> {
        val notesKey = notesKey()
        return context.localNotesDataStore.data.map { preferences ->
            preferences[notesKey]?.let(::decodeNotes).orEmpty()
        }.first()
    }

    private suspend fun writeNotes(notes: List<Note>) {
        val notesKey = notesKey()
        context.localNotesDataStore.edit { preferences ->
            preferences[notesKey] = json.encodeToString(notes.map { note -> note.toEntity() })
        }
    }

    private suspend fun notesKey() = stringPreferencesKey("notes_${tokenStorage.getUserId() ?: "legacy"}")

    private fun decodeNotes(value: String): List<Note> {
        return runCatching { json.decodeFromString<List<LocalNoteEntity>>(value) }
            .getOrDefault(emptyList())
            .map { entity -> entity.toDomain() }
    }

    private fun Note.matchesQuery(query: String): Boolean {
        val normalizedQuery = query.trim()
        return location.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            target.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            text.orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            date?.toString().orEmpty().contains(normalizedQuery, ignoreCase = true) ||
            time?.toString().orEmpty().contains(normalizedQuery, ignoreCase = true)
    }

    private fun Note.toEntity(): LocalNoteEntity {
        return LocalNoteEntity(
            id = id,
            date = date?.toString(),
            time = time?.toString(),
            location = location,
            target = target,
            text = text,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            isSynced = isSynced,
            pendingDelete = pendingDelete,
        )
    }

    private fun LocalNoteEntity.toDomain(): Note {
        return Note(
            id = id,
            date = date?.let(LocalDate::parse),
            time = time?.let(LocalTime::parse),
            location = location,
            target = target,
            text = text,
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt),
            isSynced = isSynced,
            pendingDelete = pendingDelete,
        )
    }
}

@Serializable
private data class LocalNoteEntity(
    val id: String,
    val date: String? = null,
    val time: String? = null,
    val location: String? = null,
    val target: String? = null,
    val text: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean,
    val pendingDelete: Boolean = false,
)
