
const val INIT_SQL = """
  CREATE TABLE IF NOT EXISTS txpowlist (
    txpow VARCHAR(64000) NOT NULL PRIMARY KEY,
    height BIGINT NOT NULL,
    hash VARCHAR(160) NOT NULL,
    isblock INT NOT NULL,
    relayed BIGINT NOT NULL,
    txns INT NOT NULL
  )"""

const val INDEX_HASH = "CREATE INDEX hash_idx ON txpowlist(hash)"
const val INDEX_HEIGHT = "CREATE INDEX height_idx ON txpowlist(height DESC)"

fun selectLatest(limit: Int = 100) = "SELECT * from txpowlist WHERE isblock = 1 ORDER BY HEIGHT DESC LIMIT $limit"

fun insertBlock(
  txpowEncoded: String,
  height: Int,
  txpowId: String,
  isBlock: Int,
  relayed: Long,
  length: Int
) = "INSERT INTO txpowlist VALUES (\'$txpowEncoded\', $height, \'$txpowId\', $isBlock, $relayed, $length)"

fun search(query: String) = "SELECT * FROM txpowlist WHERE TXPOW LIKE '%$query%' ORDER BY RELAYED"