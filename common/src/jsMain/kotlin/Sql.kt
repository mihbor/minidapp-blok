
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
"""

fun selectLatest(limit: Int = 100) = "SELECT * from txpowlist WHERE isblock = 1 ORDER BY HEIGHT DESC LIMIT $limit"

fun insertBlockSql(
  txpow: String?,
  height: Int,
  txpowId: String,
  isBlock: Int,
  relayed: Long,
  length: Int
) = "INSERT INTO txpowlist VALUES (\'$txpow\', $height, '$txpowId', $isBlock, $relayed, $length)"

fun insertTransactionSql(
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

fun searchBlocksAndTransactions(query: String) = """
  SELECT * FROM txpowlist WHERE txpow LIKE '%$query%' OR hash IN(
    SELECT block FROM tx WHERE id LIKE '%$query%' OR header LIKE '%$query%' OR inputs LIKE '%$query%' OR outputs LIKE '%$query%' OR state LIKE '%$query%'
  ) ORDER BY RELAYED"""
