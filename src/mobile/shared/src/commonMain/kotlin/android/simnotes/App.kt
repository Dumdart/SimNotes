package android.simnotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import simnotes.shared.generated.resources.Res
import simnotes.shared.generated.resources.ic_back

@Composable
fun App() {
    val store = rememberNotesStore().getOrNull()
    SimNotesTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (store == null) {
                Column(Modifier.safeDrawingPadding()) {
                    AppBar()
                    Text("Unable to open your notebooks.", Modifier.padding(24.dp))
                }
            } else {
                NotebooksApp(store)
            }
        }
    }
}

@Composable
private fun NotebooksApp(store: NotesStore) {
    var notebookId by rememberSaveable { mutableStateOf<String?>(null) }
    var showDone by rememberSaveable { mutableStateOf(false) }
    var creating by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val notebook = store.notebooks.firstOrNull { it.id == notebookId }

    fun save(change: () -> Unit): Boolean = try {
        change()
        true
    } catch (_: NotesStorageException) {
        scope.launch { snackbar.showSnackbar("Couldn't save changes on this device.") }
        false
    }

    fun goBack() {
        focus.clearFocus()
        if (showDone) showDone = false else notebookId = null
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = notebook != null,
        onBackCompleted = ::goBack,
    )

    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(Modifier.fillMaxSize()) {
            AppBar(
                backLabel = when {
                    notebook == null -> null
                    showDone -> notebook.name
                    else -> "Notebooks"
                },
                onBack = ::goBack,
            )
            if (notebook == null) {
                NotebookGrid(
                    notebooks = store.notebooks,
                    openNotebook = { notebookId = it },
                    createNotebook = { creating = true },
                )
            } else {
                key(notebook.id) {
                    NotebookDetail(
                        notebook = notebook,
                        store = store,
                        showDone = showDone,
                        openDone = {
                            focus.clearFocus()
                            showDone = true
                        },
                        save = ::save,
                    )
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (creating) {
        NotebookNameDialog(
            onDismiss = { creating = false },
            onCreate = { name ->
                val saved = save { notebookId = store.createNotebook(name).id }
                if (saved) creating = false
                saved
            },
        )
    }
}

@Composable
private fun AppBar(backLabel: String? = null, onBack: () -> Unit = {}) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (backLabel == null) {
                Text("SimNotes", Modifier.padding(start = 8.dp), style = MaterialTheme.typography.titleLarge)
            } else {
                TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                    NoteIcon(Res.drawable.ic_back, description = "Back")
                    Spacer(Modifier.size(8.dp))
                    Text(
                        backLabel,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
internal fun ScreenHeading(title: String, actions: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge)
        actions()
    }
}
