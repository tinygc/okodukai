package com.tinygc.okodukai.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupMigrationManagerTest {

    @Test
    fun `同一バージョンはそのまま返す`() {
        val manager = BackupMigrationManager(currentVersion = 1)
        val raw = "{\"backupSchemaVersion\":1}"

        val migrated = manager.migrateToCurrent(raw, 1)

        assertEquals(raw, migrated)
    }

    @Test
    fun `段階マイグレーションを順次適用する`() {
        val manager = BackupMigrationManager(
            currentVersion = 3,
            migrationSteps = mapOf(
                1 to BackupMigrationStep { "{\"backupSchemaVersion\":2}" },
                2 to BackupMigrationStep { "{\"backupSchemaVersion\":3}" }
            )
        )

        val migrated = manager.migrateToCurrent("{\"backupSchemaVersion\":1}", 1)

        assertEquals("{\"backupSchemaVersion\":3}", migrated)
    }

    @Test
    fun `未来バージョンは失敗する`() {
        val manager = BackupMigrationManager(currentVersion = 1)

        val exception = runCatching {
            manager.migrateToCurrent("{}", 2)
        }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
    }
}
