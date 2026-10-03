package uz.relay.todoapp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import uz.relay.todoapp.domain.model.Priority

/** Colors Material 3 has no slot for: secondary text, coral accent, priorities, list colors. */
@Immutable
data class TickColors(
    val muted: Color,
    val faint: Color,
    val line: Color,
    val surface2: Color,
    val coral: Color,
    val coralSoft: Color,
    val amber: Color,
    val slate: Color,
    val green: Color,
    val heroStart: Color,
    val heroMid: Color,
    val heroEnd: Color,
    val lists: List<Color>
) {
    fun listColor(index: Int): Color = lists[Math.floorMod(index, lists.size)]

    fun priorityColor(priority: Priority): Color = when (priority) {
        Priority.HIGH -> coral
        Priority.MEDIUM -> amber
        Priority.LOW -> slate
        Priority.NONE -> slate
    }
}

object LightPalette {
    val Background = Color(0xFFF6F6FB)
    val Surface = Color(0xFFFFFFFF)
    val Surface2 = Color(0xFFEEEEF6)
    val Line = Color(0xFFE6E6F0)
    val Text = Color(0xFF14142B)
    val Muted = Color(0xFF6B6C86)
    val Faint = Color(0xFFA3A4BA)
    val Primary = Color(0xFF4338CA)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimarySoft = Color(0xFFE7E6FB)
    val Coral = Color(0xFFFF6B57)
    val CoralSoft = Color(0xFFFFE7E3)

    val Colors = TickColors(
        muted = Muted,
        faint = Faint,
        line = Line,
        surface2 = Surface2,
        coral = Coral,
        coralSoft = CoralSoft,
        amber = Color(0xFFF5A524),
        slate = Color(0xFF8E93AD),
        green = Color(0xFF16A34A),
        heroStart = Color(0xFF6D5BFF),
        heroMid = Color(0xFF4338CA),
        heroEnd = Color(0xFF2E2596),
        lists = listOf(
            Color(0xFF4338CA), Color(0xFFFF6B57), Color(0xFF0EA5A4),
            Color(0xFFF5A524), Color(0xFFD946EF), Color(0xFF64748B)
        )
    )
}

object DarkPalette {
    val Background = Color(0xFF0D0E1A)
    val Surface = Color(0xFF171933)
    val Surface2 = Color(0xFF21243F)
    val Line = Color(0xFF262947)
    val Text = Color(0xFFECECFA)
    val Muted = Color(0xFFA2A3C2)
    val Faint = Color(0xFF6C6E92)
    val Primary = Color(0xFF8B8DFF)
    val OnPrimary = Color(0xFF0D0E1A)
    val PrimarySoft = Color(0xFF262862)
    val Coral = Color(0xFFFF8A78)
    val CoralSoft = Color(0xFF3D1F22)

    val Colors = TickColors(
        muted = Muted,
        faint = Faint,
        line = Line,
        surface2 = Surface2,
        coral = Coral,
        coralSoft = CoralSoft,
        amber = Color(0xFFF7BC4E),
        slate = Color(0xFF8E93AD),
        green = Color(0xFF4ADE80),
        heroStart = Color(0xFF5B4BEF),
        heroMid = Color(0xFF3A30B8),
        heroEnd = Color(0xFF221C7A),
        lists = listOf(
            Color(0xFF8B8DFF), Color(0xFFFF8A78), Color(0xFF2DD4BF),
            Color(0xFFF7BC4E), Color(0xFFE879F9), Color(0xFF94A3B8)
        )
    )
}

val LocalTickColors = staticCompositionLocalOf { LightPalette.Colors }
