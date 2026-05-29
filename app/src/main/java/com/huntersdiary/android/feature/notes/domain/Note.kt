@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class Note(
    val id: String,
    val date: LocalDate?,
    val time: LocalTime?,
    val location: String?,
    val target: String?,
    val text: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isSynced: Boolean = true,
    val pendingDelete: Boolean = false,
)
