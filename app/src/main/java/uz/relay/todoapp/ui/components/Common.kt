package uz.relay.todoapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uz.relay.todoapp.domain.model.ListIcon
import uz.relay.todoapp.ui.theme.TickTheme

/** One snackbar host for the whole app, above the bottom bar. */
val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

fun ListIcon.vector(): ImageVector = when (this) {
    ListIcon.LIST -> Icons.AutoMirrored.Rounded.List
    ListIcon.WORK -> Icons.Rounded.Work
    ListIcon.HOME -> Icons.Rounded.Home
    ListIcon.CART -> Icons.Rounded.ShoppingCart
    ListIcon.TRAVEL -> Icons.Rounded.Flight
    ListIcon.STUDY -> Icons.Rounded.MenuBook
    ListIcon.HEALTH -> Icons.Rounded.Favorite
    ListIcon.STAR -> Icons.Rounded.Star
    ListIcon.MONEY -> Icons.Rounded.Payments
    ListIcon.GIFT -> Icons.Rounded.Redeem
}

@Composable
fun ListBadge(icon: ListIcon, color: Color, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.34f))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon.vector(), contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.5f))
    }
}

/** Pill input that adds a task on Enter and keeps the keyboard up for the next one. */
@Composable
fun QuickAddBar(
    placeholder: String,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }
    fun submit() {
        if (text.isNotBlank()) {
            onSubmit(text)
            text = ""
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(18.dp, CircleShape, ambientColor = Color(0x664338CA), spotColor = Color(0x664338CA))
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(start = 20.dp, end = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (text.isEmpty()) {
                Text(text = placeholder, style = MaterialTheme.typography.bodyMedium, color = TickTheme.colors.faint, maxLines = 1)
            }
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(TickTheme.colors.coral)
                .clickable { submit() },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add task", tint = Color.White)
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    stroke: Dp = 8.dp,
    track: Color = Color.White.copy(alpha = 0.18f),
    color: Color = TickTheme.colors.coral,
    content: @Composable () -> Unit = {}
) {
    val animated by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(700), label = "ring")
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val width = stroke.toPx()
            val inset = width / 2
            val arcSize = androidx.compose.ui.geometry.Size(this.size.width - width, this.size.height - width)
            drawArc(track, 0f, 360f, false, topLeft = Offset(inset, inset), size = arcSize, style = Stroke(width))
            drawArc(color, -90f, 360f * animated, false, topLeft = Offset(inset, inset), size = arcSize, style = Stroke(width, cap = StrokeCap.Round))
        }
        content()
    }
}

@Composable
fun HeroCard(done: Int, total: Int, modifier: Modifier = Modifier) {
    val colors = TickTheme.colors
    val left = (total - done).coerceAtLeast(0)
    val (title, subtitle) = when {
        total == 0 -> "Fresh start" to "Plan something for today"
        left == 0 -> "All done" to "$done of $total finished"
        done == 0 -> "Let's go" to "$left tasks today"
        done * 2 >= total -> "Nice pace" to "$left left today"
        else -> "Keep going" to "$left left today"
    }

    val glow = colors.coral.copy(alpha = 0.32f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Brush.linearGradient(listOf(colors.heroStart, colors.heroMid, colors.heroEnd)))
            // Drawn, not laid out, so the glow never changes the card's height.
            .drawBehind {
                drawCircle(glow, radius = 75.dp.toPx(), center = Offset(size.width - 35.dp.toPx(), size.height + 15.dp.toPx()))
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressRing(progress = if (total == 0) 0f else done / total.toFloat()) {
                Text(text = "$done/$total", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Check
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 10.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(TickTheme.colors.coral)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 10.dp, y = (-14).dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(TickTheme.colors.amber)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TickTheme.colors.muted)
    }
}

/** Rounded chip used in the editor and the reminder sheet. */
@Composable
fun TickChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selected: Boolean = false,
    accent: Color? = null
) {
    val background = when {
        selected -> MaterialTheme.colorScheme.primary
        accent != null -> accent.copy(alpha = 0.16f)
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val content = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        accent != null -> accent
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) Icon(imageVector = icon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = content, maxLines = 1)
    }
}

@Composable
fun AppLogo(modifier: Modifier = Modifier, size: Dp = 88.dp, background: Color = Color(0xFF4338CA)) {
    val coral = Color(0xFFFF6B57)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.66f)) {
            val w = this.size.width
            val ringStroke = w * 0.085f
            drawCircle(Color.White.copy(alpha = 0.28f), radius = w * 0.31f, style = Stroke(ringStroke))
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.32f, w * 0.51f)
                lineTo(w * 0.45f, w * 0.64f)
                lineTo(w * 0.71f, w * 0.36f)
            }
            drawPath(path, coral, style = Stroke(w * 0.105f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
    }
}

@Composable
fun rememberSnackbarHost(): SnackbarHostState = remember { SnackbarHostState() }
