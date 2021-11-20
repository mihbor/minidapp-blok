import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Form
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.url.URLSearchParams

@Composable
fun Search() {
  var searchInput by mutableStateOf("")
  URLSearchParams(window.location.search).get("search")?.let{ searchInput = it }

  Form(attrs = {
    this.addEventListener("submit") {
      it.preventDefault()
      if (searchInput.isNotBlank()) {
        val searchParams = URLSearchParams(window.location.search)
        searchParams.set("search", searchInput)
        window.history.pushState(null, "", "?"+searchParams.toString())
      }
    }
  }) {
    Input(type = InputType.Text, attrs = {
      id("search-input")
      style {
        width(700.px)
      }
      value(searchInput)
      onInput {
        searchInput = it.value
      }
    })
    Button(attrs = {
      style {
        width(100.px)
      }
    }) {
      Text("search")
    }
  }
}