package com.tinygc.okodukai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tinygc.okodukai.data.local.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * テンプレートデータアクセスオブジェクト
 */
@Dao
interface TemplateDao {

    /**
     * テンプレートを追加
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: TemplateEntity)

    /**
     * テンプレートを更新
     */
    @Update
    suspend fun update(template: TemplateEntity)

    /**
     * テンプレートを削除
     */
    @Delete
    suspend fun delete(template: TemplateEntity)

    /**
     * IDでテンプレートを取得
     */
    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getById(id: String): TemplateEntity?

    /**
     * 全テンプレートを取得
     */
    @Query("SELECT * FROM templates ORDER BY display_order ASC, created_at ASC")
    fun getAllFlow(): Flow<List<TemplateEntity>>

    /**
     * 全テンプレートを取得
     */
    @Query("SELECT * FROM templates ORDER BY display_order ASC, created_at ASC")
    suspend fun getAll(): List<TemplateEntity>

    /**
     * テンプレート件数を取得
     */
    @Query("SELECT COUNT(*) FROM templates")
    suspend fun getCount(): Int

    /**
     * 指定カテゴリのテンプレートを取得
     */
    @Query("SELECT * FROM templates WHERE category_id = :categoryId ORDER BY display_order ASC, created_at ASC")
    suspend fun getByCategoryId(categoryId: String): List<TemplateEntity>

    /**
     * 全テンプレートを削除
     */
    @Query("DELETE FROM templates")
    suspend fun deleteAll()
}
