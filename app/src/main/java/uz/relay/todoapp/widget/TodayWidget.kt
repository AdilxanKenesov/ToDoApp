package uz.relay.todoapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.CheckboxDefaults
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import uz.relay.todoapp.MainActivity
import uz.relay.todoapp.R
import uz.relay.todoapp.domain.model.Priority
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.model.TodayData
import uz.relay.todoapp.utils.label

class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = entryPoint(context).getTodayUseCase()().first()
        provideContent {
            GlanceTheme { Content(data) }
        }
    }

    @Composable
    private fun Content(data: TodayData) {
        val tasks = data.overdue + data.today
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(26.dp)
                .background(ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF171933)))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = GlanceModifier.defaultWeight().clickable(actionStartActivity<MainActivity>())) {
                    Text(text = "Today", style = TextStyle(color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    Text(text = "${data.doneCount} of ${data.total} done", style = TextStyle(color = mutedColor, fontSize = 11.sp))
                }
                Box(
                    modifier = GlanceModifier
                        .size(34.dp)
                        .cornerRadius(17.dp)
                        .background(ColorProvider(day = Color(0xFFFF6B57), night = Color(0xFFFF8A78)))
                        .clickable(actionStartActivity<MainActivity>(actionParametersOf(MainActivity.QUICK_ADD to true))),
                    contentAlignment = Alignment.Center
                ) {
                    Image(provider = ImageProvider(R.drawable.ic_widget_add), contentDescription = "Add a task", modifier = GlanceModifier.size(18.dp))
                }
            }
            Spacer(modifier = GlanceModifier.height(6.dp))

            if (tasks.isEmpty()) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = if (data.doneCount > 0) "All done" else "Nothing for today", style = TextStyle(color = mutedColor, fontSize = 13.sp))
                }
            } else {
                LazyColumn {
                    items(tasks, itemId = { it.id }) { task -> TaskRow(task, data) }
                }
            }
        }
    }

    @Composable
    private fun TaskRow(task: Task, data: TodayData) {
        val late = task.isOverdue(data.date)
        val time = when {
            late -> task.dueDate?.label(data.date)
            else -> task.dueTime?.label()
        }
        CheckBox(
            checked = false,
            onCheckedChange = actionRunCallback<ToggleTaskAction>(actionParametersOf(ToggleTaskAction.TASK_ID to task.id)),
            text = if (time != null) "${task.title} · $time" else task.title,
            style = TextStyle(color = if (late) coralColor else textColor, fontSize = 13.sp),
            colors = CheckboxDefaults.colors(
                checkedColor = indigoColor,
                uncheckedColor = if (task.priority == Priority.HIGH) coralColor else mutedColor
            ),
            maxLines = 1,
            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 1.dp)
        )
    }

    private companion object {
        val textColor = ColorProvider(day = Color(0xFF14142B), night = Color(0xFFECECFA))
        val mutedColor = ColorProvider(day = Color(0xFF6B6C86), night = Color(0xFFA2A3C2))
        val coralColor = ColorProvider(day = Color(0xFFFF6B57), night = Color(0xFFFF8A78))
        val indigoColor = ColorProvider(day = Color(0xFF4338CA), night = Color(0xFF8B8DFF))
    }
}

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[TASK_ID] ?: return
        entryPoint(context).toggleTaskUseCase()(id, done = true).first()
    }

    companion object {
        val TASK_ID = ActionParameters.Key<Long>("task_id")
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}

private fun entryPoint(context: Context): WidgetEntryPoint =
    EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
