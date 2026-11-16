package android.simnotes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
internal actual fun rememberNotesStore(): Result<NotesStore> =
    remember { openNotesStore { FileNotesStorage(notesFile()) } }

internal actual fun systemReducesMotion(): Boolean = UIAccessibilityIsReduceMotionEnabled()
