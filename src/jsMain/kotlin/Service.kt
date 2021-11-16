import org.w3c.workers.ServiceWorkerGlobalScope

const val appName = "BloK"

const val INIT_SQL = """
  CREATE TABLE IF NOT EXISTS txpowlist (
    txpow VARCHAR(16000) NOT NULL PRIMARY KEY,
    height BIGINT NOT NULL,
    hash VARCHAR(160) NOT NULL,
    isblock INT NOT NULL,
    relayed BIGINT NOT NULL,
    txns INT NOT NULL
  )"""

const val INDEX_HASH = "CREATE INDEX hash_idx ON txpowlist(hash)"
const val INDEX_HEIGHT = "CREATE INDEX height_idx ON txpowlist(height DESC)"

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

external val self: ServiceWorkerGlobalScope

@JsExport
fun service() {
  self.addEventListener("install", {
    console.log("Service worker installed")
  })
  self.addEventListener("activate", {
    console.log("Service worker activated")
    minima()
  })

}

fun minima() {
  Minima.debug = true
  Minima.init{ msg: dynamic ->
    if (msg.event == "connected") {

      // init SQL DB for blocks
      createSQL()

    } else if (msg.event == "newtxpow") {

//      addTxPoW(msg.info.txpow)
//
//      pruneData(msg.info.txpow.header.block)

    }
  }
}