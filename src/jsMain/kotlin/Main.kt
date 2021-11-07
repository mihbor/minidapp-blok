
import androidx.compose.runtime.mutableStateListOf
import kotlinx.datetime.Instant
import org.jetbrains.compose.web.renderComposable

data class Block(
  val number: Long,
  val hash: String,
  val transactionCount: Int,
  val timestamp: Instant,
  val size: Long,
  val nonce: Long,
  val superBlockLevel: Byte,
  val parentHash: String
)

fun main() {

  val blocks = mutableStateListOf<Block>()

  Minima.debug = true
  Minima.init{
    val msg = it
    when(msg.event) {
      "connected" -> console.log("Connected to Minima.")
      "newtxpow" -> {
        val txpow = msg.info.txpow
//        console.log("txpow: ${JSON.stringify(txpow)}")
        console.log("isBlock: ${txpow.isblock}")
        if (txpow.isblock) {
          blocks += Block(
            number = (txpow.header.block as String).toLong(),
            hash = txpow.txpowid,
            transactionCount = txpow.body.txnlist.length,
            timestamp = Instant.fromEpochMilliseconds((txpow.header.timemilli as String).toLong()),
            size = txpow.size,
            nonce = (txpow.header.nonce as String).toLong(),
            superBlockLevel = txpow.superblock,
            parentHash = txpow.header.superparents[0].parent
          )
        }
      }
    }
  }

  renderComposable(rootElementId = "root") {
    BlockList(blocks)
  }
}