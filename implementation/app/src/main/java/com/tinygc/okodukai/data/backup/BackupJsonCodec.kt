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
        return gson.fromJson(rawJson, BackupDocument::class.java)
    }

    fun readSchemaVersion(rawJson: String): Int {
        val normalized = rawJson.removePrefix("\uFEFF").trim()
        if (normalized.isBlank()) {
            throw IllegalArgumentException("バックアップファイルが空です")
        }

        val root: JsonObject = try {
            val element = JsonParser.parseString(normalized)
            if (!element.isJsonObject) {
                throw IllegalArgumentException("バックアップJSONの形式が不正です")
            }
            element.asJsonObject
        } catch (e: JsonSyntaxException) {
            throw IllegalArgumentException("バックアップJSONの形式が不正です", e)
        }

        val schemaElement = root.get("backupSchemaVersion")
        if (schemaElement != null) {
            if (!schemaElement.isJsonPrimitive || !schemaElement.asJsonPrimitive.isNumber) {
                throw IllegalArgumentException("backupSchemaVersion の値が不正です")
            }
            return schemaElement.asInt
        }

        // 旧バックアップ(v1)は backupSchemaVersion を持たないため互換として 1 扱いにする。
        val hasLegacyShape = root.get("payload")?.isJsonObject == true &&
            root.get("appDataVersion")?.isJsonPrimitive == true
        if (hasLegacyShape) return 1

        throw IllegalArgumentException("backupSchemaVersion が存在しません")
    }
}
