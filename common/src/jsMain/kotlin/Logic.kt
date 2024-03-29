import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.*

const val appName = "BloK"

const val txPoWMaxSize = 16000

suspend fun MdsApi.createSQL() {
  log(INIT_SQL)
  val sqlResult = sql(INIT_SQL)
  if (sqlResult?.jsonBoolean("status") != true) {
    log("$appName : ERROR in SQL call!")
    try {
      log("$appName : ${JSON.stringify(sqlResult)}")
    } catch (e: Exception) {
      log("$appName : cannot log sqlResult")
    }
  }
}

var lastBlockId: String? = null
var isBackfilling: Boolean = false

tailrec suspend fun MdsApi.addTxPoW(txpow: JsonElement) {
  val block = json.decodeFromJsonElement<Block>(txpow)
  if (txpow.jsonObject["body"] == null) {
    log("txpow body not found!")
  } else {
    addBlock(block, txpow)
    block.body.txnList.forEach {
      log("get txpow by id $it")
      getTxPoW(it)?.toTransaction()?.let { addTransaction(it, block.id) }
    }
    val parentId = block.header.superParents.first().parent
    if (parentId != lastBlockId.also { lastBlockId = block.id }) {
      log("last block: $lastBlockId, parent $parentId")
      if (blockExists(parentId) == false) {
        log("parent $parentId not found, getting from MDS")
        getTxPoW(parentId)?.let { parent ->
          markBackfilling(true)
          return addTxPoW(parent)
        }
      }
    }
    markBackfilling(false)
  }
}

private suspend fun MdsApi.blockExists(id: String): Boolean? {
  return sql(selectBlockById(id))?.takeIf{ it.jsonBoolean("status") }?.let{ result ->
    log("selectBlockById $id results: ${result.jsonBoolean("results")}")
    result.jsonBoolean("results") && result.jsonObject("rows").jsonArray.firstOrNull() != null
  }
}

private suspend fun MdsApi.markBackfilling(backfilling: Boolean) {
  if (backfilling != isBackfilling) {
    sql(setFlag("backfilling", backfilling))
    isBackfilling = backfilling
  }
}

private suspend fun MdsApi.addTransaction(tx: Transaction, blockId: String) {
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

private suspend fun MdsApi.addBlock(block: Block, txpow: JsonElement) {
  log("addTxPoW")
  val txpowEncoded = if (block.size > txPoWMaxSize) {
    log("$appName: Transaction at height: ${block.header.block} with size: ${block.size} is too big for database column.")
    null
  } else encodeURIComponent(json.encodeToString(txpow))
  val blockResult = sql(
    insertBlock(
      id = block.id,
      txpow = txpowEncoded,
      height = block.header.block,
      isBlock = if (txpow.jsonBoolean("isblock")) 1 else 0,
      relayed = block.header.timeMillis,
      length = block.body.txnList.size
    )
  )
  if (blockResult?.jsonBoolean("status") == true) {
    log("inserted block ${block.id} relayed ${Instant.fromEpochMilliseconds(block.header.timeMillis)}")
  }
}

data class BlockStats(
  val blockCount: Int,
  val txCount: Int,
  val minTimeMillis: Long?,
)

suspend fun MdsApi.getBlockStats() = listOf(1, 24, 7*24).map {
  it to sql(selectStats(it))!!.statsResult()
}.toMap()

private fun JsonElement.statsResult() = jsonObject("rows").jsonArray.single().let {
  BlockStats(
    it.jsonString("COUNT(*)").toInt(),
    it.jsonStringOrNull("SUM(TXNS)")?.toInt() ?: 0,
    it.jsonStringOrNull("MIN(RELAYED)")?.toLong()
  )
}
