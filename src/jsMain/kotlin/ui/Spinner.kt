package ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun Spinner(diameter: CSSNumeric = 50.px, width: CSSNumeric = 100.percent, height: CSSNumeric = 60.vh) {
  Div({
    classes("spinner-overlay")
    style {
      width(width)
      height(height)
    }
  }) {
    Div({
      classes("spinner-container")
      style {
        width(diameter)
        height(diameter)
      }
    }) {
    }
  }
}
