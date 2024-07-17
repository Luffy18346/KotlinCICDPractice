package com.example.kotlincicdpractice.feature_note.presentation.notes

import com.example.kotlincicdpractice.feature_note.domain.model.Note
import com.example.kotlincicdpractice.feature_note.domain.util.NoteOrder

sealed class NotesEvent {
    data class SortOrder(val noteOrder: NoteOrder) : NotesEvent()

    data class DeleteNote(val note: Note) : NotesEvent()

    data object RestoreNote : NotesEvent()

    data object ToggleOrderSection : NotesEvent()
}
