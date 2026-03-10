package com.tinygc.okodukai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tinygc.okodukai.data.local.entity.CategoryOrderEntity
import kotlinx.coroutines.flow.Flow

/**
 * カテゴリ並び順データアクセスオブジェクト
 */
@Dao
interface CategoryOrderDao {

    /**
     * カテゴリ並び順を追加
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categoryOrder: CategoryOrderEntity)

    /**
     * カテゴリ並び順を更新
     */
    @Update
    suspend fun update(categoryOrder: CategoryOrderEntity)

    /**
     * カテゴリ並び順を削除
     */
    @Delete
    suspend fun delete(categoryOrder: CategoryOrderEntity)

    /**
     * カテゴリIDで並び順を取得
     */
    @Query("SELECT * FROM category_orders WHERE category_id = :categoryId")
    suspend fun getByCategoryId(categoryId: String): CategoryOrderEntity?

    /**
     * カテゴリIDで並び順を取得（Flow）
     */
    @Query("SELECT * FROM category_orders WHERE category_id = :categoryId")
    fun getByCategoryIdFlow(categoryId: String): Flow<CategoryOrderEntity?>

    /**
     * parent_idでフィルタして並び順のリストを取得
     * parent_id が NULL の場合は親カテゴリ間の並び順を取得
     */
    @Query("SELECT * FROM category_orders WHERE parent_id IS NULL ORDER BY display_order ASC")
    suspend fun getParentOrders(): List<CategoryOrderEntity>

    /**
     * parent_idでフィルタして並び順のリストを取得（Flow）
     */
    @Query("SELECT * FROM category_orders WHERE parent_id IS NULL ORDER BY display_order ASC")
    fun getParentOrdersFlow(): Flow<List<CategoryOrderEntity>>

    /**
     * 指定parent_idのサブカテゴリ並び順を取得
     */
    @Query("SELECT * FROM category_orders WHERE parent_id = :parentId ORDER BY display_order ASC")
    suspend fun getSubOrders(parentId: String): List<CategoryOrderEntity>

    /**
     * 指定parent_idのサブカテゴリ並び順を取得（Flow）
     */
    @Query("SELECT * FROM category_orders WHERE parent_id = :parentId ORDER BY display_order ASC")
    fun getSubOrdersFlow(parentId: String): Flow<List<CategoryOrderEntity>>

    /**
     * 指定parent_idの最大display_orderを取得
     */
    @Query("SELECT MAX(display_order) FROM category_orders WHERE parent_id IS NULL")
    suspend fun getMaxParentDisplayOrder(): Int?

    /**
     * 指定parent_idの最大display_orderを取得（サブカテゴリ用）
     */
    @Query("SELECT MAX(display_order) FROM category_orders WHERE parent_id = :parentId")
    suspend fun getMaxSubDisplayOrder(parentId: String): Int?

    /**
     * カテゴリIDで並び順を削除
     */
    @Query("DELETE FROM category_orders WHERE category_id = :categoryId")
    suspend fun deleteByCategoryId(categoryId: String)

    /**
     * 全並び順を取得
     */
    @Query("SELECT * FROM category_orders ORDER BY parent_id ASC, display_order ASC")
    suspend fun getAll(): List<CategoryOrderEntity>

    /**
     * 全並び順を取得（Flow）
     */
    @Query("SELECT * FROM category_orders ORDER BY parent_id ASC, display_order ASC")
    fun getAllFlow(): Flow<List<CategoryOrderEntity>>
}
