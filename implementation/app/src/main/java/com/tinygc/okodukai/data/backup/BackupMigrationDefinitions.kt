package com.tinygc.okodukai.data.backup

import com.google.gson.JsonParser

object BackupMigrationDefinitions {

    val V1_TO_V2 = BackupMigrationStep { rawJson ->
        val root = JsonParser.parseString(rawJson).asJsonObject

        val backupPolicy = root.getAsJsonObject("backupPolicy")
            ?: defaultPolicyJson()
        root.add("backupPolicy", backupPolicy)

        val payload = root.getAsJsonObject("payload")
            ?: throw IllegalArgumentException("payload が存在しません")
        if (!payload.has("settings")) {
            val settings = JsonParser.parseString(
                "{\"defaultCategoryId\":null,\"goalAchievementMode\":\"INDIVIDUAL\"}"
            ).asJsonObject
            payload.add("settings", settings)
        }

        root.addProperty("backupSchemaVersion", 2)
        root.toString()
    }

        private fun defaultPolicyJson() =
        JsonParser.parseString(
            """
            {
                            "${BackupSchemas.KEY_BUDGETS}": "${BackupSchemas.POLICY_INCLUDED}",
                            "${BackupSchemas.KEY_EXPENSES}": "${BackupSchemas.POLICY_INCLUDED}",
                            "${BackupSchemas.KEY_CATEGORIES}": "${BackupSchemas.POLICY_INCLUDED}",
                            "${BackupSchemas.KEY_CATEGORY_ORDERS}": "${BackupSchemas.POLICY_INCLUDED}",
                            "${BackupSchemas.KEY_TEMPLATES}": "${BackupSchemas.POLICY_INCLUDED}",
                            "${BackupSchemas.KEY_INCOMES}": "${BackupSchemas.POLICY_INCLUDED}",
                            "${BackupSchemas.KEY_SAVING_GOALS}": "${BackupSchemas.POLICY_INCLUDED}",
                            "${BackupSchemas.KEY_SETTINGS}": "${BackupSchemas.POLICY_INCLUDED}"
            }
            """.trimIndent()
        ).asJsonObject
}
