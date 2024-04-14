import androidx.compose.runtime.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement
import ltd.mbor.minimak.*
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.PopStateEvent
import org.w3c.dom.url.URL
import org.w3c.dom.url.URLSearchParams
import ui.*

const val RESULT_LIMIT = 100

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
val burn = mutableStateMapOf<String, BurnStats>()
val blockStats = mutableStateMapOf<Int, BlockStats>()
var status by mutableStateOf<Status?>(null)

val txnCache = mutableStateMapOf<String, JsonElement?>()

fun main() {
  
  init { uid ->
    val urlParams = URLSearchParams(window.location.search)
    var searchText by mutableStateOf(urlParams.get("searchText"))
    var searchFrom by mutableStateOf(urlParams.get("searchFrom"))
    var searchTo by mutableStateOf(urlParams.get("searchTo"))
    var limit by mutableStateOf(urlParams.get("limit")?.toInt() ?: RESULT_LIMIT)
    var page by mutableStateOf(urlParams.get("page")?.toInt() ?: 0)
    var isSearchingOrPaging by mutableStateOf(!searchText.isNullOrBlank() || !searchFrom.isNullOrBlank() || !searchTo.isNullOrBlank() || page != 0)
    val blocks = mutableStateListOf<Block>()
    val results = mutableStateListOf<Block>()

    fun updateResults(search: String?, fromDate: String?, toDate: String?, newPage: Int) {
      page = newPage
      scope.launch {
        results.clear()
        results.addAll(populateBlocks(searchBlocksAndTransactions(search, fromDate, toDate, limit, page * limit)))
        isSearchingOrPaging = true
      }
    }

    fun setPage(newPage: Int) {
      setUrlParams(searchText?.takeUnless { it.isBlank() }, searchFrom?.takeUnless { it.isBlank() }, searchTo?.takeUnless { it.isBlank() }, newPage)
      updateResults(searchText, searchFrom, searchTo, newPage)
    }

    scope.launch {
      initMinima(uid) { block -> blocks.add(0, block) }
      MDS.createSQL()
      tokens.putAll(MDS.getTokens().associateBy { it.tokenId })
      blocks.addAll(populateBlocks(selectLatest(RESULT_LIMIT)))
      if (isSearchingOrPaging) results.addAll(populateBlocks(searchBlocksAndTransactions(searchText, searchFrom, searchTo, limit, page * limit)))
      status = MDS.getStatus()
      burn.putAll(MDS.burn())
      blockStats.putAll(MDS.getBlockStats())
    }

    window.addEventListener("popstate", {
      val event = it as PopStateEvent
      if(event.state == null) {
        searchText = ""
        searchFrom = ""
        searchTo = ""
        page = 0
        limit = RESULT_LIMIT
        isSearchingOrPaging = false
      } else {
        it.state.toString().split(";").let {
          searchText = it[0]
          searchFrom = it[1]
          searchTo = it[2]
          page = it[3].toInt()
          limit = it[4].toIntOrNull() ?: RESULT_LIMIT
        }
      }
    })

    renderComposable(rootElementId = "root") {
      console.log("isSearching: $isSearchingOrPaging")
      Stats(status, blockStats, burn)
      Search(searchText, searchFrom, searchTo, ::updateResults)
      Export(if (isSearchingOrPaging) results else blocks)
      Paginator(page, limit, results.size, ::setPage)
      BlockList(if (isSearchingOrPaging) results else blocks)
      Paginator(page, limit, results.size, ::setPage)
    }
  }
}

fun setUrlParams(search: String?, fromDate: String?, toDate: String?, page: Int = 0, limit: Int = RESULT_LIMIT) {
  val url = URL(window.location.href)
  if (search != null) url.searchParams.set("searchText", search)
  else url.searchParams.delete("searchText")
  if (fromDate != null) url.searchParams.set("searchFrom", fromDate)
  else url.searchParams.delete("searchFrom")
  if (toDate != null) url.searchParams.set("searchTo", toDate)
  else url.searchParams.delete("searchTo")
  url.searchParams.set("limit", limit.toString())
  url.searchParams.set("page", page.toString())
  window.history.pushState(listOf(search, fromDate, toDate, page, limit).map { it ?: "" }.joinToString(";"), "", url.toString())
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
