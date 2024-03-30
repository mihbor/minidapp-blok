package ui

import Block
import RESULT_LIMIT
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.browser.window
import kotlinx.coroutines.launch
import ltd.mbor.minimak.log
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.PopStateEvent
import org.w3c.dom.url.URL
import populateBlocks
import scope
import searchBlocksAndTransactions

@Composable
fun Search(searchText: String?, searchFrom: String?, searchTo: String?, results: SnapshotStateList<Block>, setSearching: (Boolean) -> Unit) {

  var searchInput by mutableStateOf(searchText ?: "")
  var fromDate by mutableStateOf(searchFrom ?: "")
  var toDate by mutableStateOf(searchTo ?: "")

  fun setSearchParams(search: String?, fromDate: String?, toDate: String?) {
    val url = URL(window.location.href)
    if (search != null) url.searchParams.set("searchText", search)
    else url.searchParams.delete("searchText")
    if (fromDate != null) url.searchParams.set("searchFrom", fromDate)
    else url.searchParams.delete("searchFrom")
    if (toDate != null) url.searchParams.set("searchTo", toDate)
    else url.searchParams.delete("searchTo")
    window.history.pushState(listOf(search, fromDate, toDate).map { it ?: "" }.joinToString(";"), "", url.toString())
  }

  fun updateResults(search: String?, fromDate: String?, toDate: String?) {
    scope.launch {
      results.clear()
      populateBlocks(searchBlocksAndTransactions(search, fromDate, toDate, RESULT_LIMIT), results)
      setSearching(true)
    }
  }

  fun search() {
    log("search $searchInput, from $fromDate, to $toDate")
    setSearchParams(searchInput.takeUnless { it.isBlank() }, fromDate.takeUnless { it.isBlank() }, toDate.takeUnless { it.isBlank() })
    updateResults(searchInput.takeUnless { it.isBlank() }, fromDate.takeUnless { it.isBlank() }, toDate.takeUnless { it.isBlank() })
  }

  window.addEventListener("popstate", {
    val event = it as PopStateEvent
    if(event.state == null) {
      searchInput = ""
      fromDate = ""
      toDate = ""
      setSearching(false)
    } else {
      it.state.toString().split(";").let {
        searchInput = it[0]
        fromDate = it[1]
        toDate = it[2]
      }
      updateResults(searchInput, fromDate, toDate)
    }
  })

  Form(attrs = {
    this.addEventListener("submit") {
      it.preventDefault()
      search()
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
            searchInput = ""
            search()
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
            search()
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
            search()
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
