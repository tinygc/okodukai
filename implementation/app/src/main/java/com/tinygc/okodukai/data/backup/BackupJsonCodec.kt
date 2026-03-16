package com.tinygc.okodukai.data.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser

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
        val root: JsonObject = JsonParser.parseString(normalized).asJsonObject
        root.get("backupSchemaVersion")?.asInt?.let { return it }

        // 旧バックアップ(v1)は backupSchemaVersion を持たないため互換として 1 扱いにする。
        val hasLegacyShape = root.has("payload") && root.has("appDataVersion")
        if (hasLegacyShape) return 1

        throw IllegalArgumentException("backupSchemaVersion が存在しません")
    }
}
