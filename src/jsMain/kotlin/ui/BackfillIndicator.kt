package ui

import androidx.compose.runtime.*
import blockStats
import getBlockStats
import getFlag
import kotlinx.coroutines.delay
import ltd.mbor.minimak.MDS
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Text
import kotlin.time.Duration.Companion.seconds

var isBackfilling by mutableStateOf<Boolean?>(null)

tailrec suspend fun checkIfBackfilling() {
  isBackfilling = MDS.getFlag("backfilling").also {
    if (isBackfilling == true) {
      blockStats.putAll(MDS.getBlockStats())
    }
  }
  if (isBackfilling == true) {
    delay(5.seconds)
    checkIfBackfilling()
  }
}

@Composable
fun BackfillIndicator() {
  LaunchedEffect("BackfillIndicator") {
    delay(3.seconds)
    checkIfBackfilling()
  }
  if (isBackfilling == true) {
    Spinner(20.px, 25.px, 25.px)
    Text("Backfilling blocks...")
  }
}
