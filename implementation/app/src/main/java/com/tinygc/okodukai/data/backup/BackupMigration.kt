package com.tinygc.okodukai.data.backup

fun interface BackupMigrationStep {
    fun migrate(rawJson: String): String
}

class BackupMigrationManager(
    private val currentVersion: Int = BackupSchemas.CURRENT_SCHEMA_VERSION,
    private val migrationSteps: Map<Int, BackupMigrationStep> = emptyMap()
) {
    fun migrateToCurrent(rawJson: String, fromVersion: Int): String {
        if (fromVersion == currentVersion) return rawJson
        if (fromVersion > currentVersion) {
            throw IllegalArgumentException("このバックアップバージョンには未対応です")
        }

        var version = fromVersion
        var migrated = rawJson
        while (version < currentVersion) {
            val step = migrationSteps[version]
                ?: throw IllegalStateException("v$version からのマイグレーション定義がありません")
            migrated = step.migrate(migrated)
            version += 1
        }
        return migrated
    }
}
