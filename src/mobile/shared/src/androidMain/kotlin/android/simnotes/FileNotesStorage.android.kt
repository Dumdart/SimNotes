package android.simnotes

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

internal class FileNotesStorage(private val file: File) : NotesStorage {
    override fun read(): String? = try {
        Files.newBufferedReader(file.toPath()).use { it.readText() }
    } catch (_: NoSuchFileException) {
        null
    } catch (error: IOException) {
        throw NotesStorageException("Unable to read notebooks", error)
    }

    override fun write(contents: String) {
        try {
            val staged = File.createTempFile("notebooks-", ".tmp", file.parentFile)
            try {
                FileOutputStream(staged).use {
                    it.write(contents.encodeToByteArray())
                    it.fd.sync()
                }
                Files.move(staged.toPath(), file.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
            } finally {
                staged.delete()
            }
        } catch (error: IOException) {
            throw NotesStorageException("Unable to save notebooks", error)
        }
    }
}
