import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.browser.window
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*
import ltd.mbor.minimak.*

suspend fun populateBlocks(sql: String): List<Block> {
  return try {
    val result = MDS.sql(sql)
    if (result?.jsonBoolean("status") == true) {
      result.jsonObject["rows"]?.jsonArray?.map {
        it.jsonString("TXPOW")
          .let(::decodeURIComponent)
          .let{ json.decodeFromString<JsonElement>(it) }
          .let(::mapTxPoW)
      } ?: throw Error("1. Fetching from sql failed.")
    } else {
      throw Error("2. Fetching from sql failed.")
    }
  } catch(err: Error) {
    log(err.toString())
    emptyList()
  }
}

suspend fun initMinima(uid: String?, consumer: (Block) -> Unit) {

  MDS.init(uid ?: "0x00", window.location.hostname, 9004){
    val msg = it
    when(msg.jsonString("event")) {
      "inited" -> console.log("Connected to Minima.")
      "NEWBLOCK" -> {
        val txpow = msg.jsonObject["data"]!!.jsonObject["txpow"]!!
        console.log("isBlock: ${txpow.jsonBoolean("isblock")}")
        if (txpow.jsonBoolean("isblock")) {
          consumer.invoke(mapTxPoW(txpow))
        }
      }
    }
  }
}

fun mapTxPoW(txpow: JsonElement): Block {
  val header = txpow.jsonObject["header"]!!
  val isTransaction = txpow.jsonBoolean("istransaction")
  return Block(
    hash = txpow.jsonString("txpowid"),
    number = header.jsonString("block").toLong(),
    transactionCount = txpow.jsonObject["body"]!!.jsonObject["txnlist"]!!.jsonArray.size + if (isTransaction) 1 else 0,
    timestamp = Instant.fromEpochMilliseconds(header.jsonString("timemilli").toLong()),
    size = txpow.jsonObject["size"]!!.jsonPrimitive.long,
    nonce = header.jsonString("nonce").toBigDecimal(),
    superBlockLevel = txpow.jsonObject["superblock"]!!.jsonPrimitive.int.toByte(),
    parentHash = header.jsonObject["superparents"]!!.jsonArray[0].jsonString("parent"),
    txpow
  )
}
