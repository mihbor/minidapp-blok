package ui

import androidx.compose.runtime.Composable
import ltd.mbor.minimak.Coin
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Tr
import tokens

@Composable
fun InputOutputDetails(it: Coin) {

  Table {
    Tr {
      Td { Text("address") }
      Td { Text(it.address) }
    }
    Tr {
      Td { Text("miniaddress") }
      Td { Text(it.miniAddress) }
    }
    Tr {
      Td { Text("token") }
      Td {
        TokenName(it.tokenId, tokens)
      }
    }
    Tr {
      Td { Text("amount") }
      Td { Text(it.tokenAmount.toPlainString()) }
    }
  }
}