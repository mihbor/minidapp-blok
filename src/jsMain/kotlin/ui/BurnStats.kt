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
fun BurnStats(burn: Map<String, BurnStats>, color: Color.RGBA = Color.RGBA(255, 0, 0, 1)) {
  Div({
    style {
      width(100.percent)
      display(Flex)
      justifyContent(JustifyContent.SpaceEvenly)
    }
  })  {
    burn.entries.sortedBy { it.key.takeWhile { it.isDigit() } }.forEachIndexed { index, it ->
      Span({
        style {
          backgroundColor(color.copy(a = 0.5f - index * 0.1f))
          width(33.33.percent)
        }
      }) {
        B({
          style { color(rgba(255, 255, 255, 0.75)) }
        }) { Text(it.key) }
        Text(" transactions: ${it.value.txns} | average burn: ${it.value.avg}")
      }
    }
  }
}
