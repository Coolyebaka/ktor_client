package com.huntersdiary.android.feature.notes.data

import kotlinx.serialization.Serializable

@Serializable
data class CreateNoteRequest(
    val date: String? = null,
    val time: String? = null,
    val location: String? = null,
    val target: String? = null,
    val text: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class UpdateNoteRequest(
    val date: String? = null,
    val time: String? = null,
    val location: String? = null,
    val target: String? = null,
    val text: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class NoteResponse(
    val id: String,
    val date: String? = null,
    val time: String? = null,
    val location: String? = null,
    val target: String? = null,
    val text: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class NoteApiError(
    val code: String,
    val message: String,
)
