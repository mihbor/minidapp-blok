package ui

import BlockStats
import androidx.compose.runtime.Composable
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import ltd.mbor.minimak.BurnStats
import org.jetbrains.compose.web.dom.Text

@Composable
fun Stats(chainWeight: BigDecimal?, blockStats: Map<Int, BlockStats>, burnStats: Map<String, BurnStats>) {
  chainWeight?.let { Text("Chain weight: ${it.toPlainString()}") }
  BlockStats(blockStats)
  BurnStats(burnStats)
  BackfillIndicator()
}
