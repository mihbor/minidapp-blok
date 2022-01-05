import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import org.jetbrains.compose.web.dom.H5
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text
import ui.InputOutputDetails
import ui.txnCache

@Serializable
data class InputOutput(
  val coinid: String,
  val address: String,
  val mxaddress: String,
  val tokenid: String,
  val amount: String
)

@Composable
fun TransactionDetails(txn: String) {

  if (txnCache.containsKey(txn)) {
    val transaction = txnCache[txn].body.txn
    val inputs = Json.decodeFromDynamic<Array<InputOutput>>(transaction.inputs)
    val outputs = Json.decodeFromDynamic<Array<InputOutput>>(transaction.outputs)

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