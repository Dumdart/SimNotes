package android.simnotes

import android.animation.ValueAnimator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberNotesStore(): Result<NotesStore> {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        openNotesStore { FileNotesStorage(context.filesDir.resolve(NotesFileName)) }
    }
}

internal actual fun systemReducesMotion(): Boolean = !ValueAnimator.areAnimatorsEnabled()
