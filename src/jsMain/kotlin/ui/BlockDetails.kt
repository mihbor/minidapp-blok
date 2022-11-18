package ui

import Block
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import minima.MDS
import org.jetbrains.compose.web.attributes.cols
import org.jetbrains.compose.web.attributes.colspan
import org.jetbrains.compose.web.attributes.rows
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.dom.*
import scope

var showJson by mutableStateOf(false)
val txnCache = mutableStateMapOf<String, dynamic>()

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
        val transactions = Json.decodeFromDynamic<Array<String>>(block.txpow.body.txnlist)
        transactions.forEach { txnId ->
          Hr()
          Div({
            title("Click to expand/hide")
            onClick {
              if (selected != txnId){
                selected = txnId
                if (!txnCache.containsKey(txnId)) scope.launch {
                  console.log("caching txn $txnId")
                  val txnpow = MDS.cmd("txpow txpowid:$txnId")
                  txnCache.put(txnId, txnpow.response)
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
          TextArea(JSON.stringify(block.txpow, null, 2)) {
            cols(150)
            rows(20)
          }
        }
      }
    }
  }
}
