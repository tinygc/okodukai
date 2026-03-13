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
        val root: JsonObject = JsonParser.parseString(rawJson).asJsonObject
        return root.get("backupSchemaVersion")?.asInt
            ?: throw IllegalArgumentException("backupSchemaVersion が存在しません")
    }
}
