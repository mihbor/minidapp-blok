import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Tr

@Composable
fun InputOutputDetails(it: InputOutput) {

  Table {
    Tr {
      Td { Text("address") }
      Td { Text(it.mxaddress) }
    }
    Tr {
      Td { Text("token") }
      Td { Text(it.tokenid) }
    }
    Tr {
      Td { Text("amount") }
      Td { Text(it.amount) }
    }
  }
}