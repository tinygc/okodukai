package com.tinygc.okodukai.data.backup

/**
 * バックアップ処理で使用するエラーメッセージ定数。
 * Repository / Codec 側での throw と ViewModel 側での識別を
 * 文字列リテラルではなく定数で統一することで、サイレントデグレードを防ぐ。
 */
internal object BackupErrorMessages {
    const val FILE_NOT_FOUND = "バックアップファイルが見つかりません"
    const val FILE_EMPTY = "バックアップファイルが空です"
    const val JSON_MALFORMED = "バックアップJSONの形式が不正です"
    const val SCHEMA_KEY_MISSING = "backupSchemaVersion が存在しません"
    const val SCHEMA_VALUE_INVALID = "backupSchemaVersion の値が不正です"
    const val DECODE_NULL = "バックアップJSONの解析結果がnullです"
    const val DOCUMENT_INVALID = "バックアップ形式が不正です"
    const val CATEGORY_DATA_INVALID = "カテゴリデータが不正です"
    const val EXPENSE_DATA_INVALID = "支出データが不正です"
    const val POLICY_KEYS_MISSING = "backupPolicy に必須キーが不足しています"
    const val POLICY_VALUE_INVALID_PREFIX = "backupPolicy の値が不正です: "
    const val POLICY_EXCLUDED_UNSUPPORTED = "settings 以外の EXCLUDED は未対応です"
}
