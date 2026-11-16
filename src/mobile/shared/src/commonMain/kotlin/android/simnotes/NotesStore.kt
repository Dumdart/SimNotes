package android.simnotes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
internal data class Notebook(
    val id: String,
    val name: String,
    val notes: List<Note> = emptyList(),
) {
    val activeNotes: List<Note> get() = notes.filterNot(Note::isDone)
    val doneNotes: List<Note> get() = notes.filter(Note::isDone)
}

@Serializable
internal data class Note(val id: String, val text: String, val isDone: Boolean = false)

internal interface NotesStorage {
    fun read(): String?
    fun write(contents: String)
}

internal const val NotesFileName = "notebooks.json"

internal class NotesStorageException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

internal class NotesStore(private val storage: NotesStorage) {
    var notebooks by mutableStateOf(load())
        private set

    fun createNotebook(name: String): Notebook {
        require(name.isNotBlank())
        val notebook = Notebook(Uuid.random().toString(), name.trim())
        save(notebooks + notebook)
        return notebook
    }

    fun addNote(notebookId: String, text: String) {
        require(text.isNotBlank())
        updateNotebook(notebookId) { it.notes + Note(Uuid.random().toString(), text) }
    }

    fun editNote(notebookId: String, noteId: String, text: String) {
        require(text.isNotBlank())
        updateNote(notebookId, noteId) { it.copy(text = text) }
    }

    fun setDone(notebookId: String, noteId: String, isDone: Boolean) {
        updateNote(notebookId, noteId) { it.copy(isDone = isDone) }
    }

    fun deleteNote(notebookId: String, noteId: String) {
        updateNotebook(notebookId) { notebook -> notebook.notes.filterNot { it.id == noteId } }
    }

    private fun updateNote(notebookId: String, noteId: String, edit: (Note) -> Note) {
        updateNotebook(notebookId) { notebook ->
            notebook.notes.map { if (it.id == noteId) edit(it) else it }
        }
    }

    private fun updateNotebook(notebookId: String, update: (Notebook) -> List<Note>) {
        save(notebooks.map { if (it.id == notebookId) it.copy(notes = update(it)) else it })
    }

    private fun load(): List<Notebook> {
        val contents = storage.read() ?: return emptyList()
        return try {
            Json.decodeFromString(contents)
        } catch (error: SerializationException) {
            throw NotesStorageException("Unable to read notebook data", error)
        }
    }

    private fun save(updated: List<Notebook>) {
        if (updated == notebooks) return
        storage.write(Json.encodeToString(updated))
        notebooks = updated
    }
}
