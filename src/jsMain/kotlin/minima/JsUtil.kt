package minima

import com.ionspin.kotlin.bignum.serialization.kotlinx.bigdecimal.bigDecimalHumanReadableSerializerModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

external fun decodeURIComponent(encodedURI: String): String
external fun encodeURIComponent(string: String): String

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> jsObject(): T = js("({})")
inline fun <T : Any> jsObject(builder: T.() -> Unit): T = jsObject<T>().apply(builder)

val json = Json {
  ignoreUnknownKeys = true
  serializersModule = bigDecimalHumanReadableSerializerModule
}

fun JsonElement.jsonString(field: String) = this.jsonObject[field]?.jsonPrimitive?.content
