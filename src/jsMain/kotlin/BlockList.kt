import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.jetbrains.compose.web.attributes.colspan
import org.jetbrains.compose.web.dom.*

@Composable
fun BlockList(blocks: SnapshotStateList<Block>) {
  var selected by mutableStateOf<Block?>(null)
  Table {
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
        Tr(attrs = {
          id(block.hash)
          onClick {
            if(selected?.hash != block.hash) selected = block
            else selected = null
          }
        }) {
          Td { Text(block.number.toString()) }
          Td { Text(block.hash) }
          Td { Text(block.transactionCount.toString()) }
          Td { Text(block.timestamp.toString()) }
        }
        if (block.hash == selected?.hash) {
          Tr(attrs = {
            id("details")
          }) {
            Td(attrs = { colspan(4) }) {
              BlockDetails(block)
              Hr {  }
            }
          }
        }
      }
    }
  }
}
