package uz.relay.todoapp.presenter.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.orbitmvi.orbit.compose.collectAsState
import uz.relay.todoapp.domain.model.DayCount
import uz.relay.todoapp.domain.model.Stats
import uz.relay.todoapp.domain.model.TaskList
import uz.relay.todoapp.presenter.stats.StatsContract.Intent
import uz.relay.todoapp.ui.components.EmptyState
import uz.relay.todoapp.ui.components.ListBadge
import uz.relay.todoapp.ui.components.ProgressRing
import uz.relay.todoapp.ui.components.SectionHeader
import uz.relay.todoapp.ui.theme.TickTheme
import uz.relay.todoapp.utils.shortDay
import java.time.LocalDate
import kotlin.math.roundToInt

object StatsScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Rounded.Insights)
            return remember { TabOptions(index = 3u, title = "Stats", icon = icon) }
        }

    @Composable
    override fun Content() {
        val viewModel: StatsContract.ViewModel = getViewModel<StatsViewModel>()
        val state = viewModel.collectAsState().value
        StatsScreenContent(state = state, onEventDispatcher = viewModel::onEventDispatcher)
    }
}

@Composable
internal fun StatsScreenContent(
    state: StatsContract.UiStatsState,
    onEventDispatcher: (Intent) -> Unit
) {
    val stats = state.stats
    val colors = TickTheme.colors

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "YOUR PROGRESS", style = MaterialTheme.typography.labelSmall, color = colors.muted)
                    Text(text = "Stats", style = MaterialTheme.typography.headlineMedium)
                }
                FilledIconButton(
                    onClick = { onEventDispatcher(Intent.OpenSettings) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                }
            }

            WeekCard(stats)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(Icons.Rounded.CheckCircle, stats.doneToday, "Done today", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                StatTile(Icons.Rounded.RadioButtonUnchecked, stats.openToday, "Left today", colors.amber, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(Icons.Rounded.WarningAmber, stats.overdue, "Overdue", colors.coral, Modifier.weight(1f))
                StatTile(
                    Icons.Rounded.LocalFireDepartment,
                    stats.streakDays,
                    if (stats.streakDays == 1) "Day streak" else "Days streak",
                    colors.green,
                    Modifier.weight(1f)
                )
            }

            if (!state.loading && stats.totalDone == 0) {
                EmptyState(
                    title = "No stats yet",
                    subtitle = "Check off a task to start",
                    icon = Icons.Rounded.Insights,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                )
            } else {
                SectionHeader(title = "Last 7 days", count = stats.last7Days.sumOf { it.count })
                WeekChart(days = stats.last7Days)

                if (stats.lists.isNotEmpty()) {
                    SectionHeader(title = "Lists", count = null)
                    Card {
                        stats.lists.forEachIndexed { index, list ->
                            ListProgressRow(list)
                            if (index != stats.lists.lastIndex) Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
                Text(
                    text = "${stats.totalDone} tasks done in total",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.faint,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                )
            }
        }

        if (state.loading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun WeekCard(stats: Stats) {
    val colors = TickTheme.colors
    val percent = (stats.weekProgress * 100).roundToInt()
    val glow = colors.coral.copy(alpha = 0.32f)
    val subtitle = when {
        stats.plannedThisWeek == 0 -> "Nothing planned this week"
        stats.doneThisWeek == stats.plannedThisWeek -> "Everything done, well played"
        else -> "${stats.doneThisWeek} of ${stats.plannedThisWeek} tasks done"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Brush.linearGradient(listOf(colors.heroStart, colors.heroMid, colors.heroEnd)))
            .drawBehind {
                drawCircle(glow, radius = 75.dp.toPx(), center = Offset(size.width - 35.dp.toPx(), size.height + 15.dp.toPx()))
            }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(progress = stats.weekProgress, size = 76.dp, stroke = 9.dp) {
                Text(text = "$percent%", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = "This week", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun StatTile(icon: ImageVector, value: Int, label: String, accent: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = value.toString(), style = MaterialTheme.typography.headlineSmall)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TickTheme.colors.muted, maxLines = 1)
        }
    }
}

@Composable
private fun WeekChart(days: List<DayCount>) {
    val max = (days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val today = days.lastOrNull()?.date
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEach { day ->
                Bar(
                    day = day,
                    fraction = day.count / max.toFloat(),
                    highlight = day.date == today,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun Bar(day: DayCount, fraction: Float, highlight: Boolean, modifier: Modifier = Modifier) {
    val colors = TickTheme.colors
    val animated by animateFloatAsState(targetValue = fraction, animationSpec = tween(600), label = "bar")
    val barColor = when {
        highlight -> colors.coral
        day.count > 0 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

    // The bar and the empty space above it split the height, so the count sits on top of the bar.
    val bar = animated.coerceIn(0.04f, 1f)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (bar < 1f) Spacer(modifier = Modifier.weight(1f - bar))
        Text(
            text = day.count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (day.count > 0) MaterialTheme.colorScheme.onSurface else colors.faint
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(bar)
                .clip(RoundedCornerShape(8.dp))
                .background(barColor)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = day.date.shortDay(),
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) colors.coral else colors.muted
        )
    }
}

@Composable
private fun ListProgressRow(list: TaskList) {
    val color = TickTheme.colors.listColor(list.color)
    val animated by animateFloatAsState(targetValue = list.progress, animationSpec = tween(600), label = "listProgress")
    Row(verticalAlignment = Alignment.CenterVertically) {
        ListBadge(icon = list.icon, color = color, size = 34.dp)
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(text = "${list.doneCount}/${list.taskCount}", style = MaterialTheme.typography.labelSmall, color = TickTheme.colors.muted)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animated)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) { content() }
}

@Preview
@Composable
private fun PreviewStatsScreenContent() {
    val today = LocalDate.now()
    TickTheme {
        StatsScreenContent(
            state = StatsContract.UiStatsState(
                loading = false,
                stats = Stats(
                    doneToday = 3,
                    openToday = 2,
                    overdue = 1,
                    doneThisWeek = 12,
                    plannedThisWeek = 17,
                    streakDays = 5,
                    totalDone = 48,
                    last7Days = (6L downTo 0L).map { DayCount(today.minusDays(it), listOf(2, 4, 1, 0, 5, 3, 3)[6 - it.toInt()]) },
                    lists = listOf(TaskList(id = 1, name = "Work", taskCount = 10, doneCount = 7), TaskList(id = 2, name = "Home", color = 1, taskCount = 6, doneCount = 2))
                )
            ),
            onEventDispatcher = {}
        )
    }
}
