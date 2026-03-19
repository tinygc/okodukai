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
            throw IllegalArgumentException(BackupErrorMessages.UNSUPPORTED_SCHEMA_VERSION)
        }

        var version = fromVersion
        var migrated = rawJson
        while (version < currentVersion) {
            val step = migrationSteps[version]
                ?: throw IllegalStateException("${BackupErrorMessages.MIGRATION_DEFINITION_MISSING_PREFIX}: v$version")
            migrated = step.migrate(migrated)
            version += 1
        }
        return migrated
    }
}
