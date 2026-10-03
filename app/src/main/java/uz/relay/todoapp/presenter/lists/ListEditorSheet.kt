package uz.relay.todoapp.presenter.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import uz.relay.todoapp.domain.model.ListIcon
import uz.relay.todoapp.domain.model.TaskList
import uz.relay.todoapp.ui.components.ListBadge
import uz.relay.todoapp.ui.components.vector
import uz.relay.todoapp.ui.theme.TickTheme

/** Name, color and icon of a list; used to create one and to edit it. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListEditorSheet(
    initial: TaskList?,
    onDismiss: () -> Unit,
    onSave: (TaskList) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var color by rememberSaveable { mutableIntStateOf(initial?.color ?: 0) }
    var icon by rememberSaveable { mutableStateOf(initial?.icon ?: ListIcon.LIST) }
    val colors = TickTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ListBadge(icon = icon, color = colors.listColor(color), size = 48.dp)
                Text(text = if (initial == null) "New list" else "Edit list", style = MaterialTheme.typography.titleLarge)
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(40) },
                placeholder = { Text("List name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colors.lists.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.listColor(index))
                            .clickable { color = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (index == color) Icon(Icons.Rounded.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ListIcon.entries.forEach { option ->
                    val selected = option == icon
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) colors.listColor(color).copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHighest)
                            .then(if (selected) Modifier.border(2.dp, colors.listColor(color), RoundedCornerShape(14.dp)) else Modifier)
                            .clickable { icon = option },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(option.vector(), contentDescription = option.name, tint = if (selected) colors.listColor(color) else colors.muted)
                    }
                }
            }

            Button(
                onClick = { onSave(TaskList(id = initial?.id ?: 0, name = name, color = color, icon = icon)) },
                enabled = name.isNotBlank(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(text = if (initial == null) "Create list" else "Save", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
