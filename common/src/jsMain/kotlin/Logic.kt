import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import ltd.mbor.minimak.*

const val appName = "BloK"

const val txPoWMaxSize = 16000

suspend fun MdsApi.createSQL() {
  log("$INIT_SQL; $INDEX_HASH; $INDEX_HEIGHT")
  val sqlResult = sql("$INIT_SQL; $INDEX_HASH; $INDEX_HEIGHT")
  if (sqlResult?.jsonBoolean("status") != true) {
    log("$appName : ERROR in SQL call!")
    log("$appName : ${JSON.stringify(sqlResult)}")
  }
}

suspend fun MdsApi.addTxPoW(txpow: JsonElement) {
  val header = txpow.jsonObject["header"]!!
  val txPoWSize = txpow.jsonObject["size"]!!.jsonPrimitive.int
  val txPoWHeight = header.jsonObject["block"]!!.jsonPrimitive.int
  if (txpow.jsonObject["body"] == null) {
    log("txpow body not found!")
  } else {
//    txpow.body.witness.signatures = null
//    txpow.body.witness.mmrproofs = null
    log("addTxPoW")
    val txpowEncoded = encodeURIComponent(json.encodeToString(txpow))
    log("txpowEncoded")
    if (txPoWSize > txPoWMaxSize) {
      log("$appName: Transaction at height: $txPoWHeight with size: $txPoWSize is too big for database column.")
    } else {
      val isBlock = if (txpow.jsonBoolean("isblock") == true) 1 else 0
      val sqlResult = sql(
        insertBlock(
          txpowEncoded,
          txPoWHeight,
          txpow.jsonString("txpowid"),
          isBlock,
          header.jsonObject["timemilli"]!!.jsonPrimitive.long,
          txpow.jsonObject["body"]!!.jsonObject["txnlist"]!!.jsonArray.size
        )
      )
      if (sqlResult?.jsonBoolean("status") == true) {
        log("$appName: timemilli ${header.jsonObject["timemilli"]?.jsonPrimitive?.long}")
        log("TxPoW Added To SQL Table... ")
      }
    }
  }
}
