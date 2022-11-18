package ui

import androidx.compose.runtime.Composable
import kotlinx.serialization.json.decodeFromDynamic
import minima.Coin
import minima.json
import org.jetbrains.compose.web.dom.H5
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text

@Composable
fun TransactionDetails(txnId: String) {

  if (txnCache.containsKey(txnId)) {
    val transaction = txnCache[txnId].body.txn
    val inputs = json.decodeFromDynamic<Array<Coin>>(transaction.inputs)
    val outputs = json.decodeFromDynamic<Array<Coin>>(transaction.outputs)

    H5 { Text("Inputs") }
    Hr()
    inputs.forEach {
      InputOutputDetails(it)
      Hr()
    }
    H5 { Text("Outputs") }
    Hr()
    outputs.forEach {
      InputOutputDetails(it)
      Hr()
    }
  }
}