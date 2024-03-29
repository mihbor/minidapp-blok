package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import getFlag
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ltd.mbor.minimak.MDS
import org.jetbrains.compose.web.dom.Text
import scope
import kotlin.time.Duration.Companion.seconds

var isBackfilling by mutableStateOf<Boolean?>(null)

tailrec suspend fun checkIfBackfilling() {
  isBackfilling = MDS.getFlag("backfilling")
  if (isBackfilling == true) {
    delay(5.seconds)
    checkIfBackfilling()
  }
}

@Composable
fun BackfillIndicator() {
  scope.launch {
    delay(1.seconds)
    checkIfBackfilling()
  }
  if (isBackfilling == true) {
    Text("Backfilling missed blocks")
  }
}
