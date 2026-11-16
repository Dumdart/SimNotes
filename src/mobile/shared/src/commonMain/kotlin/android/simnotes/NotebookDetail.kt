package android.simnotes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import simnotes.shared.generated.resources.Res
import simnotes.shared.generated.resources.ic_done
import simnotes.shared.generated.resources.ic_edit
import simnotes.shared.generated.resources.ic_more
import simnotes.shared.generated.resources.ic_plus

@Composable
internal fun NotebookDetail(
    notebook: Notebook,
    store: NotesStore,
    showDone: Boolean,
    openDone: () -> Unit,
    save: (() -> Unit) -> Boolean,
) {
    var draft by rememberSaveable { mutableStateOf("") }
    var composing by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    val popping = remember { mutableStateMapOf<String, Animatable<Float, AnimationVector1D>>() }
    val notes = notebook.notes.filter { !it.isDone || it.id in popping }
    val activeScroll = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current

    fun complete(note: Note) {
        if (note.isDone) return
        val reduceMotion = systemReducesMotion()
        if (!save { store.setDone(notebook.id, note.id, true) } || reduceMotion) return

        // Commit Done before animating so leaving the screen or app cannot undo the check.
        val scale = Animatable(1f)
        popping[note.id] = scale
        scope.launch {
            try {
                scale.animateTo(1.05f, tween(85))
                scale.animateTo(0.95f, tween(95))
            } finally {
                popping.remove(note.id)
            }
        }
    }

    if (showDone) {
        DoneNotes(notebook.doneNotes) { note ->
            save { store.setDone(notebook.id, note.id, false) }
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            LazyColumn(state = activeScroll, modifier = Modifier.weight(1f, fill = false)) {
                item {
                    ScreenHeading(notebook.name) { NotebookMenu(openDone) }
                }
                items(notes, key = Note::id) { note ->
                    ActiveNoteRow(
                        note = note,
                        pop = popping[note.id],
                        onEdit = { editingId = note.id },
                        onDone = { complete(note) },
                    )
                }
            }
            NoteComposer(
                composing = composing,
                text = draft,
                onOpen = { composing = true },
                onChange = { draft = it },
                onSave = {
                    if (draft.isNotBlank() && save { store.addNote(notebook.id, draft) }) {
                        focus.clearFocus()
                        draft = ""
                        composing = false
                        activeScroll.requestScrollToItem(notes.size + 1)
                    }
                },
            )
        }

        notebook.activeNotes.firstOrNull { it.id == editingId }?.let { note ->
            key(note.id) {
                fun finishEdit(change: () -> Unit): Boolean {
                    val saved = save(change)
                    if (saved) editingId = null
                    return saved
                }

                EditNoteDialog(
                    note = note,
                    onDismiss = { editingId = null },
                    onSave = { text -> finishEdit { store.editNote(notebook.id, note.id, text) } },
                    onDelete = { finishEdit { store.deleteNote(notebook.id, note.id) } },
                )
            }
        }
    }
}

@Composable
private fun NotebookMenu(openDone: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Box(
                Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                NoteIcon(Res.drawable.ic_more, description = "Notebook menu")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            DropdownMenuItem(
                text = { Text("View Done", style = MaterialTheme.typography.labelLarge) },
                onClick = {
                    expanded = false
                    openDone()
                },
                leadingIcon = { NoteIcon(Res.drawable.ic_done) },
            )
        }
    }
}

@Composable
private fun ActiveNoteRow(
    note: Note,
    pop: Animatable<Float, AnimationVector1D>?,
    onEdit: () -> Unit,
    onDone: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val surface = if (pop == null) {
        Modifier.background(colors.surface)
    } else {
        val shape = RoundedCornerShape(14.dp)
        Modifier.background(colors.surfaceContainer, shape).border(1.dp, colors.outline, shape)
    }
    Column(
        Modifier.padding(horizontal = 24.dp)
            .zIndex(if (pop == null) 0f else 1f)
            .graphicsLayer {
                val scale = pop?.value ?: 1f
                scaleX = scale
                scaleY = scale
            }
            .then(surface),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 71.dp).padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                note.text,
                Modifier.weight(1f).padding(vertical = 10.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
            IconButton(onClick = onEdit, enabled = pop == null) {
                NoteIcon(Res.drawable.ic_edit, description = "Edit note")
            }
            Checkbox(
                checked = note.isDone,
                onCheckedChange = { onDone() },
                modifier = Modifier.semantics { contentDescription = "Mark note done" },
                enabled = pop == null,
                colors = CheckboxDefaults.colors(disabledCheckedColor = colors.primary),
            )
        }
        if (pop == null) HorizontalDivider()
    }
}

@Composable
private fun NoteComposer(
    composing: Boolean,
    text: String,
    onOpen: () -> Unit,
    onChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (composing) {
            val focus = remember { FocusRequester() }
            LaunchedEffect(Unit) { focus.requestFocus() }
            OutlinedTextField(
                value = text,
                onValueChange = onChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                label = { Text("New note") },
                minLines = 2,
                maxLines = 4,
                shape = shape,
                textStyle = MaterialTheme.typography.bodyLarge,
            )
            Button(
                onSave,
                Modifier.align(Alignment.End).heightIn(min = 48.dp),
                enabled = text.isNotBlank(),
            ) {
                Text("Save note")
            }
        } else {
            OutlinedButton(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).background(colors.surfaceContainer, shape),
                shape = shape,
                border = BorderStroke(1.dp, colors.outline),
            ) {
                NoteIcon(Res.drawable.ic_plus)
                Spacer(Modifier.size(8.dp))
                Text("Write a note", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun DoneNotes(notes: List<Note>, restore: (Note) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item { ScreenHeading("Done") }
        if (notes.isEmpty()) {
            item {
                Text(
                    "No notes in Done yet.",
                    Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(notes, key = Note::id) { note ->
            Column(Modifier.padding(horizontal = 24.dp)) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 71.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(note.text, Modifier.weight(1f).padding(vertical = 10.dp))
                    OutlinedButton(onClick = { restore(note) }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Restore")
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
