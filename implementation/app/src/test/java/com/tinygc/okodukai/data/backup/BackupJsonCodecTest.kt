package com.tinygc.okodukai.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupJsonCodecTest {

    private val codec = BackupJsonCodec()

    @Test
    fun `未知フィールドを含んでもデコードできる`() {
        val json = """
            {
              "backupSchemaVersion": 1,
              "appDataVersion": "4",
              "exportedAt": "2026-03-13T00:00:00",
              "backupPolicy": {"budgets":"INCLUDED"},
              "payload": {"budgets":[]},
              "futureField": "ignored"
            }
        """.trimIndent()

        val decoded = codec.decode(json)

        assertEquals(1, decoded.backupSchemaVersion)
        assertEquals("4", decoded.appDataVersion)
    }

    @Test
    fun `schemaVersionを読み取れる`() {
        val json = "{\"backupSchemaVersion\":1}"

        val version = codec.readSchemaVersion(json)

        assertEquals(1, version)
    }

    @Test
    fun `旧形式でschemaVersion欠落時はv1として扱う`() {
        val legacyJson = """
            {
              "appDataVersion": "4",
              "payload": {"budgets": []}
            }
        """.trimIndent()

        val version = codec.readSchemaVersion(legacyJson)

        assertEquals(1, version)
    }

    @Test
    fun `空文字は読み取り失敗になる`() {
        val exception = runCatching {
            codec.readSchemaVersion("   ")
        }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
        assertEquals(BackupErrorMessages.FILE_EMPTY, exception?.message)
    }

    @Test
    fun `不正JSONは形式不正として失敗する`() {
        val exception = runCatching {
            codec.readSchemaVersion("not-json")
        }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
        assertEquals(BackupErrorMessages.JSON_MALFORMED, exception?.message)
    }

    @Test
    fun `schemaVersionが数値以外なら失敗する`() {
        val exception = runCatching {
            codec.readSchemaVersion("{\"backupSchemaVersion\":\"x\"}")
        }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
        assertEquals(BackupErrorMessages.SCHEMA_VALUE_INVALID, exception?.message)
    }

    @Test
    fun `BOM付きJSONでもschemaVersionを読み取れる`() {
        val json = "\uFEFF{\"backupSchemaVersion\":2}"

        val version = codec.readSchemaVersion(json)

        assertEquals(2, version)
    }

    @Test
    fun `schemaKeyもlegacy形式もない場合はSCHEMA_KEY_MISSINGエラー`() {
        val exception = runCatching {
            codec.readSchemaVersion("{\"someOtherKey\":1}")
        }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
        assertEquals(BackupErrorMessages.SCHEMA_KEY_MISSING, exception?.message)
    }

    @Test
    fun `encodeしてdecodeすると同一データが復元できる`() {
        val original = BackupDocument(
            backupSchemaVersion = 2,
            appDataVersion = "4",
            exportedAt = "2026-03-17T00:00:00",
            backupPolicy = mapOf("budgets" to "INCLUDED"),
            payload = BackupPayload()
        )

        val decoded = codec.decode(codec.encode(original))

        assertEquals(original.backupSchemaVersion, decoded.backupSchemaVersion)
        assertEquals(original.appDataVersion, decoded.appDataVersion)
        assertEquals(original.exportedAt, decoded.exportedAt)
        assertEquals(original.backupPolicy, decoded.backupPolicy)
    }

    @Test
    fun `decode_nullはDECODE_NULLエラーを投げる`() {
        val exception = runCatching {
            codec.decode("null")
        }.exceptionOrNull()

        assertTrue(exception is IllegalArgumentException)
        assertEquals(BackupErrorMessages.DECODE_NULL, exception?.message)
    }
}
