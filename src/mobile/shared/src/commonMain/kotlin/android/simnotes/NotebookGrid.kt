package android.simnotes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import simnotes.shared.generated.resources.Res
import simnotes.shared.generated.resources.ic_list
import simnotes.shared.generated.resources.ic_next
import simnotes.shared.generated.resources.ic_plus

@Composable
internal fun NotebookGrid(
    notebooks: List<Notebook>,
    openNotebook: (String) -> Unit,
    createNotebook: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeading("Notebooks")
        if (notebooks.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val iconShape = RoundedCornerShape(24.dp)
                    Box(
                        Modifier.size(72.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer, iconShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, iconShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        NoteIcon(Res.drawable.ic_list, Modifier.size(36.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No notebooks yet",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Create one to start taking notes.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                    NewNotebookButton(createNotebook)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(145.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(notebooks, key = Notebook::id) { notebook ->
                    NotebookCard(notebook) { openNotebook(notebook.id) }
                }
            }
            NewNotebookButton(
                createNotebook,
                Modifier.align(Alignment.End).padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun NotebookCard(notebook: Notebook, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 130.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    notebook.name,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                NoteIcon(Res.drawable.ic_next)
            }
            notebook.notes.firstOrNull { !it.isDone }?.let { preview ->
                Spacer(Modifier.height(8.dp))
                Text(
                    preview.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NewNotebookButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier.heightIn(min = 56.dp)) {
        NoteIcon(Res.drawable.ic_plus)
        Spacer(Modifier.size(8.dp))
        Text("New notebook")
    }
}
