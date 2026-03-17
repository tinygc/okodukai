package com.tinygc.okodukai.data.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
        // 呼び出し元で normalizeBackupJson() によるBOM除去・trim済みであること。
        return gson.fromJson(rawJson, BackupDocument::class.java)
            ?: throw IllegalArgumentException(BackupErrorMessages.DECODE_NULL)
    }

    fun readSchemaVersion(rawJson: String): Int {
        val normalized = rawJson.removePrefix("\uFEFF").trim()
        if (normalized.isBlank()) {
            throw IllegalArgumentException(BackupErrorMessages.FILE_EMPTY)
        }

        val root: JsonObject = try {
            val element = JsonParser.parseString(normalized)
            if (!element.isJsonObject) {
                throw IllegalArgumentException(BackupErrorMessages.JSON_MALFORMED)
            }
            element.asJsonObject
        } catch (e: JsonSyntaxException) {
            throw IllegalArgumentException(BackupErrorMessages.JSON_MALFORMED, e)
        }

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
}
