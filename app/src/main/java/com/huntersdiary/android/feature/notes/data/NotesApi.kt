package com.huntersdiary.android.feature.notes.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class NotesApi(
    private val client: HttpClient,
) {
    suspend fun getNotes(query: String?): List<NoteResponse> {
        return client.get("/notes") {
            query?.let { parameter("query", it) }
        }.body()
    }

    suspend fun getNoteById(id: String): NoteResponse {
        return client.get("/notes/$id").body()
    }

    suspend fun createNote(request: CreateNoteRequest): NoteResponse {
        return client.post("/notes") {
            setBody(request)
        }.body()
    }

    suspend fun updateNote(id: String, request: UpdateNoteRequest): NoteResponse {
        return client.put("/notes/$id") {
            setBody(request)
        }.body()
    }

    suspend fun deleteNote(id: String) {
        client.delete("/notes/$id")
    }
}
