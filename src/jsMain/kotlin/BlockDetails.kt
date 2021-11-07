import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.colspan
import org.jetbrains.compose.web.dom.*

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
  }
}
