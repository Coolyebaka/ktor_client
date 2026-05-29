@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.data

import com.huntersdiary.android.feature.notes.domain.Note
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

fun NoteResponse.toDomain(): Note {
    return Note(
        id = id,
        date = date?.let(LocalDate::parse),
        time = time?.let(LocalTime::parse),
        location = location,
        target = target,
        text = text,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
        isSynced = true,
        pendingDelete = false,
    )
}

fun toCreateNoteRequest(
    date: LocalDate?,
    time: LocalTime?,
    location: String?,
    target: String?,
    text: String?,
    createdAt: Instant,
    updatedAt: Instant,
): CreateNoteRequest {
    return CreateNoteRequest(
        date = date?.toString(),
        time = time?.toApiString(),
        location = location,
        target = target,
        text = text,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

fun toUpdateNoteRequest(
    date: LocalDate?,
    time: LocalTime?,
    location: String?,
    target: String?,
    text: String?,
    createdAt: Instant,
    updatedAt: Instant,
): UpdateNoteRequest {
    return UpdateNoteRequest(
        date = date?.toString(),
        time = time?.toApiString(),
        location = location,
        target = target,
        text = text,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
}

private fun LocalTime.toApiString(): String {
    return toString().let { value ->
        if (value.count { char -> char == ':' } == 1) "$value:00" else value
    }
}
