package ui

import androidx.compose.runtime.Composable
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import minima.Coin
import minima.Token
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Tr
import tokens

infix fun BigDecimal.toToken(token: Token?) = this * BigDecimal.TEN.pow(token?.scale ?: 0)

infix fun String.toToken(token: Token?) = BigDecimal.parseString(this).toToken(token).toPlainString()

@Composable
fun InputOutputDetails(it: Coin) {

  Table {
    Tr {
      Td { Text("address") }
      Td { Text(it.address) }
    }
    Tr {
      Td { Text("miniaddress") }
      Td { Text(it.miniaddress) }
    }
    Tr {
      Td { Text("token") }
      Td {
        TokenName(it.tokenid, tokens)
      }
    }
    Tr {
      Td { Text("amount") }
      Td { Text(it.tokenamount.toPlainString()) }
    }
  }
}