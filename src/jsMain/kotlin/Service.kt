import minima.MDS
import minima.encodeURIComponent
import org.w3c.workers.ServiceWorkerGlobalScope

const val appName = "BloK"

const val txPoWMaxSize = 16000

fun createSQL() {
  MDS.sql("$INIT_SQL; $INDEX_HASH; $INDEX_HEIGHT") { sqlResult ->
    MDS.log(JSON.stringify(sqlResult))
    if (!sqlResult.status) {
      MDS.log("$appName : ERROR in SQL call!")
    }
  }
}

fun addTxPoW(txpow: dynamic) {
  val txPoWSize = txpow.size
  val txPoWHeight = txpow.header.block
  if (txpow.body == null) {
    MDS.log("txpow body not found!")
  } else {
//    txpow.body.witness.signatures = null
//    txpow.body.witness.mmrproofs = null
    val txpowEncoded = encodeURIComponent(JSON.stringify(txpow).replace("'", "%27"))
    if (txPoWSize > txPoWMaxSize) {
      MDS.log("$appName: Transaction at height: $txPoWHeight with size: $txPoWSize is too big for database column.")
    } else {
      val isBlock = if (txpow.isblock) 1 else 0
      MDS.sql(insertBlock(txpowEncoded, txPoWHeight, txpow.txpowid, isBlock, txpow.header.timemilli, txpow.body.txnlist.length)) {
        if (it.status) {
          MDS.log("$appName: timemilli ${txpow.header.timemilli}")
          MDS.log("TxPoW Added To SQL Table... ")
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
//  MDS.init{ msg: dynamic ->
//    if (msg.event == "inited") {
//
//      // init SQL DB for blocks
//      createSQL()
//
//    } else if (msg.event == "NEWBLOCK") {
//
//      addTxPoW(msg.data.txpow)
//
//    }
//  }
}