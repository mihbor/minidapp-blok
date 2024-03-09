package ui

import Block
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.getTxPoW
import ltd.mbor.minimak.json
import org.jetbrains.compose.web.attributes.cols
import org.jetbrains.compose.web.attributes.colspan
import org.jetbrains.compose.web.attributes.rows
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.dom.*
import scope

var showJson by mutableStateOf(false)
val txnCache = mutableStateMapOf<String, JsonElement?>()

@Composable
fun BlockDetails(block: Block) {
  var selected by remember { mutableStateOf<String?>(null) }
  Table {
    Tr {
      Td(attrs = { colspan(2)}) { Text("Details for block: ${block.number}") }
    }
    Tr {
      Td { Text("TxPoW ID") }
      Td { Text(block.hash) }
    }
    Tr {
      Td { Text("Timestamp") }
      Td { Text(block.timestamp.toString()) }
    }
    Tr {
      Td { Text("Size") }
      Td { Text(block.size.toString()) }
    }
    Tr {
      Td { Text("Nonce") }
      Td { Text(block.nonce.toPlainString()) }
    }
    Tr {
      Td { Text("Superblock level") }
      Td { Text(block.superBlockLevel.toString()) }
    }
    Tr {
      Td { Text("Parent") }
      Td { Text(block.parentHash) }
    }
    Tr {
      Td { Text("Transactions") }
      Td { Text(block.transactionCount.toString()) }
    }
    if (block.transactionCount > 0) Tr {
      Td()
      Td {
        val transactions: List<String> = Json.decodeFromJsonElement(block.txpow.jsonObject["body"]!!.jsonObject["txnlist"]!!)
        transactions.forEach { txnId ->
          Hr()
          Div({
            title("Click to expand/hide")
            onClick {
              if (selected != txnId){
                selected = txnId
                if (!txnCache.containsKey(txnId)) scope.launch {
                  console.log("caching txn $txnId")
                  val txnpow = MDS.getTxPoW(txnId)!!
                  txnCache.put(txnId, txnpow)
                }
              }
              else selected = null
            }
            if (txnId == selected) style {
              fontWeight("bold")
            }
          }) {
            Text(txnId)
          }
          if (txnId == selected) {
            TransactionDetails(txnId)
          }
        }
        Hr()
      }
    }
    Tr {
      Td(attrs = {
        style {
          property("vertical-align", "top")
        }
      }) {
        Button(attrs = {
          onClick { showJson = !showJson }
        }) {
          Text("${if (showJson) "Hide" else "Show"} JSON")
        }
      }
      Td {
        if (showJson) {
          TextArea(json.encodeToString(block.txpow)) {
            cols(150)
            rows(20)
          }
        }
      }
    }
  }
}
