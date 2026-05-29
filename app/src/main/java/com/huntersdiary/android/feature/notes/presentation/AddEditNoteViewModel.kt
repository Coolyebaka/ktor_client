@file:Suppress("DEPRECATION")

package com.huntersdiary.android.feature.notes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huntersdiary.android.feature.notes.domain.CreateNoteUseCase
import com.huntersdiary.android.feature.notes.domain.GetNoteByIdUseCase
import com.huntersdiary.android.feature.notes.domain.UpdateNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditNoteViewModel(
    private val noteId: String?,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AddEditNoteUiState(
            date = currentDateText(),
            time = currentTimeText(),
            isEditMode = noteId != null,
        ),
    )
    val uiState: StateFlow<AddEditNoteUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    fun onDateChange(value: String) {
        _uiState.update { state -> state.copy(date = value) }
    }

    fun onTimeChange(value: String) {
        _uiState.update { state -> state.copy(time = value) }
    }

    fun onLocationChange(value: String) {
        _uiState.update { state -> state.copy(location = value) }
    }

    fun onTargetChange(value: String) {
        _uiState.update { state -> state.copy(target = value) }
    }

    fun onTextChange(value: String) {
        _uiState.update { state -> state.copy(text = value) }
    }

    fun save() {
        val current = uiState.value
        val noteDateTimeResult = parseDateAndTime(current.date, current.time)
        if (noteDateTimeResult.isFailure) return
        val noteDateTime = noteDateTimeResult.getOrThrow()

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isSaving = true, errorMessage = null)
            }

            val result = if (noteId == null) {
                createNoteUseCase(
                    date = noteDateTime.date,
                    time = noteDateTime.time,
                    location = current.location,
                    target = current.target,
                    text = current.text,
                )
            } else {
                updateNoteUseCase(
                    id = noteId,
                    date = noteDateTime.date,
                    time = noteDateTime.time,
                    location = current.location,
                    target = current.target,
                    text = current.text,
                )
            }

            _uiState.update { state ->
                result.fold(
                    onSuccess = {
                        state.copy(
                            isSaving = false,
                            errorMessage = null,
                            saveCompleted = true,
                            savedNoteId = it.id,
                        )
                    },
                    onFailure = { error ->
                        state.copy(isSaving = false, errorMessage = error.message)
                    },
                )
            }
        }
    }

    fun retryLoad() {
        noteId?.let(::loadNote)
    }

    fun consumeSaveCompleted() {
        _uiState.update { state -> state.copy(saveCompleted = false, savedNoteId = null) }
    }

    private fun loadNote(id: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null)
            }

            val result = getNoteByIdUseCase(id)

            _uiState.update { state ->
                result.fold(
                    onSuccess = { note ->
                        state.copy(
                            date = note.date?.toString().orEmpty(),
                            time = note.time?.toString().orEmpty(),
                            location = note.location.orEmpty(),
                            target = note.target.orEmpty(),
                            text = note.text.orEmpty(),
                            isLoading = false,
                            errorMessage = null,
                        )
                    },
                    onFailure = { error ->
                        state.copy(isLoading = false, errorMessage = error.message)
                    },
                )
            }
        }
    }

    private fun parseDateAndTime(date: String, time: String): Result<NoteDateTime> {
        return try {
            val normalizedDate = date.trim()
            val normalizedTime = time.trim()
            val parsedDate = if (normalizedDate.isBlank()) {
                null
            } else {
                require(DATE_REGEX.matches(normalizedDate))
                LocalDate.parse(normalizedDate)
            }
            val parsedTime = if (normalizedTime.isBlank()) {
                null
            } else {
                require(TIME_REGEX.matches(normalizedTime))
                LocalTime.parse(normalizedTime)
            }
            Result.success(NoteDateTime(date = parsedDate, time = parsedTime))
        } catch (exception: IllegalArgumentException) {
            _uiState.update { state ->
                state.copy(
                    errorMessage = "Введите дату в формате ГГГГ-ММ-ДД и время в формате ЧЧ:ММ",
                )
            }
            Result.failure(exception)
        }
    }

    private companion object {
        val DATE_REGEX = Regex("\\d{4}-\\d{2}-\\d{2}")
        val TIME_REGEX = Regex("\\d{2}:\\d{2}")

        fun currentDateText(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        }

        fun currentTimeText(): String {
            return SimpleDateFormat("HH:mm", Locale.US).format(Date())
        }
    }
}

private data class NoteDateTime(
    val date: LocalDate?,
    val time: LocalTime?,
)
