import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import ltd.mbor.minimak.*

const val appName = "BloK"

const val txPoWMaxSize = 16000

suspend fun MdsApi.createSQL() {
  log(INIT_SQL)
  val sqlResult = sql(INIT_SQL)
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
    val txpowEncoded = if (txPoWSize > txPoWMaxSize) {
      log("$appName: Transaction at height: $txPoWHeight with size: $txPoWSize is too big for database column.")
      null
    } else encodeURIComponent(json.encodeToString(txpow))
    val isBlock = if (txpow.jsonBoolean("isblock") == true) 1 else 0
    val txIds = txpow.jsonObject("body").jsonObject("txnlist").jsonArray
    val blockId = txpow.jsonString("txpowid")
    val sqlResult = sql(
      insertBlockSql(
        txpowEncoded,
        txPoWHeight,
        blockId,
        isBlock,
        header.jsonObject("timemilli").jsonPrimitive.long,
        txIds.size
      )
    )
    if (sqlResult?.jsonBoolean("status") == true) {
      log("inserted block $blockId")
    }
    txIds.forEach {
      log("get txpow by id $it")
      getTxPoW(it.jsonPrimitive.content)?.toTransaction()?.let { tx ->
        log("inserting transaction ${tx.transactionId}")
        val sqlResult = sql(
          insertTransactionSql(
            tx.transactionId,
            blockId,
            encodeURIComponent(json.encodeToString(tx.header)),
            encodeURIComponent(json.encodeToString(tx.inputs)),
            encodeURIComponent(json.encodeToString(tx.outputs)),
            encodeURIComponent(json.encodeToString(tx.state))
          )
        )
        if (sqlResult?.jsonBoolean("status") == true) {
          log("inserted transaction ${tx.transactionId}")
        }
      }
    }
  }
}
