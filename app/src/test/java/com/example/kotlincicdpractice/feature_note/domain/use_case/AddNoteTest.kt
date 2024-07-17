package com.example.kotlincicdpractice.feature_note.domain.use_case

import com.example.kotlincicdpractice.feature_note.data.repository.FakeNoteRepository
import com.example.kotlincicdpractice.feature_note.domain.model.InvalidNoteException
import com.example.kotlincicdpractice.feature_note.domain.model.Note
import com.example.kotlincicdpractice.feature_note.domain.util.NoteOrder
import com.example.kotlincicdpractice.feature_note.domain.util.OrderType
import com.google.common.truth.Truth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AddNoteTest {
    private lateinit var getNotes: GetNotes
    private lateinit var addNote: AddNote
    private lateinit var fakeRepository: FakeNoteRepository

    @Before
    fun setUp() {
        fakeRepository = FakeNoteRepository()
        getNotes = GetNotes(fakeRepository)
        addNote = AddNote(fakeRepository)
    }

    @Test
    fun `Add note with empty title, get exception`() =
        runBlocking {
            val note =
                Note(
                    title = "",
                    content = "Content",
                    timestamp = 1.toLong(),
                    color = 1,
                )

            try {
                addNote(note)
                Assert.fail("Expected exception to be thrown")
            } catch (e: InvalidNoteException) {
                Truth.assertThat(e).isInstanceOf(InvalidNoteException::class.java)
                Truth.assertThat(e.message == "The title of the note can't be empty.").isTrue()
            }
        }

    @Test
    fun `Add note with empty content, get exception`() =
        runBlocking {
            val note =
                Note(
                    title = "Title",
                    content = "",
                    timestamp = 1.toLong(),
                    color = 1,
                )

            try {
                addNote(note)
                Assert.fail("Expected exception to be thrown")
            } catch (e: InvalidNoteException) {
                Truth.assertThat(e).isInstanceOf(InvalidNoteException::class.java)
                Truth.assertThat(e.message == "The content of the note can't be empty.").isTrue()
            }
        }

    @Test
    fun `Add note with proper data, addition success`() =
        runBlocking {
            val note =
                Note(
                    title = "Title",
                    content = "Content",
                    timestamp = 100.toLong(),
                    color = 100,
                )

            addNote(note)
            val notes = getNotes(NoteOrder.Date(OrderType.Descending)).first()
            Truth.assertThat(notes[0].title).matches("Title")
        }
}