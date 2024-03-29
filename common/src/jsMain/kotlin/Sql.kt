import kotlinx.datetime.Instant
import kotlinx.serialization.json.jsonArray
import ltd.mbor.minimak.MdsApi
import ltd.mbor.minimak.jsonBoolean
import ltd.mbor.minimak.jsonObject
import ltd.mbor.minimak.jsonString

const val INIT_SQL = """
  CREATE TABLE IF NOT EXISTS txpowlist (
    txpow VARCHAR(64000),
    height BIGINT NOT NULL,
    hash VARCHAR(160) NOT NULL PRIMARY KEY,
    isblock INT NOT NULL,
    relayed BIGINT NOT NULL,
    txns INT NOT NULL
  );
  CREATE TABLE IF NOT EXISTS tx (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    block VARCHAR(255) NOT NULL,
    header VARCHAR(64000) NOT NULL,
    inputs VARCHAR(64000) NOT NULL,
    outputs VARCHAR(64000) NOT NULL,
    state VARCHAR(64000) NOT NULL,
    FOREIGN KEY (block) REFERENCES txpowlist(hash)
  );
  CREATE INDEX IF NOT EXISTS hash_idx ON txpowlist(hash);
  CREATE INDEX IF NOT EXISTS height_idx ON txpowlist(height DESC);
  CREATE TABLE IF NOT EXISTS flag (
    "key" VARCHAR(255) NOT NULL PRIMARY KEY,
    "value" BOOL
  );
"""

fun selectLatest(limit: Int = 100) = "SELECT * FROM txpowlist WHERE isblock = 1 ORDER BY HEIGHT DESC LIMIT $limit"

fun insertBlock(
  id: String,
  txpow: String?,
  height: Int,
  isBlock: Int,
  relayed: Long,
  length: Int
) = "INSERT INTO txpowlist VALUES (\'$txpow\', $height, '$id', $isBlock, $relayed, $length)"

fun selectBlockById(id: String) = "SELECT * FROM txpowlist WHERE hash = \'$id\'"

fun insertTransaction(
  id: String,
  block: String,
  header: String,
  inputs: String,
  outputs: String,
  state: String
) = "INSERT INTO tx VALUES (\'$id\', \'$block\', \'$header\', \'$inputs\', \'$outputs\', \'$state\')"

fun searchBlocks(query: String) =
  "SELECT * FROM txpowlist WHERE txpow LIKE '%$query%' ORDER BY RELAYED"

fun searchTransactions(query: String) =
  "SELECT * FROM tx WHERE id LIKE '%$query%' OR header LIKE '%$query%' OR inputs LIKE '%$query%' OR outputs LIKE '%$query%' OR state LIKE '%$query%' ORDER BY id"

fun searchBlocksAndTransactions(text: String?, fromDate: String?, toDate: String?): String {
  val sb = StringBuilder("SELECT * FROM txpowlist ")
  if (text != null || fromDate != null || toDate != null) sb.append("WHERE ")
  text?.let{ sb.append(textClause(it)) }
  fromDate?.let{
    if (text != null) sb.append(" AND ")
    sb.append("relayed >= ").append(Instant.parse("$it:00Z").toEpochMilliseconds())
  }
  toDate?.let{
    if (text != null || fromDate != null) sb.append(" AND ")
    sb.append("relayed <= ").append(Instant.parse("$it:59Z").toEpochMilliseconds())
  }
  sb.append(" ORDER BY RELAYED DESC")
  return sb.toString()
}

private fun textClause(text: String) = "txpow LIKE '%$text%' OR hash IN(${selectBlockFromTx(text)})"

fun selectBlockFromTx(query: String) =
  "SELECT block FROM tx WHERE id LIKE '%$query%' OR header LIKE '%$query%' OR inputs LIKE '%$query%' OR outputs LIKE '%$query%' OR state LIKE '%$query%'"

const val SECONDS_IN_HOUR = 60 * 60
fun selectStats(hoursBack: Int) =
  """SELECT count(*), sum(txns), min(relayed) FROM txpowlist WHERE relayed >= (EXTRACT (EPOCH from CURRENT_TIMESTAMP()) - ${hoursBack * SECONDS_IN_HOUR})*1000"""

fun setFlag(key: String, value: Boolean?) =
  """MERGE INTO flag KEY("key") VALUES ('$key', $value)"""

fun selectFlag(key: String) =
  """SELECT "value" FROM flag WHERE "key" = '$key'"""

suspend fun MdsApi.getFlag(key: String) = sql(selectFlag(key))
  ?.takeIf{ it.jsonBoolean("status") && it.jsonBoolean("results") }
  ?.let{
    it.jsonObject("rows").jsonArray.singleOrNull()?.jsonString("value")?.toBoolean()
  }
