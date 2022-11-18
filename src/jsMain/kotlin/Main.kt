
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.serialization.kotlinx.bigdecimal.bigDecimalHumanReadableSerializerModule
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import minima.MDS
import minima.Token
import minima.decodeURIComponent
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
  val txpow: dynamic
)

val scope = MainScope()

val json = Json {
  serializersModule = bigDecimalHumanReadableSerializerModule
}

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
      tokens.putAll(getTokens().associateBy { it.tokenid })
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

suspend fun getTokens(): Array<Token> {
  val tokens = MDS.cmd("tokens")
  return json.decodeFromDynamic(tokens.response)
}

fun populateBlocks(sql: String, blocks: SnapshotStateList<Block>) {
  try {
    MDS.sql(sql) {
      if (it.status) {
        if (it.rows != null) {
          blocks += (it.rows as Array<dynamic>)
            .map { it.TXPOW }
            .map(::decodeURIComponent)
            .map{ JSON.parse<dynamic>(it) }
            .map(::mapTxPoW)
        } else {
          throw Error("1. Fetching from sql failed.")
        }
      } else {
        throw Error("2. Fetching from sql failed.")
      }
    }
  } catch(err: Error) {
    MDS.log(err.toString())
  }
}

suspend fun initMinima(uid: String?, consumer: (Block) -> Unit) {
  
  MDS.init(uid ?: "0x0", window.location.hostname, 9003){
    val msg = it
    when(msg.event) {
      "inited" -> console.log("Connected to Minima.")
      "NEWBLOCK" -> {
        val txpow = msg.data.txpow
        console.log("isBlock: ${txpow.isblock}")
        if (txpow.isblock) {
          consumer.invoke(mapTxPoW(txpow))
        }
      }
    }
  }
}

fun mapTxPoW(txpow: dynamic) = Block(
  hash = txpow.txpowid,
  number = (txpow.header.block as String).toLong(),
  transactionCount = txpow.body.txnlist.length,
  timestamp = Instant.fromEpochMilliseconds((txpow.header.timemilli as String).toLong()),
  size = txpow.size,
  nonce = (txpow.header.nonce as String).toBigDecimal(),
  superBlockLevel = txpow.superblock,
  parentHash = txpow.header.superparents[0].parent,
  txpow
)