package ui

import Block
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.jetbrains.compose.web.attributes.colspan
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.Position.Companion.Absolute
import org.jetbrains.compose.web.css.Position.Companion.Relative
import org.jetbrains.compose.web.dom.*

@Composable
fun BlockList(blocks: SnapshotStateList<Block>) {
  var selected by remember { mutableStateOf<String?>(null) }
  Table({
    style { width(100.percent) }
  }) {
    Thead {
      Tr {
        Td { Text("Height") }
        Td { Text("Hash") }
        Td { Text("Txns") }
        Td { Text("Time relayed") }
      }
    }
    Tbody {
      blocks.map { block ->
        key(block.hash) {
          Tr(attrs = {
            title("Click to expand/hide")
            onClick {
              console.log("Clicked ${block.hash} selected $selected")
              if (selected != block.hash) selected = block.hash
              else selected = null
              console.log("Clicked ${block.hash} selected $selected")
            }
            if (block.hash == selected) style {
              fontWeight("bold")
            }
          }) {
            Td { Text(block.number.toString()) }
            Td({
              style { position(Relative)
              width(100.percent)}
            }) {
              Div({
                style {
                  position(Absolute)
                  left(0.px)
                  top(0.px)
                  width(100.percent)
                  overflow("hidden")
                  whiteSpace("nowrap")
                  property("text-overflow", "ellipsis")
                }
              }) {
                Text(block.hash)
              }
            }
            Td { Text(block.transactionCount.toString()) }
            Td { Text(block.timestamp.toString()) }
          }
          if (block.hash == selected) {
            key("details") {
              Tr {
                Td(attrs = { colspan(4) }) {
                  BlockDetails(block)
                  Hr { }
                }
              }
            }
          }
        }
      }
    }
  }
}
