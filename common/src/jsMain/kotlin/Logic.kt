import kotlinx.datetime.Instant
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

var lastBlockId: String? = null
tailrec suspend fun MdsApi.addTxPoW(txpow: JsonElement) {
  val block = json.decodeFromJsonElement<Block>(txpow)
  val txPoWSize = block.size
  val txPoWHeight = block.header.block
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
    val isBlock = txpow.jsonBoolean("isblock") == true
    val txIds = block.body.txnList
    val blockId = block.id
    val blockTime = block.header.timeMillis
    val blockResult = sql(
      insertBlock(
        blockId,
        txpowEncoded,
        txPoWHeight,
        if (isBlock) 1 else 0,
        blockTime,
        txIds.size
      )
    )
    if (blockResult?.jsonBoolean("status") == true) {
      log("inserted block $blockId relayed ${Instant.fromEpochMilliseconds(blockTime)}")
    }
    txIds.forEach {
      log("get txpow by id $it")
      getTxPoW(it)?.toTransaction()?.let { tx ->
        log("inserting transaction ${tx.transactionId}")
        try {
          val txResult = sql(
            insertTransaction(
              tx.transactionId,
              blockId,
              encodeURIComponent(json.encodeToString(tx.header)),
              encodeURIComponent(json.encodeToString(tx.inputs)),
              encodeURIComponent(json.encodeToString(tx.outputs)),
              encodeURIComponent(json.encodeToString(tx.state))
            )
          )
          if (txResult?.jsonBoolean("status") == true) {
            log("inserted transaction ${tx.transactionId}")
          }
        } catch (e: Throwable) {
          log(e.message ?: "ERROR")
        }
      }
    }
    val parentId = block.header.superParents.first().parent
    if (parentId != lastBlockId.also { lastBlockId = blockId }) {
      log("last block: $lastBlockId, parent $parentId")
      val result = sql(selectBlockById(parentId))
      if (result?.jsonBoolean("status") == true) {
        log("selectBlockById $parentId results: ${result.jsonBoolean("results")}")
        if (result.jsonBoolean("results")) {
          if (result.jsonObject("rows").jsonArray.firstOrNull() == null) {
            log("parent $parentId not found, getting from MDS")
            val parent = getTxPoW(parentId)
            if (parent != null) addTxPoW(parent)
          }
        }
      }
    }
  }
}

data class BlockStats(
  val blockCount: Int,
  val txCount: Int,
)

suspend fun MdsApi.getBlockStats() = listOf(1, 24, 7*24).map {
  it to sql(selectStats(it))!!.statsResult()
}.toMap()

private fun JsonElement.statsResult() = jsonObject("rows").jsonArray.single().let {
  BlockStats(it.jsonObject("COUNT(*)").jsonPrimitive.content.toInt(), it.jsonObject("SUM(TXNS)").jsonPrimitive.content.toInt())
}
