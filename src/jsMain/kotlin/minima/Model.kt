@file:UseContextualSerialization(BigDecimal::class)
@file:OptIn(ExperimentalSerializationApi::class)

package minima

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.json.*

@Serializable
data class Balance(
  val tokenid: String,
  @JsonNames("token")
  val _token: JsonElement,
//  val total: String,
  val confirmed: BigDecimal,
  val unconfirmed: BigDecimal,
  val sendable: BigDecimal,
  val coins: Int
) {
  val tokenName get() = if (_token is JsonPrimitive) _token.jsonPrimitive.content else _token.jsonString("name")
  val tokenUrl get() = _token.jsonString("url")
}

@Serializable
data class Token(
  val tokenid: String,
  @JsonNames("name")
  val _name: JsonElement,
  val total: BigDecimal,
  val decimals: Int,
  val script: String? = null,
  val coinid: String? = null,
  val totalamount: BigDecimal? = null,
  @JsonNames("scale")
  val _scale: JsonPrimitive
) {
  val name get() = if(_name is JsonPrimitive) _name.jsonPrimitive.content else _name.jsonString("name")
  val url get() = _name.jsonString("name")
  val scale get() = if (_scale.isString) _scale.content.toInt() else _scale.int
}

@Serializable
data class Coin(
  val address: String,
  val miniaddress: String,
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
