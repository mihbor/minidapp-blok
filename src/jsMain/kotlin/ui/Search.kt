package ui

import Block
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
import org.w3c.dom.PopStateEvent
import org.w3c.dom.url.URL
import populateBlocks
import search

@Composable
fun Search(searchParam: String?, results: SnapshotStateList<Block>, setSearching: (Boolean) -> Unit) {

  var searchInput by mutableStateOf(searchParam ?: "")

  fun setSearchParam(search: String?) {
    val url = URL(window.location.href)
    if (search != null) url.searchParams.set("search", search)
    else url.searchParams.delete("search")
    window.history.pushState(search, "", url.toString())
  }

  fun updateResults(search: String) {
    results.clear()
    populateBlocks(search(search), results)
    setSearching(true)
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
        updateResults(searchInput)
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
    Button(attrs = {
      style {
        width(100.px)
      }
    }) {
      Text("search")
    }
  }
}