package ui

import BlockStats
import androidx.compose.runtime.Composable
import kotlinx.datetime.Clock
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.DisplayStyle.Companion.Flex
import org.jetbrains.compose.web.dom.B
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun BlockStats(blockStats: Map<Int, BlockStats>, color: Color.RGBA = Color.RGBA(0, 0, 255, 1)) {
  Div({
    style {
      width(100.percent)
      display(Flex)
      justifyContent(JustifyContent.SpaceEvenly)
    }
  }) {
    blockStats.entries.sortedBy { it.key }.forEachIndexed { index, it ->
      Span({
        style {
          backgroundColor(color.copy(a = 0.5f - index * 0.1f))
          width(33.33.percent)
        }
      }) {
        B({
          style { color(rgba(255, 255, 255, 0.75)) }
        }) {
          Text(if (it.key <= 48) "${it.key}hour" else "${it.key / 24}day")
        }
        Text(" transactions: ${it.value.txCount} | blocks: ${it.value.blockCount} ")
        if (it.value.blockCount > 0 && it.value.minTimeMillis != null) {
          Text("| average block time: ${(Clock.System.now().toEpochMilliseconds() - it.value.minTimeMillis!!) / 1000 / it.value.blockCount}s")
        }
      }
    }
  }
}
