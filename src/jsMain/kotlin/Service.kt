import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import ltd.mbor.minimak.*
import org.w3c.dom.url.URLSearchParams
import org.w3c.workers.ServiceWorkerGlobalScope

const val appName = "BloK"

const val txPoWMaxSize = 16000

suspend fun createSQL() {
  val sqlResult = MDS.sql("$INIT_SQL; $INDEX_HASH; $INDEX_HEIGHT")!!
  log(sqlResult.toString())
  if (sqlResult.jsonBoolean("status") != true) {
    log("$appName : ERROR in SQL call!")
  }
}

suspend fun addTxPoW(txpow: JsonElement) {
  val header = txpow.jsonObject["header"]!!
  val txPoWSize = txpow.jsonObject["size"]!!.jsonPrimitive.int
  val txPoWHeight = header.jsonObject["block"]!!.jsonPrimitive.int
  if (txpow.jsonObject["body"] == null) {
    log("txpow body not found!")
  } else {
//    txpow.body.witness.signatures = null
//    txpow.body.witness.mmrproofs = null
    val txpowEncoded = encodeURIComponent(JSON.stringify(txpow).replace("'", "%27"))
    if (txPoWSize > txPoWMaxSize) {
      log("$appName: Transaction at height: $txPoWHeight with size: $txPoWSize is too big for database column.")
    } else {
      val isBlock = if (txpow.jsonBoolean("isblock") == true) 1 else 0
      val sqlResult = MDS.sql(insertBlock(txpowEncoded, txPoWHeight, txpow.jsonString("txpowid")!!, isBlock, header.jsonObject["timemilli"]!!.jsonPrimitive.long, txpow.jsonObject["body"]!!.jsonObject["txnlist"]!!.jsonArray.size))!!
      if (sqlResult.jsonBoolean("status") == true) {
        log("$appName: timemilli ${header.jsonObject["timemilli"]?.jsonPrimitive?.long}")
        log("TxPoW Added To SQL Table... ")
      }
    }
  }
}

external val self: ServiceWorkerGlobalScope

fun service() {
  self.addEventListener("install", {
    console.log("Service worker installed")
  })
  self.addEventListener("activate", {
    console.log("Service worker activated")
    scope.launch {
      runMinima()
    }
  })
}

suspend fun runMinima() {
  val uid = URLSearchParams(self.location.search).get("uid")
  MDS.init(uid ?: "0x0", "localhost", 9003) { msg ->
    if (msg.jsonString("event") == "inited") {
      createSQL()
    } else if (msg.jsonString("event") == "NEWBLOCK") {
      addTxPoW(msg.jsonObject["data"]!!.jsonObject["txpow"]!!)
    }
  }
}