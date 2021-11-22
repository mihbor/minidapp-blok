import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.attributes.cols
import org.jetbrains.compose.web.attributes.colspan
import org.jetbrains.compose.web.attributes.rows
import org.jetbrains.compose.web.dom.*

var showJson by mutableStateOf(false)
@Composable
fun BlockDetails(block: Block) {
  Table {
    Tr {
      Td(attrs = { colspan(2)}) { Text("Details for block: ${block.number}") }
    }
    Tr {
      Td { Text("TxPoW ID") }
      Td { Text(block.hash) }
    }
    Tr {
      Td { Text("Timestamp") }
      Td { Text(block.timestamp.toString()) }
    }
    Tr {
      Td { Text("Size") }
      Td { Text(block.size.toString()) }
    }
    Tr {
      Td { Text("Transaction Count") }
      Td { Text(block.transactionCount.toString()) }
    }
    Tr {
      Td { Text("Nonce") }
      Td { Text(block.nonce.toString()) }
    }
    Tr {
      Td { Text("Superblock level") }
      Td { Text(block.superBlockLevel.toString()) }
    }
    Tr {
      Td { Text("Parent") }
      Td { Text(block.parentHash) }
    }
    Tr {
      Td(attrs = {
        style {
          property("vertical-align", "top")
        }
      }) {
        Button(attrs = {
          onClick { showJson = !showJson }
        }) {
          Text("${if (showJson) "Hide" else "Show"} JSON")
        }
      }
      Td {
        if (showJson) {
          TextArea(JSON.stringify(block.txpow, null, 2)) {
            cols(150)
            rows(20)
          }
        }
      }
    }
  }
}
