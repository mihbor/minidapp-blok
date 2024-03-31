package ui

import androidx.compose.runtime.*
import ltd.mbor.minimak.log
import org.jetbrains.compose.web.attributes.ButtonType
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.onSubmit
import org.jetbrains.compose.web.attributes.type
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import setUrlParams

@Composable
fun Search(searchText: String?, searchFrom: String?, searchTo: String?, updateResults: (String?, String?, String?, Int) -> Unit) {

  var searchInput by remember { mutableStateOf(searchText ?: "") }
  var fromDate by remember { mutableStateOf(searchFrom ?: "") }
  var toDate by remember { mutableStateOf(searchTo ?: "") }

  fun search() {
    log("search $searchInput, from $fromDate, to $toDate")
    setUrlParams(searchInput.takeUnless { it.isBlank() }, fromDate.takeUnless { it.isBlank() }, toDate.takeUnless { it.isBlank() })
    updateResults(searchInput.takeUnless { it.isBlank() }, fromDate.takeUnless { it.isBlank() }, toDate.takeUnless { it.isBlank() }, 0)
  }

  Form(attrs = {
    onSubmit {
      log("submit")
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
          type(ButtonType.Button)
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
          type(ButtonType.Button)
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
          type(ButtonType.Button)
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
      type(ButtonType.Submit)
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
