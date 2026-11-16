package android.simnotes

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToURL

@OptIn(ExperimentalForeignApi::class)
internal class FileNotesStorage(private val file: NSURL) : NotesStorage {
    override fun read(): String? {
        val path = file.path ?: throw NotesStorageException("Invalid notebook file location")
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
        return NSString.create(contentsOfURL = file, encoding = NSUTF8StringEncoding, error = null)
            ?.toString() ?: throw NotesStorageException("Unable to read notebooks")
    }

    override fun write(contents: String) {
        val written = NSString.create(string = contents)
            .writeToURL(file, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        if (!written) throw NotesStorageException("Unable to save notebooks")
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun notesFile(): NSURL {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return directory?.URLByAppendingPathComponent(NotesFileName)
        ?: throw NotesStorageException("Unable to open the notebook directory")
}
