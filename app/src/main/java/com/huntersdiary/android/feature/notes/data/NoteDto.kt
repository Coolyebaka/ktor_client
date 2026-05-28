package com.huntersdiary.android.feature.notes.data

import kotlinx.serialization.Serializable

@Serializable
data class CreateNoteRequest(
    val dateTime: String,
    val location: String,
    val target: String,
    val text: String,
)

@Serializable
data class UpdateNoteRequest(
    val dateTime: String,
    val location: String,
    val target: String,
    val text: String,
)

@Serializable
data class NoteResponse(
    val id: String,
    val dateTime: String,
    val location: String,
    val target: String,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class NoteApiError(
    val code: String,
    val message: String,
)
