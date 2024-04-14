package ui

import androidx.compose.runtime.Composable
import ltd.mbor.minimak.BurnStats
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.DisplayStyle.Companion.Flex
import org.jetbrains.compose.web.dom.B
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun BurnStats(burn: Map<String, BurnStats>) {
  Div({
    style {
      width(100.percent)
      display(Flex)
      justifyContent(JustifyContent.SpaceEvenly)
    }
  })  {
    burn.entries.sortedBy { it.key.takeWhile { it.isDigit() } }.forEach {
      Span {
        B { Text(it.key) }
        Text(" transactions: ${it.value.txns}, avg burn: ${it.value.avg}; ")
      }
    }
  }
}
