package android.simnotes

import java.nio.file.Files
import kotlin.test.Test

class FileNotesStorageAndroidTest {
    @Test
    fun reopensTheOnDiskNotebookFile() {
        val directory = Files.createTempDirectory("simnotes-test").toFile()
        try {
            verifyReopening { FileNotesStorage(directory.resolve(NotesFileName)) }
        } finally {
            directory.resolve(NotesFileName).delete()
            directory.delete()
        }
    }
}
