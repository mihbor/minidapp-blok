import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
fun Search(searchParam: String?, results: SnapshotStateList<Block>, setSearching: (Boolean) -> Unit) {
  var searchInput by mutableStateOf(searchParam ?: "")

  Form(attrs = {
    this.addEventListener("submit") {
      it.preventDefault()
      if (searchInput.isNotBlank()) {
        val searchParams = URLSearchParams(window.location.search)
        searchParams.set("search", searchInput)
        window.history.pushState(null, "", "?$searchParams")
        setSearching(true)
        results.clear()
        populateBlocks(search(searchInput), results)
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