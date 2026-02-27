# おこづかいアプリ - コーディングスタイル & コンベンション

## 言語
- **Kotlin 1.9.24**
- **JVM Target**: Java 11

## ファイル構成規則

### パッケージ構造
```
com.tinygc.okodukai/
  data/           - Repository実装、Room Entities、DAOs
  di/             - Hilt DIモジュール
  domain/         - Use Cases、Repository Interfaces、Domain Models
  presentation/   - Compose Screens、ViewModels、UI State
```

### ファイル命名規則
- **Entity**: `[名前]Entity.kt` (例: `ExpenseEntity.kt`)
- **DAO**: `[名前]Dao.kt` (例: `ExpenseDao.kt`)
- **Repository Interface**: `[名前]Repository.kt` (例: `ExpenseRepository.kt`)
- **Repository実装**: `[名前]RepositoryImpl.kt` (例: `ExpenseRepositoryImpl.kt`)
- **Use Case**: `[動詞][名前]UseCase.kt` (例: `AddExpenseUseCase.kt`)
- **ViewModel**: `[画面名]ViewModel.kt` (例: `ExpenseEntryViewModel.kt`)
- **Screen**: `[画面名]Screen.kt` (例: `ExpenseEntryScreen.kt`)
- **DI Module**: `[用途]Module.kt` (例: `DatabaseModule.kt`)

## Kotlin コーディングスタイル

### クラス・関数命名
- **クラス**: PascalCase (例: `ExpenseRepository`)
- **関数**: camelCase (例: `getExpensesByMonth`)
- **プロパティ**: camelCase (例: `categoryName`)
- **定数**: UPPER_SNAKE_CASE (例: `MAX_CATEGORIES`)

### データクラス
```kotlin
data class Expense(
    val id: String,
    val date: String,
    val amount: Int,
    val categoryId: String?,
    val subCategoryId: String?,
    val memo: String?,
    val isUncategorized: Boolean
)
```

### 関数スタイル
- **シングル式関数**（可能な場合）:
```kotlin
fun calculateRemaining(budget: Int, spent: Int): Int = budget - spent
```

- **複数行関数**:
```kotlin
fun getMonthlySummary(month: String): Flow<MonthlySummary> {
    return flow {
        val budget = budgetDao.getBudgetByMonth(month)
        val expenses = expenseDao.getExpensesByMonth(month)
        emit(MonthlySummary(budget, expenses))
    }
}
```

### Nullable vs Non-Null
- **基本方針**: Non-Nullを優先、必要な場合のみNullable
- **Nullable処理**: `?.let { }`, `?:`, `!!`（最終手段のみ）

```kotlin
val category = categoryId?.let { categoryDao.getCategoryById(it) }
val name = category?.name ?: "未分類"
```

## Clean Architecture レイヤ規則

### Data層
- **Entity**: Room Entityのみ、ドメインモデルと分離
- **DAO**: Room DAOインターフェース、suspend関数とFlow使用
- **Repository実装**: DAOを呼び出し、ドメインモデルに変換

```kotlin
// Entity (Data層)
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val date: String,
    val amount: Int,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "is_uncategorized") val isUncategorized: Boolean
)

// DAO (Data層)
@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE date LIKE :month || '%'")
    fun getExpensesByMonth(month: String): Flow<List<ExpenseEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)
}

// Repository実装 (Data層)
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {
    override fun getExpensesByMonth(month: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByMonth(month).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
```

### Domain層
- **Domain Model**: シンプルなデータクラス、ビジネスロジックなし
- **Use Case**: 単一責任、1つのビジネスロジックのみ
- **Repository Interface**: 抽象定義のみ

```kotlin
// Domain Model (Domain層)
data class Expense(
    val id: String,
    val date: String,
    val amount: Int,
    val categoryId: String?,
    val subCategoryId: String?,
    val memo: String?,
    val isUncategorized: Boolean
)

// Use Case (Domain層)
class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense): Result<Unit> {
        return try {
            expenseRepository.addExpense(expense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Repository Interface (Domain層)
interface ExpenseRepository {
    fun getExpensesByMonth(month: String): Flow<List<Expense>>
    suspend fun addExpense(expense: Expense)
}
```

### Presentation層
- **State**: データクラスでUI Stateを定義
- **ViewModel**: StateFlowでStateを管理、UI Eventを処理
- **Screen**: Composable関数、ViewModelからStateを購読

```kotlin
// UI State (Presentation層)
data class ExpenseEntryState(
    val amount: String = "",
    val selectedCategoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// ViewModel (Presentation層)
@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(ExpenseEntryState())
    val state: StateFlow<ExpenseEntryState> = _state.asStateFlow()
    
    fun onAmountChanged(amount: String) {
        _state.update { it.copy(amount = amount) }
    }
    
    fun onSaveClicked() {
        viewModelScope.launch {
            // ビジネスロジック実行
        }
    }
}

// Screen (Presentation層)
@Composable
fun ExpenseEntryScreen(
    viewModel: ExpenseEntryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Column {
        TextField(
            value = state.amount,
            onValueChange = { viewModel.onAmountChanged(it) }
        )
        Button(onClick = { viewModel.onSaveClicked() }) {
            Text("保存")
        }
    }
}
```

## Jetpack Compose スタイル

### Composable関数命名
- PascalCase、名詞形（例: `ExpenseEntryScreen`, `CategoryList`）
- プレビュー関数: `[関数名]Preview` (例: `ExpenseEntryScreenPreview`)

### パラメータ順序
```kotlin
@Composable
fun ExpenseItem(
    // 1. データ
    expense: Expense,
    // 2. コールバック
    onClick: () -> Unit,
    // 3. Modifier（最後、デフォルト値あり）
    modifier: Modifier = Modifier
) { ... }
```

### State管理
```kotlin
// remember を使用
var amount by remember { mutableStateOf("") }

// ViewModel から State 購読
val state by viewModel.state.collectAsStateWithLifecycle()

// LaunchedEffect で副作用
LaunchedEffect(Unit) {
    viewModel.loadData()
}
```

## Hilt 依存性注入

### アノテーション規則
- **ViewModel**: `@HiltViewModel`
- **Singleton**: `@Singleton`
- **Inject**: `@Inject constructor()`

```kotlin
// ViewModel
@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() { ... }

// Repository実装
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository { ... }

// DI Module
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OkodukaiDatabase {
        return Room.databaseBuilder(
            context,
            OkodukaiDatabase::class.java,
            "okodukai.db"
        ).build()
    }
}
```

## Room データベーススタイル

### テーブル命名
- 小文字、複数形（例: `expenses`, `categories`, `budgets`）

### カラム命名
- スネークケース（例: `category_id`, `is_uncategorized`）
- Kotlinプロパティはキャメルケース、`@ColumnInfo`でマッピング

```kotlin
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val date: String,
    val amount: Int,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "sub_category_id") val subCategoryId: String?,
    @ColumnInfo(name = "is_uncategorized") val isUncategorized: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String
)
```

### インデックス
```kotlin
@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["date"]),
        Index(value = ["category_id"])
    ]
)
data class ExpenseEntity(...)
```

## テストスタイル

### テストクラス命名
- `[クラス名]Test` (例: `AddExpenseUseCaseTest`)

### テスト関数命名
- バッククォートで日本語可、またはスネークケース英語
```kotlin
@Test
fun `予算を正常に保存できる`() { ... }

// または
@Test
fun should_save_budget_successfully() { ... }
```

### テスト構造（Given-When-Then）
```kotlin
@Test
fun `支出を追加できる`() = runTest {
    // Given
    val expense = Expense(...)
    
    // When
    val result = addExpenseUseCase(expense)
    
    // Then
    assertTrue(result.isSuccess)
    verify(mockRepository).addExpense(expense)
}
```

## コメント・ドキュメンテーション

### KDoc（必要な場合のみ）
```kotlin
/**
 * 指定された月の支出一覧を取得するUse Case
 *
 * @param month YYYY-MM形式の月
 * @return 支出リストのFlow
 */
class GetExpensesByMonthUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) { ... }
```

### インラインコメント（複雑なロジックのみ）
```kotlin
// 未分類の場合、category_idをNULLに設定
val categoryId = if (isUncategorized) null else selectedCategoryId
```

## エラーハンドリング

### Result型の使用
```kotlin
suspend fun addExpense(expense: Expense): Result<Unit> {
    return try {
        expenseRepository.addExpense(expense)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### ViewModelでのエラー処理
```kotlin
fun onSaveClicked() {
    viewModelScope.launch {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        
        addExpenseUseCase(expense).fold(
            onSuccess = {
                _state.update { it.copy(isLoading = false) }
                // 成功処理
            },
            onFailure = { error ->
                _state.update {
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }
        )
    }
}
```

## コード品質チェックリスト

### 実装時の確認事項
- [ ] 適切なパッケージに配置されているか
- [ ] Clean Architectureのレイヤ依存関係を守っているか
- [ ] Hiltアノテーションが正しく付与されているか
- [ ] Nullable vs Non-Null が適切か
- [ ] suspend関数またはFlowを使用しているか（Data層）
- [ ] エラーハンドリングが実装されているか
- [ ] ユニットテストが書かれているか

### コードレビュー時の確認事項
- [ ] 命名規則に従っているか
- [ ] 単一責任原則を守っているか
- [ ] 不要なコメントがないか
- [ ] マジックナンバーが定数化されているか
- [ ] テストがある程度網羅されているか
