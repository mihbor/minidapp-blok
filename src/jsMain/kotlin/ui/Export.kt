package ui

import Block
import androidx.compose.runtime.Composable
import kotlinx.browser.document
import kotlinx.serialization.encodeToString
import ltd.mbor.minimak.json
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.File
import org.w3c.files.FilePropertyBag

@Composable
fun Export(blocks: List<Block>) {
  Button({
    onClick {
      val csv = StringBuilder().appendLine("Height,Hash,Transaction Count,Time relayed,JSON")
      blocks.forEach {
        val data = listOf(
          it.number,
          it.hash,
          it.transactionCount,
          it.timestamp,
          json.encodeToString(it.txpow).replace("\n", "").replace("\"", "\"\"")
        )
        csv.appendLine(data.joinToString("\",\"", "\"", "\""))
      }
      val file = File(csv.toString().toCharArray().toTypedArray(), "minima-blocks.csv", FilePropertyBag(type = "text/csv"))
      val link = document.createElement("a") as HTMLAnchorElement
      val url = URL.createObjectURL(file)
      link.href = url
      link.download = file.name
      link.click()
      URL.revokeObjectURL(url)
    }
  }) {
    Text("CSV")
  }
}
