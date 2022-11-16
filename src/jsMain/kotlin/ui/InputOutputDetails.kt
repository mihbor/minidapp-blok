package ui

import InputOutput
import androidx.compose.runtime.Composable
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import minima.Token
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Tr
import tokens

infix fun BigDecimal.toToken(token: Token?) = this * BigDecimal.TEN.pow(token?.scale ?: 0)

infix fun String.toToken(token: Token?) = BigDecimal.parseString(this).toToken(token).toPlainString()

@Composable
fun InputOutputDetails(it: InputOutput) {

  Table {
    Tr {
      Td { Text("address") }
      Td { Text(it.address) }
    }
    Tr {
      Td { Text("mxaddress") }
      Td { Text(it.mxaddress) }
    }
    Tr {
      Td { Text("token") }
      Td {
        TokenName(it.tokenid, tokens)
      }
    }
    Tr {
      Td { Text("amount") }
      Td { Text(it.amount toToken tokens[it.tokenid]) }
    }
  }
}