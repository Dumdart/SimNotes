package android.simnotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import simnotes.shared.generated.resources.Res
import simnotes.shared.generated.resources.ic_close

@Composable
internal fun NotebookNameDialog(onDismiss: () -> Unit, onCreate: (String) -> Boolean) {
    TextEditorDialog(
        title = "New notebook",
        fieldLabel = "Notebook name",
        initialText = "",
        submitLabel = "Create",
        singleLine = true,
        onDismiss = onDismiss,
        onSubmit = onCreate,
    )
}

@Composable
internal fun EditNoteDialog(note: Note, onDismiss: () -> Unit, onSave: (String) -> Boolean, onDelete: () -> Boolean) {
    TextEditorDialog(
        title = "Edit note",
        fieldLabel = "Note",
        initialText = note.text,
        submitLabel = "Save",
        singleLine = false,
        onDismiss = onDismiss,
        onSubmit = onSave,
        onDelete = onDelete,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TextEditorDialog(
    title: String,
    fieldLabel: String,
    initialText: String,
    submitLabel: String,
    singleLine: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Boolean,
    onDelete: (() -> Boolean)? = null,
) {
    val sheet = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheet,
        containerColor = colors.surfaceContainer,
        contentColor = colors.onSurface,
        sheetMaxWidth = 480.dp,
    ) {
        var text by rememberSaveable { mutableStateOf(initialText) }
        var failed by remember { mutableStateOf(false) }
        val focus = remember { FocusRequester() }

        fun submit() {
            if (text.isNotBlank()) failed = !onSubmit(text)
        }

        LaunchedEffect(sheet.currentValue) {
            if (sheet.currentValue == SheetValue.Expanded) focus.requestFocus()
        }
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDismiss) {
                    NoteIcon(Res.drawable.ic_close, description = "Close")
                }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                label = { Text(fieldLabel) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                ),
                singleLine = singleLine,
                minLines = if (singleLine) 1 else 3,
                maxLines = if (singleLine) 1 else 5,
                keyboardOptions = KeyboardOptions(imeAction = if (singleLine) ImeAction.Done else ImeAction.Default),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            if (failed) {
                Text("Couldn't save changes on this device.", color = colors.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onDelete != null) {
                    OutlinedButton(
                        onClick = { failed = !onDelete() },
                        modifier = Modifier.heightIn(min = 48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.surface),
                    ) {
                        Text("Delete")
                    }
                    Spacer(Modifier.weight(1f))
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.onSurface),
                ) {
                    Text("Cancel")
                }
                Button(onClick = ::submit, modifier = Modifier.heightIn(min = 48.dp), enabled = text.isNotBlank()) {
                    Text(submitLabel)
                }
            }
        }
    }
}
