package ui

import BlockStats
import androidx.compose.runtime.Composable
import kotlinx.datetime.Clock
import org.jetbrains.compose.web.css.DisplayStyle.Companion.InlineBlock
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.dom.B
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun BlockStats(blockStats: Map<Int, BlockStats>) {
  Div {
    blockStats.entries.sortedBy { it.key }.forEach {
      Span({
        style {
          display(InlineBlock)
        }
      }) {
        B { Text(if (it.key <= 48) "${it.key} hours" else "${it.key / 24} days") }
        Text(" blocks: ${it.value.blockCount}, transactions: ${it.value.txCount}, ")
        if (it.value.blockCount > 0 && it.value.minTimeMillis != null) {
          Text("avg block time: ${(Clock.System.now().toEpochMilliseconds() - it.value.minTimeMillis!!) / 1000 / it.value.blockCount}s")
        }
      }
    }
  }
}
