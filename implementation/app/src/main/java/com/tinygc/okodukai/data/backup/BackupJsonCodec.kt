package com.tinygc.okodukai.data.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException

class BackupJsonCodec(
    private val gson: Gson = GsonBuilder().serializeNulls().create()
) {
    fun encode(document: BackupDocument): String {
        return gson.toJson(document)
    }

    fun decode(rawJson: String): BackupDocument {
        val normalized = rawJson.removePrefix("\uFEFF").trim()
        if (normalized.isBlank()) {
            throw IllegalArgumentException(BackupErrorMessages.FILE_EMPTY)
        }

        val root = parseRootObject(normalized)
        sanitizeForDecode(root)

        return try {
            gson.fromJson(root, BackupDocument::class.java)
                ?: throw IllegalArgumentException(BackupErrorMessages.DECODE_NULL)
        } catch (e: JsonSyntaxException) {
            throw IllegalArgumentException(BackupErrorMessages.JSON_MALFORMED, e)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(BackupErrorMessages.DOCUMENT_INVALID, e)
        }
    }

    fun readSchemaVersion(rawJson: String): Int {
        val normalized = rawJson.removePrefix("\uFEFF").trim()
        if (normalized.isBlank()) {
            throw IllegalArgumentException(BackupErrorMessages.FILE_EMPTY)
        }

        val root = parseRootObject(normalized)

        val schemaElement = root.get("backupSchemaVersion")
        if (schemaElement != null) {
            if (!schemaElement.isJsonPrimitive || !schemaElement.asJsonPrimitive.isNumber) {
                throw IllegalArgumentException(BackupErrorMessages.SCHEMA_VALUE_INVALID)
            }
            return schemaElement.asInt
        }

        // 旧バックアップ(v1)は backupSchemaVersion を持たないため互換として 1 扱いにする。
        val hasLegacyShape = root.get("payload")?.isJsonObject == true &&
            root.get("appDataVersion")?.isJsonPrimitive == true
        if (hasLegacyShape) return 1

        throw IllegalArgumentException(BackupErrorMessages.SCHEMA_KEY_MISSING)
    }

    private fun parseRootObject(normalized: String): JsonObject {
        return try {
            val element = JsonParser.parseString(normalized)
            if (element.isJsonNull) {
                throw IllegalArgumentException(BackupErrorMessages.DECODE_NULL)
            }
            if (!element.isJsonObject) {
                throw IllegalArgumentException(BackupErrorMessages.JSON_MALFORMED)
            }
            element.asJsonObject
        } catch (e: JsonSyntaxException) {
            throw IllegalArgumentException(BackupErrorMessages.JSON_MALFORMED, e)
        }
    }

    private fun sanitizeForDecode(root: JsonObject) {
        val schemaElement = root.get("backupSchemaVersion")
        if (schemaElement == null || !schemaElement.isJsonPrimitive || !schemaElement.asJsonPrimitive.isNumber) {
            throw IllegalArgumentException(BackupErrorMessages.DOCUMENT_INVALID)
        }

        if (!root.has("appDataVersion") || root.get("appDataVersion").isJsonNull) {
            root.addProperty("appDataVersion", "")
        }
        if (!root.has("exportedAt") || root.get("exportedAt").isJsonNull) {
            root.addProperty("exportedAt", "")
        }

        if (!root.has("backupPolicy") || root.get("backupPolicy").isJsonNull) {
            root.add("backupPolicy", JsonObject())
        } else if (!root.get("backupPolicy").isJsonObject) {
            throw IllegalArgumentException(BackupErrorMessages.DOCUMENT_INVALID)
        }

        val payload = ensureObject(root, "payload")
        ensureArray(payload, BackupSchemas.KEY_BUDGETS)
        ensureArray(payload, BackupSchemas.KEY_EXPENSES)
        ensureArray(payload, BackupSchemas.KEY_CATEGORIES)
        ensureArray(payload, BackupSchemas.KEY_CATEGORY_ORDERS)
        ensureArray(payload, BackupSchemas.KEY_TEMPLATES)
        ensureArray(payload, BackupSchemas.KEY_INCOMES)
        ensureArray(payload, BackupSchemas.KEY_SAVING_GOALS)

        val settings = ensureObject(payload, BackupSchemas.KEY_SETTINGS)
        if (!settings.has("defaultCategoryId")) {
            settings.add("defaultCategoryId", JsonNull.INSTANCE)
        }
        if (!settings.has("goalAchievementMode") || settings.get("goalAchievementMode").isJsonNull) {
            settings.addProperty("goalAchievementMode", BackupSchemas.DEFAULT_GOAL_ACHIEVEMENT_MODE)
        }
        if (!settings.has("monthStartDay") || settings.get("monthStartDay").isJsonNull) {
            settings.addProperty("monthStartDay", 1)
        }
    }

    private fun ensureObject(parent: JsonObject, key: String): JsonObject {
        if (!parent.has(key) || parent.get(key).isJsonNull) {
            return JsonObject().also { parent.add(key, it) }
        }
        val value = parent.get(key)
        if (!value.isJsonObject) {
            throw IllegalArgumentException(BackupErrorMessages.DOCUMENT_INVALID)
        }
        return value.asJsonObject
    }

    private fun ensureArray(parent: JsonObject, key: String) {
        if (!parent.has(key) || parent.get(key).isJsonNull) {
            parent.add(key, JsonArray())
            return
        }
        if (!parent.get(key).isJsonArray) {
            throw IllegalArgumentException(BackupErrorMessages.DOCUMENT_INVALID)
        }
    }
}
