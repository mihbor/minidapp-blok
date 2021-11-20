import minima.Minima
import minima.encodeURIComponent
import org.w3c.workers.ServiceWorkerGlobalScope

const val appName = "BloK"

const val txPoWMaxSize = 16000

fun createSQL() {
  Minima.file.load("createSql.txt") { fileResult ->
    if (!fileResult.success) {
      Minima.sql("$INIT_SQL; $INDEX_HASH; $INDEX_HEIGHT") { sqlResult ->
        Minima.log(JSON.stringify(sqlResult))
        if (!sqlResult.status) {
          Minima.log("$appName : ERROR in SQL call!")
        } else {
          Minima.file.save("", "createSql.txt")
        }
      }
    }
  }
}

fun addTxPoW(txpow: dynamic) {
  val txPoWSize = txpow.size
  val txPoWHeight = txpow.header.block
  if (txpow.body == null) {
    Minima.log("txpow body not found!")
  } else {
    txpow.body.witness.signatures = null
    txpow.body.witness.mmrproofs = null
    val txpowEncoded = encodeURIComponent(JSON.stringify(txpow).replace("'", "%27"))
    if (txPoWSize > txPoWMaxSize) {
      Minima.log("$appName: Transaction at height: $txPoWHeight with size: $txPoWSize is too big for database column.")
    } else {
      val isBlock = if (txpow.isblock) 1 else 0
      Minima.sql("INSERT INTO txpowlist VALUES (\'$txpowEncoded\', $txPoWHeight, \'${txpow.txpowid}\', $isBlock, ${txpow.header.timemilli}, ${txpow.body.txnlist.length})") {
        if (it.status) {
          Minima.log("$appName: timemilli ${txpow.header.timemilli}")
          Minima.log("TxPoW Added To SQL Table... ")
        }
      }
    }
  }
}

external val self: ServiceWorkerGlobalScope

fun service() {
  self.addEventListener("install", {
    console.log("Service worker installed")
  })
  self.addEventListener("activate", {
    console.log("Service worker activated")
    runMinima()
  })

}

fun runMinima() {
  Minima.debug = true
  Minima.init{ msg: dynamic ->
    if (msg.event == "connected") {

      // init SQL DB for blocks
      createSQL()

    } else if (msg.event == "newtxpow") {

      addTxPoW(msg.info.txpow)

    }
  }
}