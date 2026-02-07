package com.tinygc.okodukai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tinygc.okodukai.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * カテゴリデータアクセスオブジェクト
 */
@Dao
interface CategoryDao {

    /**
     * カテゴリを追加
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    /**
     * カテゴリを更新
     */
    @Update
    suspend fun update(category: CategoryEntity)

    /**
     * カテゴリを削除
     */
    @Delete
    suspend fun delete(category: CategoryEntity)

    /**
     * IDでカテゴリを取得
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    /**
     * IDでカテゴリを取得（Flow）
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    fun getByIdFlow(id: String): Flow<CategoryEntity?>

    /**
     * 親カテゴリのみを取得（parent_id が NULL）
     */
    @Query("SELECT * FROM categories WHERE parent_id IS NULL ORDER BY created_at ASC")
    fun getParentCategoriesFlow(): Flow<List<CategoryEntity>>

    /**
     * 親カテゴリのみを取得
     */
    @Query("SELECT * FROM categories WHERE parent_id IS NULL ORDER BY created_at ASC")
    suspend fun getParentCategories(): List<CategoryEntity>

    /**
     * 親カテゴリの件数を取得
     */
    @Query("SELECT COUNT(*) FROM categories WHERE parent_id IS NULL")
    suspend fun getParentCategoryCount(): Int

    /**
     * 指定親カテゴリのサブカテゴリを取得
     */
    @Query("SELECT * FROM categories WHERE parent_id = :parentId ORDER BY created_at ASC")
    fun getSubCategoriesFlow(parentId: String): Flow<List<CategoryEntity>>

    /**
     * 指定親カテゴリのサブカテゴリを取得
     */
    @Query("SELECT * FROM categories WHERE parent_id = :parentId ORDER BY created_at ASC")
    suspend fun getSubCategories(parentId: String): List<CategoryEntity>

    /**
     * 指定親カテゴリのサブカテゴリ件数を取得
     */
    @Query("SELECT COUNT(*) FROM categories WHERE parent_id = :parentId")
    suspend fun getSubCategoryCount(parentId: String): Int

    /**
     * 全カテゴリを取得
     */
    @Query("SELECT * FROM categories ORDER BY parent_id ASC, created_at ASC")
    suspend fun getAll(): List<CategoryEntity>

    /**
     * 全カテゴリを取得（Flow）
     */
    @Query("SELECT * FROM categories ORDER BY parent_id ASC, created_at ASC")
    fun getAllFlow(): Flow<List<CategoryEntity>>
}
