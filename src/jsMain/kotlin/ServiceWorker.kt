import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.MDS
import ltd.mbor.minimak.jsonString
import org.w3c.dom.url.URLSearchParams
import org.w3c.workers.ServiceWorkerGlobalScope

external val self: ServiceWorkerGlobalScope

fun service() {
  self.addEventListener("install", {
    console.log("Service worker installed")
  })
  self.addEventListener("activate", {
    console.log("Service worker activated")
    scope.launch {
      runMinima()
    }
  })
}

suspend fun runMinima() {
  val uid = URLSearchParams(self.location.search).get("uid")
  MDS.init(uid ?: "0x0", "localhost", 9003) { msg ->
    if (msg.jsonString("event") == "inited") {
      MDS.createSQL()
    } else if (msg.jsonString("event") == "NEWBLOCK") {
      MDS.addTxPoW(msg.jsonObject["data"]!!.jsonObject["txpow"]!!)
    }
  }
}
