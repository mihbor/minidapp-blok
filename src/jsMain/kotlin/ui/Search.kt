package ui

import Block
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.PopStateEvent
import org.w3c.dom.url.URL
import populateBlocks
import scope
import searchBlocksAndTransactions

@Composable
fun Search(searchParam: String?, results: SnapshotStateList<Block>, setSearching: (Boolean) -> Unit) {

  var searchInput by mutableStateOf(searchParam ?: "")
  var fromDate by mutableStateOf("")
  var toDate by mutableStateOf("")

  fun setSearchParam(search: String?) {
    val url = URL(window.location.href)
    if (search != null) url.searchParams.set("search", search)
    else url.searchParams.delete("search")
    window.history.pushState(search, "", url.toString())
  }

  fun updateResults(search: String, fromDate: String? = null, toDate: String? = null) {
    scope.launch {
      results.clear()
      populateBlocks(searchBlocksAndTransactions(search, fromDate, toDate), results)
      setSearching(true)
    }
  }

  fun clearSearch() {
    searchInput = ""
    setSearching(false)
  }

  window.addEventListener("popstate", {
    val event = it as PopStateEvent
    if(event.state == null) {
      clearSearch()
    } else {
      searchInput = it.state.toString()
      updateResults(searchInput)
    }
  })

  Form(attrs = {
    this.addEventListener("submit") {
      it.preventDefault()
      if (searchInput.isNotBlank()) {
        setSearchParam(searchInput)
        updateResults(searchInput, fromDate.takeUnless { it.isBlank() }, toDate.takeUnless { it.isBlank() })
      }
    }
  }) {
    Div(attrs = {
      style {
        display(DisplayStyle.InlineBlock)
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
      if (searchInput.isNotBlank()) {
        Button(attrs = {
          style {
            width(25.px)
          }
          onClick {
            it.preventDefault()
            setSearchParam(null)
            clearSearch()
          }
        }) {
          Text("X")
        }
      }
      Br()
      Text("From date")
      DateTimeLocalInput(fromDate, attrs = {
        onChange { fromDate = it.value; console.log("fromDate = $fromDate") }
      })
      if (fromDate.isNotBlank()) {
        Button(attrs = {
          style {
            width(25.px)
          }
          onClick {
            it.preventDefault()
            fromDate = ""
          }
        }) {
          Text("X")
        }
      }
      Text("To date")
      DateTimeLocalInput(toDate, attrs = {
        onChange { toDate = it.value; console.log("toDate = $toDate") }
      })
      if (toDate.isNotBlank()) {
        Button(attrs = {
          style {
            width(25.px)
          }
          onClick {
            it.preventDefault()
            toDate = ""
          }
        }) {
          Text("X")
        }
      }
    }
    Button(attrs = {
      style {
        width(100.px)
        height(40.px)
        property("vertical-align", "top")
      }
    }) {
      Text("search")
    }
  }
}
