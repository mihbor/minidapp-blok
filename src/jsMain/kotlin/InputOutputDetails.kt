import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*

@Composable
fun InputOutputDetails(it: InputOutput) {

  Table {
    Tr {
      Td { Text("address") }
      Td { Text(it.mxaddress) }
    }
    Tr {
      Td { Text("token") }
      Td {
        tokens[it.tokenid]?.let { token ->
          token.icon ?: "minima.svg".takeIf { token.tokenid == "0x00" }?.let {
            Img(it) {
              style {
                width(24.px)
                height(24.px)
              }
            }
          }
          Text(" ${token.token}")
        } ?: Text(it.tokenid)
      }
    }
    Tr {
      Td { Text("amount") }
      Td { Text(it.amount) }
    }
  }
}