package ui

import Block
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.jetbrains.compose.web.attributes.colspan
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.dom.*

@Composable
fun BlockList(blocks: SnapshotStateList<Block>) {
  var selected by remember { mutableStateOf<String?>(null) }
  Table {
    Thead {
      Tr {
        Td { Text("Height") }
        Td { Text("ID") }
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
            Td { Text(block.hash) }
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
