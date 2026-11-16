package android.simnotes

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import kotlin.test.Test

@OptIn(ExperimentalForeignApi::class)
class FileNotesStorageIOSTest {
    @Test
    fun reopensTheOnDiskNotebookFile() {
        val file = NSURL.fileURLWithPath(NSTemporaryDirectory() + "simnotes-" + NSUUID().UUIDString + ".json")
        try {
            verifyReopening { FileNotesStorage(file) }
        } finally {
            NSFileManager.defaultManager.removeItemAtURL(file, error = null)
        }
    }
}
