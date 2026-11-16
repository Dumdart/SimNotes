package android.simnotes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class NotesStoreTest {
    @Test
    fun createsANotebookAndAddsAndEditsAMultilineNote() {
        val store = NotesStore(MemoryStorage())
        assertTrue(store.notebooks.isEmpty())
        val notebook = store.createNotebook("  Quick notes  ")
        val otherNotebook = store.createNotebook("Quick notes")
        assertNotEquals(notebook.id, otherNotebook.id)

        store.addNote(notebook.id, "Window light\n  before noon.")
        val note = store.notebooks.first().activeNotes.single()
        assertEquals("Window light\n  before noon.", note.text)

        store.editNote(notebook.id, note.id, "Window light\n  before eleven.")
        assertEquals("Quick notes", store.notebooks.first().name)
        assertEquals(note.copy(text = "Window light\n  before eleven."), store.notebooks.first().activeNotes.single())
        assertTrue(store.notebooks.last().notes.isEmpty())
    }

    @Test
    fun doneIsNotebookScopedAndRestoreReturnsTheNoteToItsOriginalPosition() {
        val store = NotesStore(MemoryStorage())
        val notebook = store.createNotebook("Ideas")
        val otherNotebook = store.createNotebook("Reading")
        store.addNote(notebook.id, "First")
        store.addNote(notebook.id, "Café")
        store.addNote(notebook.id, "Third")
        val note = store.notebooks.first().activeNotes[1]

        store.setDone(notebook.id, note.id, true)
        assertEquals(listOf("First", "Third"), store.notebooks.first().activeNotes.map(Note::text))
        assertEquals(listOf(note.copy(isDone = true)), store.notebooks.first().doneNotes)
        assertTrue(store.notebooks.last().doneNotes.isEmpty())

        store.setDone(otherNotebook.id, note.id, false)
        assertEquals(1, store.notebooks.first().doneNotes.size)
        store.setDone(notebook.id, note.id, false)
        assertEquals(listOf("First", "Café", "Third"), store.notebooks.first().activeNotes.map(Note::text))
        assertTrue(store.notebooks.first().doneNotes.isEmpty())
    }

    @Test
    fun reopeningRetainsEditedTextAndDoneStatus() {
        val storage = MemoryStorage()
        verifyReopening { storage }
    }

    @Test
    fun failedWriteKeepsThePreviouslySavedState() {
        val storage = MemoryStorage()
        val store = NotesStore(storage)
        val notebook = store.createNotebook("Ideas")
        store.addNote(notebook.id, "First")
        val saved = store.notebooks
        val noteId = saved.single().activeNotes.single().id
        storage.failWrites = true

        assertFailsWith<NotesStorageException> { store.setDone(notebook.id, noteId, true) }
        assertEquals(saved, store.notebooks)
        assertEquals(saved, NotesStore(storage).notebooks)
    }

    @Test
    fun corruptDataIsReportedWithoutOverwritingIt() {
        val storage = MemoryStorage().apply { write("{") }
        assertFailsWith<NotesStorageException> { NotesStore(storage) }
        assertEquals("{", storage.read())
    }

    @Test
    fun blankInputIsRejectedAndDeletingRemovesTheNote() {
        val store = NotesStore(MemoryStorage())
        assertFailsWith<IllegalArgumentException> { store.createNotebook("  ") }
        val notebook = store.createNotebook("Notes")
        assertFailsWith<IllegalArgumentException> { store.addNote(notebook.id, "\n  ") }
        store.addNote(notebook.id, "Rain")
        val noteId = store.notebooks.single().activeNotes.single().id
        assertFailsWith<IllegalArgumentException> { store.editNote(notebook.id, noteId, "") }
        store.deleteNote(notebook.id, noteId)
        assertTrue(store.notebooks.single().notes.isEmpty())
    }
}

internal fun verifyReopening(openStorage: () -> NotesStorage) {
    val first = NotesStore(openStorage())
    val notebook = first.createNotebook("Café")
    first.addNote(notebook.id, "First draft")
    val noteId = first.notebooks.single().activeNotes.single().id
    first.editNote(notebook.id, noteId, "Second draft\nWith a café sketch ☕")
    first.setDone(notebook.id, noteId, true)
    first.addNote(notebook.id, "Still active")

    val reopened = NotesStore(openStorage())
    assertEquals(first.notebooks, reopened.notebooks)
    assertEquals("Second draft\nWith a café sketch ☕", reopened.notebooks.single().doneNotes.single().text)
    val active = reopened.notebooks.single().activeNotes.single()
    assertEquals("Still active", active.text)

    reopened.setDone(notebook.id, noteId, false)
    val restored = NotesStore(openStorage()).notebooks.single()
    assertTrue(restored.doneNotes.isEmpty())
    assertEquals(listOf(noteId, active.id), restored.activeNotes.map(Note::id))
}

private class MemoryStorage : NotesStorage {
    private var contents: String? = null
    var failWrites = false

    override fun read(): String? = contents

    override fun write(contents: String) {
        if (failWrites) throw NotesStorageException("Storage unavailable")
        this.contents = contents
    }
}
