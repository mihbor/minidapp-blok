
import androidx.compose.runtime.mutableStateListOf
import kotlinx.datetime.Instant
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

data class Block(val height: Long, val hash: String, val transactionCount: Int, val timeRelayed: Instant)

fun main() {

  val blocks = mutableStateListOf<Block>()
  renderComposable(rootElementId = "root") {
    Minima.debug = true
    Minima.init{
      val msg = it
      when(msg.event) {
        "connected" -> console.log("Connected to Minima.")
        "newtxpow" -> {
          val txpow = msg.info.txpow
//          console.log("txpow: ${JSON.stringify(txpow)}")
          console.log("isBlock: ${txpow.isblock}")
          if (txpow.isblock) {
            blocks += Block(
              height = txpow.header.block,
              hash = txpow.txpowid,
              transactionCount = txpow.body.txnlist.length,
              timeRelayed = Instant.fromEpochMilliseconds((txpow.header.timemilli as String).toLong())
            )
          }
        }
      }
    }
    Table {
      Thead {
        Tr {
          Td { Text("Height") }
          Td { Text("Hash") }
          Td { Text("Txns") }
          Td { Text("Time relayed") }
        }
      }
      Tbody {
        blocks.map {
          Tr {
            Td { Text(it.height.toString()) }
            Td { Text(it.hash) }
            Td { Text(it.transactionCount.toString()) }
            Td { Text(it.timeRelayed.toString()) }
          }
        }
      }
    }
  }
}