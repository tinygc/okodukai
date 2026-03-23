package com.tinygc.okodukai.data.backup

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupMigrationDefinitionsTest {

    @Test
    fun `v1からv2へ移行時にpolicyとsettingsを補完する`() {
        val v1 = """
            {
              "backupSchemaVersion": 1,
              "appDataVersion": "4",
              "exportedAt": "2026-03-13T00:00:00",
              "payload": {
                "budgets": [],
                "expenses": [],
                "categories": [],
                "categoryOrders": [],
                "templates": [],
                "incomes": [],
                "savingGoals": []
              }
            }
        """.trimIndent()

        val migrated = BackupMigrationDefinitions.V1_TO_V2.migrate(v1)
        val root = JsonParser.parseString(migrated).asJsonObject

        assertEquals(2, root.get("backupSchemaVersion").asInt)
        assertTrue(root.getAsJsonObject("backupPolicy").has(BackupSchemas.KEY_SETTINGS))
        assertTrue(root.getAsJsonObject("payload").has("settings"))
    }

    @Test
    fun `v2からv3への移行で初期設定フラグを補完できること`() {
        val v2Json = """
            {
              "backupSchemaVersion": 2,
              "appDataVersion": "4",
              "exportedAt": "2026-03-17T00:00:00",
              "backupPolicy": {"settings": "INCLUDED"},
              "payload": {
                "settings": {
                  "defaultCategoryId": "c1",
                  "goalAchievementMode": "INDIVIDUAL"
                }
              }
            }
        """.trimIndent()

        val migrated = BackupMigrationDefinitions.V2_TO_V3.migrate(v2Json)
        val decoded = BackupJsonCodec().decode(migrated)

        assertEquals(3, decoded.backupSchemaVersion)
        assertFalse(decoded.payload.settings.hideInitialSetupAnnouncement)
        assertFalse(decoded.payload.settings.templateManagementVisited)
    }
}
