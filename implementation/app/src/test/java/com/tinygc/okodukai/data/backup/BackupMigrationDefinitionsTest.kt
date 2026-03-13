package com.tinygc.okodukai.data.backup

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
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
}
