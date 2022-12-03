package ui

import androidx.compose.runtime.Composable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.Coin
import ltd.mbor.minimak.json
import org.jetbrains.compose.web.dom.H5
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text

@Composable
fun TransactionDetails(txnId: String) {

  txnCache[txnId]?.let {
    val transaction = it.jsonObject["body"]!!.jsonObject["txn"]!!
    val inputs = json.decodeFromJsonElement<List<Coin>>(transaction.jsonObject["inputs"]!!)
    val outputs = json.decodeFromJsonElement<List<Coin>>(transaction.jsonObject["outputs"]!!)

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