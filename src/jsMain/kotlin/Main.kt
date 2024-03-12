import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.Token
import ltd.mbor.minimak.getTokens
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.url.URLSearchParams
import ui.BlockList
import ui.Export
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
    val urlParams = URLSearchParams(window.location.search)
    val searchText = urlParams.get("searchText")
    val searchFrom = urlParams.get("searchFrom")
    val searchTo = urlParams.get("searchTo")
    var isSearching by mutableStateOf(!searchText.isNullOrBlank() || !searchFrom.isNullOrBlank() || !searchTo.isNullOrBlank())
    val blocks = mutableStateListOf<Block>()
    val results = mutableStateListOf<Block>()

    scope.launch {
      initMinima(uid) { block -> blocks.add(0, block) }
      MDS.createSQL()
      tokens.putAll(MDS.getTokens().associateBy { it.tokenId })
      populateBlocks(selectLatest(100), blocks)
    }

    renderComposable(rootElementId = "root") {
      console.log("isSearching: $isSearching")
      Search(searchText, searchFrom, searchTo, results) { isSearching = it }
      Export(if (isSearching) results else blocks)
      BlockList(if (isSearching) results else blocks)
    }
  }
}

fun init(block: (String?) -> Unit) {
  try {
    val uid = URLSearchParams(window.location.search).get("uid")
//    window.addEventListener("load", {
//      window.navigator.serviceWorker.getRegistrations().then{ registrations ->
//        for(registration in registrations) {
//          registration.unregister()
//        }
//      }
//      window.navigator.serviceWorker.register("minidapp-blok.js?uid=$uid")
//        .then { console.log("Service worker registered") }
//        .catch { console.error("Service worker registration failed: $it") }
//    })
    block.invoke(uid)
  } catch (t: Throwable) {
//    service()
  }
}
