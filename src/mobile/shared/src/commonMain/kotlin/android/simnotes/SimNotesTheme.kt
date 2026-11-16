package android.simnotes

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import simnotes.shared.generated.resources.Res
import simnotes.shared.generated.resources.comic_neue_bold
import simnotes.shared.generated.resources.comic_neue_regular

private val Ink = Color(0xFF1A1A1A)
private val Muted = Color(0xFF555555)
private val Paper = Color(0xFFF4F4F4)

private val NoteColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = Paper,
    onPrimaryContainer = Ink,
    background = Color.White,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    onSurfaceVariant = Muted,
    surfaceContainerLow = Paper,
    surfaceContainer = Paper,
    surfaceContainerHigh = Paper,
    outline = Color(0xFF767676),
    outlineVariant = Color(0xFFD7D7D7),
    inverseSurface = Ink,
    inverseOnSurface = Color.White,
)

@Composable
internal fun SimNotesTheme(content: @Composable () -> Unit) {
    val family = FontFamily(
        Font(Res.font.comic_neue_regular, FontWeight.Normal),
        Font(Res.font.comic_neue_bold, FontWeight.Bold),
    )
    fun type(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) =
        TextStyle(fontFamily = family, fontSize = size.sp, lineHeight = line.sp, fontWeight = weight)

    MaterialTheme(
        colorScheme = NoteColors,
        typography = Typography(
            headlineLarge = type(34, 40, FontWeight.Bold),
            headlineSmall = type(25, 30, FontWeight.Bold),
            titleLarge = type(22, 28, FontWeight.Bold),
            titleMedium = type(18, 23, FontWeight.Bold),
            bodyLarge = type(17, 23),
            bodyMedium = type(16, 21),
            bodySmall = type(14, 19),
            labelLarge = type(15, 20, FontWeight.Bold),
            labelSmall = type(12, 16),
        ),
        content = content,
    )
}

@Composable
internal fun NoteIcon(resource: DrawableResource, modifier: Modifier = Modifier, description: String? = null) {
    Icon(painterResource(resource), description, modifier.size(24.dp))
}
