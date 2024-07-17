package com.example.kotlincicdpractice.feature_note.domain.use_case

import com.example.kotlincicdpractice.feature_note.domain.model.Note
import com.example.kotlincicdpractice.feature_note.domain.repository.NoteRepository
import com.example.kotlincicdpractice.feature_note.domain.util.NoteOrder
import com.example.kotlincicdpractice.feature_note.domain.util.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetNotes(
    private val repository: NoteRepository,
) {
    operator fun invoke(noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending)): Flow<List<Note>> {
        return repository.getNotes().map { notes ->
            notes.forEach { println("GetNotes = ${it.content} ${it.title} ${it.color} ${it.id} ${it.timestamp}") }
            val list =
                when (noteOrder.orderType) {
                    is OrderType.Ascending -> {
                        when (noteOrder) {
                            is NoteOrder.Title -> notes.sortedBy { it.title.lowercase() }
                            is NoteOrder.Date -> notes.sortedBy { it.timestamp }
                            is NoteOrder.Color -> notes.sortedBy { it.color }
                        }
                    }
                    is OrderType.Descending -> {
                        when (noteOrder) {
                            is NoteOrder.Title -> notes.sortedByDescending { it.title.lowercase() }
                            is NoteOrder.Date -> notes.sortedByDescending { it.timestamp }
                            is NoteOrder.Color -> notes.sortedByDescending { it.color }
                        }
                    }
                }
            notes.forEach { println("GetNotes After = ${it.content} ${it.title} ${it.color} ${it.id} ${it.timestamp}") }
            list
        }
    }
}