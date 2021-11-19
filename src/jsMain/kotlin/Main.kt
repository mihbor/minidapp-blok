
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.browser.window
import kotlinx.datetime.Instant
import minima.Minima
import minima.decodeURIComponent
import org.jetbrains.compose.web.renderComposable

data class Block(
  val hash: String,
  val number: Long,
  val transactionCount: Int,
  val timestamp: Instant,
  val size: Long,
  val nonce: Long,
  val superBlockLevel: Byte,
  val parentHash: String
)

fun main() {
  init{
    val blocks = mutableStateListOf<Block>()

    populateBlocks(blocks)

    listenForBlocks { block -> blocks += block }

    renderComposable(rootElementId = "root") {
      BlockList(blocks)
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

fun populateBlocks(blocks: SnapshotStateList<Block>) {
  try {
    Minima.sql(SELECT_LAST_100) {
//      console.log("$appName : fetching all previous blocks saved on SQL.");
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

fun listenForBlocks(consumer: (Block) -> Unit) {

  Minima.debug = true
  Minima.logging = true
  Minima.init{
    val msg = it
    when(msg.event) {
      "connected" -> console.log("Connected to Minima.")
      "newtxpow" -> {
        val txpow = msg.info.txpow
//        console.log("txpow: ${JSON.stringify(txpow)}")
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
  parentHash = txpow.header.superparents[0].parent
)