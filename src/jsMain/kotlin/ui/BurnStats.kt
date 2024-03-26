package ui

import androidx.compose.runtime.Composable
import ltd.mbor.minimak.BurnStats
import org.jetbrains.compose.web.css.DisplayStyle.Companion.InlineBlock
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.dom.B
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun BurnStats(burn: Map<String, BurnStats>) {
  Div {
    burn.forEach {
      Span({
        style {
          display(InlineBlock)
        }
      }) {
        B { Text(it.key) }
        Text(" transactions: ${it.value.txns}, avg burn: ${it.value.avg}; ")
      }
    }
  }
}
