import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import ltd.mbor.minimak.*
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.url.URLSearchParams
import ui.BlockList
import ui.Search

data class Block(
  val hash: String,
  val number: Long,
  val transactionCount: Int,
  val timestamp: Instant,
  val size: Long,
  val nonce: BigDecimal,
  val superBlockLevel: Byte,
  val parentHash: String,
  val txpow: JsonElement
)

val scope = MainScope()
external fun decodeURIComponent(encodedURI: String): String

val tokens = mutableStateMapOf<String, Token>()

fun main() {
  
  init { uid ->
    val searchParam = URLSearchParams(window.location.search).get("search")
    var isSearching by mutableStateOf(!searchParam.isNullOrBlank())
    val blocks = mutableStateListOf<Block>()
    val results = mutableStateListOf<Block>()

    scope.launch {
      initMinima(uid) { block -> blocks += block }
      createSQL()
      tokens.putAll(MDS.getTokens().associateBy { it.tokenId })
      populateBlocks(selectLatest(100), blocks)
    }

    renderComposable(rootElementId = "root") {
      console.log("isSearching: $isSearching")
      Search(searchParam, results) { isSearching = it }
      BlockList(if (isSearching) results else blocks)
    }
  }
}

fun init(block: (String?) -> Unit) {
  try {
    val uid = URLSearchParams(window.location.search).get("uid")
    window.addEventListener("load", {
      window.navigator.serviceWorker.getRegistrations().then{ registrations ->
        for(registration in registrations) {
          registration.unregister()
        }
      }
      window.navigator.serviceWorker.register("minidapp-blok.js?uid=$uid")
        .then { console.log("Service worker registered") }
        .catch { console.error("Service worker registration failed: $it") }
    })
    block.invoke(uid)
  } catch (t: Throwable) {
    service()
  }
}

suspend fun populateBlocks(sql: String, blocks: SnapshotStateList<Block>) {
  try {
    val sql = MDS.sql(sql)
    if (sql?.jsonBoolean("status") == true) {
      sql.jsonObject["rows"]?.let {
        blocks += it.jsonArray
          .map { it.jsonString("TXPOW") }
          .map(::decodeURIComponent)
          .map{ json.decodeFromString<JsonElement>(it) }
          .map(::mapTxPoW)
      } ?: throw Error("1. Fetching from sql failed.")
    } else {
      throw Error("2. Fetching from sql failed.")
    }
  } catch(err: Error) {
    log(err.toString())
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
  return Block(
    hash = txpow.jsonString("txpowid"),
    number = header.jsonString("block").toLong(),
    transactionCount = txpow.jsonObject["body"]!!.jsonObject["txnlist"]!!.jsonArray.size,
    timestamp = Instant.fromEpochMilliseconds(header.jsonString("timemilli").toLong()),
    size = txpow.jsonObject["size"]!!.jsonPrimitive.long,
    nonce = header.jsonString("nonce").toBigDecimal(),
    superBlockLevel = txpow.jsonObject["superblock"]!!.jsonPrimitive.int.toByte(),
    parentHash = header.jsonObject["superparents"]!!.jsonArray[0].jsonString("parent"),
    txpow
  )
}