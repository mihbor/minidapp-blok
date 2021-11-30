
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.browser.window
import kotlinx.datetime.Instant
import minima.Minima
import minima.decodeURIComponent
import org.jetbrains.compose.web.ExperimentalComposeWebSvgApi
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.svg.Circle
import org.jetbrains.compose.web.svg.Svg
import org.jetbrains.compose.web.svg.SvgText
import org.w3c.dom.url.URLSearchParams

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

fun main() {

  init{
    val searchParam = URLSearchParams(window.location.search).get("search")
    var isSearching by mutableStateOf(!searchParam.isNullOrBlank())
    val blocks = mutableStateListOf<Block>()
    val results = mutableStateListOf<Block>()

    initMinima { block -> blocks += block }

    populateBlocks(selectLatest(100), blocks)

    renderComposable(rootElementId = "root") {
      svg()
      console.log("isSearching: $isSearching")
      Search(searchParam, results) { isSearching = it }
      BlockList(if (isSearching) results else blocks)
    }
  }
}

@OptIn(ExperimentalComposeWebSvgApi::class)
@Composable
fun svg() {
  Div {
    Svg(viewBox = "0 0 200 3") {
      var currentColor by remember { mutableStateOf(0) }
      val colors = listOf(
        rgb(200, 0, 0),
        rgb(100, 0, 0),
        rgb(100, 20, 0),
        rgb(20, 100, 0)
      )
      Circle(2, 1.5, 1.5, {
        attr("stroke", "black")
        attr("stroke-width", "0.1")
        attr("fill", "white")
        onClick {
          currentColor = (currentColor + 1).mod(colors.size)
        }
      })
      SvgText("8", x = 1.5, y = 2.5) {
        attr("font-size", "2.5")
      }
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

fun populateBlocks(sql: String, blocks: SnapshotStateList<Block>) {
  try {
    Minima.sql(sql) {
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

fun initMinima(consumer: (Block) -> Unit) {

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
  parentHash = txpow.header.superparents[0].parent,
  txpow
)