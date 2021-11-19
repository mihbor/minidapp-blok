
const val INIT_SQL = """
  CREATE TABLE IF NOT EXISTS txpowlist (
    txpow VARCHAR(16000) NOT NULL PRIMARY KEY,
    height BIGINT NOT NULL,
    hash VARCHAR(160) NOT NULL,
    isblock INT NOT NULL,
    relayed BIGINT NOT NULL,
    txns INT NOT NULL
  )"""

const val INDEX_HASH = "CREATE INDEX hash_idx ON txpowlist(hash)"
const val INDEX_HEIGHT = "CREATE INDEX height_idx ON txpowlist(height DESC)"

const val SELECT_LAST_100 = "SELECT * from txpowlist WHERE isblock = 1 ORDER BY HEIGHT DESC LIMIT 100"