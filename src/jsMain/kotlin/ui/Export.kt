package ui

import Block
import androidx.compose.runtime.Composable
import kotlinx.browser.document
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getTxPoW
import ltd.mbor.minimak.json
import ltd.mbor.minimak.toTransaction
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.File
import org.w3c.files.FilePropertyBag
import scope
import txnCache

@Composable
fun Export(blocks: List<Block>) {
  Button({
    onClick {
      scope.launch {
        val csv = StringBuilder().appendLine("Height,Hash,Transaction Count,Time relayed,JSON,transactions")
        blocks.forEach {
          val transactions: List<String> = Json.decodeFromJsonElement(it.txpow.jsonObject["body"]!!.jsonObject["txnlist"]!!)
          transactions.forEach { txnId ->
            if (!txnCache.containsKey(txnId)) {
              console.log("caching txn $txnId")
              val txnpow = MDS.getTxPoW(txnId)!!
              txnCache.put(txnId, txnpow)
            }
          }
          val data = listOf(
            it.number,
            it.hash,
            it.transactionCount,
            it.timestamp,
            json.encodeToString(it.txpow).replace("\n", "").replace("\"", "\"\"")
          ) + transactions.mapNotNull { txnCache[it]?.toTransaction()?.let(json::encodeToString)?.replace("\n", "")?.replace("\"", "\"\"") }
          csv.appendLine(data.joinToString("\",\"", "\"", "\""))
        }
        val file = File(csv.toString().toCharArray().toTypedArray(), "minima-blocks.csv", FilePropertyBag(type = "text/csv"))
        val link = document.createElement("a") as HTMLAnchorElement
        val url = URL.createObjectURL(file)
        link.href = url
        link.download = file.name
        link.click()
        URL.revokeObjectURL(url)
      }
    }
  }) {
    Text("Export")
  }
}
