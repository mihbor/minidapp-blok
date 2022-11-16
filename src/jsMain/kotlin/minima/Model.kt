@file:UseContextualSerialization(BigDecimal::class)
@file:OptIn(ExperimentalSerializationApi::class)

package minima

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int

@Serializable
data class Balance(
  val tokenid: String,
  val token: JsonElement,
//  val total: String,
  val confirmed: BigDecimal,
  val unconfirmed: BigDecimal,
  val sendable: BigDecimal,
  val coins: Int
)

@Serializable
data class Token(
  val tokenid: String,
  val name: JsonElement,
  val total: BigDecimal,
  val decimals: Int,
  val script: String? = null,
  val coinid: String? = null,
  val totalamount: BigDecimal? = null,
  @JsonNames("scale")
  val _scale: JsonPrimitive
) {
  val scale get() = if (_scale.isString) _scale.content.toInt() else _scale.int
}

@Serializable
data class Coin(
  val address: String,
  val amount: BigDecimal,
  val tokenamount: BigDecimal = amount,
  val coinid: String,
  val storestate: Boolean,
  val tokenid: String,
  val created: String,
  val state: List<State>
)

@Serializable
data class State(
  val port: Int,
  val type: Int,
  val data: String
)
