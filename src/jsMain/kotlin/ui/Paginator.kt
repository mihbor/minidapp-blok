package ui

import androidx.compose.runtime.Composable
import ltd.mbor.minimak.log
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun Paginator(page: Int, limit: Int, results: Int, setPage: (Int) -> Unit) {
  log("Paginator page: $page, results: $results")
  if (page > 0) {
    Button({
      onClick {
        setPage(page - 1)
      }
    }) {
      Text("Previous")
    }
  }
  if (results == limit) {
    Button({
      onClick {
        setPage(page + 1)
      }
    }) {
      Text("Next")
    }
  }
}
