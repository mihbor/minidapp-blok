
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.ionspin.kotlin.bignum.serialization.kotlinx.bigdecimal.bigDecimalHumanReadableSerializerModule
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import minima.Minima
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
  val nonce: Long,
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

  init {
    val searchParam = URLSearchParams(window.location.search).get("search")
    var isSearching by mutableStateOf(!searchParam.isNullOrBlank())
    val blocks = mutableStateListOf<Block>()
    val results = mutableStateListOf<Block>()

    initMinima { block -> blocks += block }
    scope.launch {
      tokens.putAll(getTokens().associateBy { it.tokenid })
    }

    populateBlocks(selectLatest(100), blocks)

    renderComposable(rootElementId = "root") {
      console.log("isSearching: $isSearching")
      Search(searchParam, results) { isSearching = it }
      BlockList(if (isSearching) results else blocks)
    }
  }
}

fun init(block: () -> Unit) {
  try {
    window.addEventListener("load", {
      window.navigator.serviceWorker.getRegistrations().then{ registrations ->
        for(registration in registrations) {
          registration.unregister()
        }
      }
      window.navigator.serviceWorker.register("minidapp-blockexplorer-kotlin-compose.js")
        .then { console.log("Service worker registered") }
        .catch { console.error("Service worker registration failed: $it") }
    })
    block.invoke()
  } catch (t: Throwable) {
    service()
  }
}

suspend fun getTokens(): Array<Token> {
  val tokens = Minima.cmd("tokens")
  return json.decodeFromDynamic(tokens.response.tokens)
}

fun populateBlocks(sql: String, blocks: SnapshotStateList<Block>) {
  try {
    Minima.sql(sql) {
      if (it.status) {
        if (it.response.status && it.response.rows != null) {
          blocks += (it.response.rows as Array<dynamic>)
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
    Minima.log(err)
  }
}

fun initMinima(consumer: (Block) -> Unit) {

  Minima.debug = true
  Minima.logging = true
  Minima.init{
    val msg = it
    when(msg.event) {
      "connected" -> console.log("Connected to Minima.")
      "newtxpow" -> {
        val txpow = msg.info.txpow
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
  nonce = (txpow.header.nonce as String).toLong(),
  superBlockLevel = txpow.superblock,
  parentHash = txpow.header.superparents[0].parent,
  txpow
)