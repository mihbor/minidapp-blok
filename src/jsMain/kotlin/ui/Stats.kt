package ui

import BlockStats
import androidx.compose.runtime.Composable
import ltd.mbor.minimak.BurnStats
import ltd.mbor.minimak.Status
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.DisplayStyle.Companion.Flex
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun Stats(status: Status?, blockStats: Map<Int, BlockStats>, burnStats: Map<String, BurnStats>) {
  status?.let {
    Div({
      style {
        width(100.percent)
        display(Flex)
        justifyContent(JustifyContent.SpaceEvenly)
      }
    }) {
      Span {
        Text("Chain weight: ${it.chain.weight.toPlainString()}")
      }
      Span {
        Text("difficulty: ${it.chain.difficulty}")
      }
    }
  }
  BlockStats(blockStats)
  BurnStats(burnStats)
  BackfillIndicator()
}
