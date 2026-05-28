@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.data

import com.huntersdiary.android.feature.notes.domain.Note
import kotlinx.datetime.Instant

fun NoteResponse.toDomain(): Note {
    return Note(
        id = id,
        dateTime = Instant.parse(dateTime),
        location = location,
        target = target,
        text = text,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
    )
}

fun toCreateNoteRequest(
    dateTime: Instant,
    location: String,
    target: String,
    text: String,
): CreateNoteRequest {
    return CreateNoteRequest(
        dateTime = dateTime.toString(),
        location = location,
        target = target,
        text = text,
    )
}

fun toUpdateNoteRequest(
    dateTime: Instant,
    location: String,
    target: String,
    text: String,
): UpdateNoteRequest {
    return UpdateNoteRequest(
        dateTime = dateTime.toString(),
        location = location,
        target = target,
        text = text,
    )
}
