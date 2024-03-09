package ui

import androidx.compose.runtime.Composable
import ltd.mbor.minimak.toTransaction
import org.jetbrains.compose.web.dom.H5
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text

@Composable
fun TransactionDetails(txnId: String) {

  txnCache[txnId]?.let {
    val transaction = it.toTransaction()

    H5 { Text("Inputs") }
    Hr()
    transaction.inputs.forEach {
      InputOutputDetails(it)
      Hr()
    }
    H5 { Text("Outputs") }
    Hr()
    transaction.outputs.forEach {
      InputOutputDetails(it)
      Hr()
    }
  }
}
