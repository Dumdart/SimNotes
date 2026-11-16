package android.simnotes

import androidx.compose.runtime.Composable

@Composable
internal expect fun rememberNotesStore(): Result<NotesStore>

internal expect fun systemReducesMotion(): Boolean

internal fun openNotesStore(storage: () -> NotesStorage): Result<NotesStore> = try {
    Result.success(NotesStore(storage()))
} catch (error: NotesStorageException) {
    Result.failure(error)
}
