# おこづかいアプリ開発 - 申し送り事項

## 🧭 月次サマリ再設計（モノクロ）ワイヤー構成 1枚

目的: スマホ前提で「残額」を最優先に見せ、次に理由（予算/支出）、最後に詳細（カテゴリ/履歴）へ視線誘導。

```
┌─────────────────────────────────────┐
│ 〈 2026年2月 〉                      │  ← 月移動/現在地
│  今月に戻る                           │  ← 目立たせ過ぎない操作
├─────────────────────────────────────┤
│  残額                                │  ← 最重要ラベル
│  ¥12,340                             │  ← 最大サイズ/太字
│  予算 ¥50,000   支出 ¥37,660         │  ← 補助情報
│  [███████████░░░░░░░░░] 75%          │  ← 進捗バー(濃淡のみ)
├─────────────────────────────────────┤
│  カテゴリ別支出                       │
│  (円グラフ: 濃淡)   食費 40%          │  ← Top3だけ表示
│                    交通 25%          │
│                    日用品 15%        │
│  一覧を見る ＞                        │  ← 詳細遷移
├─────────────────────────────────────┤
│  最近の支出                           │
│  02/14  食費     ¥1,200              │
│  02/13  交通     ¥  450              │
│  02/13  日用品   ¥  980              │
│  全件表示 ＞                          │
└─────────────────────────────────────┘
```

### 詳細仕様（サイズ/余白/文言）

**タイポスケール**
- 残額数字: 34sp, Bold
- 残額ラベル: 14sp, Medium
- 予算/支出: 14sp, Regular
- セクションタイトル: 16sp, SemiBold
- 一覧行: 15sp, Regular
- リンク文言（一覧を見る/全件表示）: 12sp, Medium

**レイアウトと余白**
- 画面左右パディング: 16dp
- セクション間の縦余白: 16dp
- 残額カード内パディング: 16dp
- 行間: 6dp（行内の数字とラベル）
- 進捗バーの高さ: 6dp

**カード/区切り**
- 残額カード角丸: 20dp
- 通常カード角丸: 12dp
- 区切り線: 1dp（濃淡で強弱）
- 予算超過時: 枠線2dp + 残額数字太字維持

**文言ルール**
- 残額ラベルは「残額」で固定（迷いを減らす）
- 補助情報は「予算」「支出」のみ
- 一覧リンクは右寄せで小さく配置

### 強調ルール
- 色ではなく「サイズ・太字・余白・角丸・線幅」で優先度を出す
- 残額カードは角丸大きめ + 余白厚め + 数字最大
- 予算超過時はカード枠線を太く + 数字の太字強調
- 一覧/詳細リンクは小さく右寄せで階層を下げる

### グラフと一覧の表示ルール

**カテゴリ別グラフ**
- デフォルトはTop3のみ表示（その他は「その他」に集約）
- Top3の下に「一覧を見る ＞」を配置
- 0件のときは「データがありません」を1行で表示
- 表示順は金額の降順

**支出一覧**
- デフォルトは最新5件まで表示
- 0件のときは「今月の支出はありません」
- 表示順は日付の降順（同日なら作成日時降順）
- 行の並びは「日付｜カテゴリ｜金額」
- 未分類はカテゴリ名を「未分類」で固定

**リンク文言**
- グラフ側: 「一覧を見る ＞」
- 支出一覧側: 「全件表示 ＞」

### 月選択UIの挙動

**UI構成**
- ヘッダー中央に「YYYY年M月」
- 左右に小さな矢印（前月/翌月）
- 右上に「今月に戻る」テキストボタン

**操作**
- 左右スワイプで月移動（メイン導線）
- 矢印タップでも月移動（補助導線）
- 「今月に戻る」は当月以外のときだけ表示

**状態表示**
- 選択中の月はヘッダーの太字で強調
- 当月は「今月」ラベルを小さく併記

**空月の扱い**
- 予算/支出/残額は0表示
- グラフと一覧は空表示メッセージ

### 画面遷移と導線ルール

**遷移先**
- 「一覧を見る ＞」: カテゴリ別一覧画面へ遷移
- 「全件表示 ＞」: 支出一覧画面へ遷移
- 支出一覧の行タップ: 支出編集画面へ遷移

**戻り導線**
- 一覧/編集から戻ったときは元の月を維持
- 編集/削除後は月次サマリを再読込

**編集/削除**
- 行スワイプで削除（補助）
- 行タップで編集（主要）
- 削除前は確認ダイアログを必須

**未分類の導線**
- 未分類は一覧で「未分類」ラベル固定
- 未分類タップ時はカテゴリ付けを最初に表示

### アニメーション/インタラクション

**ページ入場**
- 残額カードと進捗バーを先にフェードイン（150ms）
- その後にグラフ→一覧をスタッガー表示（各80ms）

**月切替**
- 左右スワイプ時はコンテンツ全体をスライド（120ms）
- 数値の変化はクロスフェード（150ms）

**行操作**
- 行タップ時は薄いリップル
- スワイプ削除は「削除」背景がスライド表示

**空表示**
- 空メッセージは小さなフェードインのみ

## 🧩 MonthlySummaryScreen 実装差分（設計）

対象: 現行の月次サマリUIを再設計仕様に合わせて改修

### 変更ポイント（UI構造）
- ヘッダー: 「YYYY年M月」中央 + 前月/翌月矢印 + 右上「今月に戻る」
- 残額カード: 残額ラベル/大きな残額数字/予算・支出/進捗バー
- カテゴリ別: Top3 + その他集約 + 「一覧を見る ＞」
- 支出一覧: 最新5件 + 「全件表示 ＞」

### 変更ポイント（表示ルール）
- Top3のみ表示、その他は「その他」
- 0件のときは空表示メッセージ
- 未分類は「未分類」固定
- 行タップで編集、スワイプで削除

### 変更ポイント（文言）
- 残額ラベルは「残額」固定
- グラフ側リンク「一覧を見る ＞」
- 支出一覧側リンク「全件表示 ＞」

### 変更ポイント（スタイル）
- 残額数字 34sp/Bold
- セクションタイトル 16sp/SemiBold
- 画面左右 16dp / セクション間 16dp
- 残額カード角丸 20dp / 通常カード 12dp

### 変更ポイント（挙動）
- 月切替: スワイプ + 矢印タップ
- 当月以外で「今月に戻る」表示
- 月切替時は数値クロスフェード

### 追加/変更が必要なUI状態
- Top3カテゴリの抽出結果
- 「その他」合計
- 空月フラグ（全データ0）

### ViewModel設計（追加状態）

**状態追加案**
- topCategories: List<CategoryTotal>
- otherTotal: Int
- isEmptyMonth: Boolean

**算出ルール**
- topCategories: カテゴリ合計の降順Top3
- otherTotal: Top3以外の合計
- isEmptyMonth: 予算=0 && 支出=0 && 収入=0 && 一覧0件

**UI利用**
- topCategoriesが空なら「データがありません」
- otherTotal > 0 のときのみ「その他」を表示
- isEmptyMonthなら空月の表示ルールを適用

### 既存ViewModel/UiStateへの統合方針

**対象**
- presentation/viewmodel/MonthlySummaryViewModel.kt
- presentation/viewmodel/MonthlySummaryUiState.kt
- MonthlySummaryScreenは上記を使用（現状のscreen配下ViewModelは整理対象）

**UiStateへの追加項目**
- topCategories: List<CategoryTotalUiModel>
- otherTotal: Int
- isEmptyMonth: Boolean

**導出の入れ方**
- ViewModelでloadSummary内に集計処理を追加
- categoryTotalsから降順Top3を抽出しtopCategoriesに格納
- otherTotalはTop3以外の合計
- isEmptyMonthは(予算==0 && 支出==0 && 収入==0 && 一覧0件)

**UI側での利用**
- topCategoriesが空なら「データがありません」
- otherTotal>0なら「その他」を1行で追加
- isEmptyMonthなら空月の表示ルールを優先

## 🎯 完了した成果物

### 1. 要件定義 (`requirement/requirements.md`)
- **機能要件 9個** (FR-1 ~ FR-9)
  - FR-1: 月次単位での支出管理＋月別サマリ（カレンダーで過去月閲覧可）
  - FR-2: 支出追加＆編集（日付変更可能、テンプレ or 通常入力）
  - FR-3: カテゴリ管理（最大10個、親子関係対応）
  - FR-4: テンプレ管理（最大10個のテンプレを登録・利用）
  - FR-5: 月額予算設定（月ごと、集計時に実績と比較）
  - FR-6: 月次サマリ画面（カテゴリ別集計、支出一覧、日付で遡り可能）
  - FR-7: 未分類支出の即時カテゴリ化（月次サマリで実施）
  - FR-8: 支出削除（月次サマリで実施）
  - FR-9: 臨時収入の記録（集計対象外、月次サマリで別枠表示）

- **非機能要件 4個** (NFR-1 ~ NFR-4)
  - NFR-1: ローカル永続化 (Room + SQLite)
  - NFR-2: パフォーマンス（支出追加1秒以内、グラフ2秒以内、3タップ以内）
  - NFR-3: 拡張性（後続機能追加容易な設計）
  - NFR-4: ユーザビリティ（直感的な操作）

### 2. アーキテクチャ設計 (`architecture/architecture.md`)
- **Clean Architecture適用**
  - Presentation層: Jetpack Compose UI + MVVM
  - Domain層: Use Cases, Entities
  - Data層: Repository Pattern, Room DAOs

- **主要スクリーン 8個**
  1. 支出入力画面（通常+テンプレタブ）
  2. 月次サマリ画面（重要：集計＆編集＆削除の中心hub）
  3. 管理ハブ画面（テンプレ・カテゴリ・収入・履歴へのアクセス）
  4. テンプレ管理画面
  5. カテゴリ管理画面
  6. 臨時収入管理画面
  7. 月別履歴閲覧画面
  8. 予算設定画面

- **3フェーズ実装計画**
  - Phase 1 (MVP): 支出CRUD, テンプレ, カテゴリ, 予算, 月次サマリ
  - Phase 2: 臨時収入, 月別履歴
  - Phase 3: ウィジェット拡張（Pixel Watchは開発スコープ外）

### 3. UI仕様書 (`architecture/ui-spec.md`)
- **8スクリーン全てについて以下を定義**（Pixel Watch除く）
  - 入力値の検証（整数のみ、負値/ゼロ不可など）
  - 操作フロー（ボタン、タップ、スクロール）
  - 成功/失敗メッセージ（Toast, Dialog）
  - 確認ダイアログ（削除時など）
  - エラーメッセージ例
  - **カテゴリ管理画面は階層表示UI**（カテゴリタップ→サブカテゴリ展開）

### 4. データモデル (`architecture/data-model.md`)
- **5つのテーブル設計**
  ```
  budgets         (月ごとの予算)
  expenses        (支出、uncategorized_flagで未分類管理)
  incomes         (臨時収入)
  categories      (親子関係対応、最大10個制約)
  templates       (テンプレ、最大10個制約)
  ```

- **主要なIndeX**
  - expenses.date でMonth抽出用
  - expenses.category_id で集計用
  - budgets.month（YYYY-MM独自INDEX）

### 5. 指示ファイル更新 (`.github/instructions/`)
- **requirement.instructions.md**: スコープ・制約明確化の重要性を追加
- **architecture.instructions.md**: 要件との整合性確認を強化
- **implementation.instructions.md**: 4点検証（要件・アーキテクチャ・UI・データモデル）を明記
- **testing.instructions.md**: 要件駆動テスト設計を強化

---

## 🔑 重要な設計決定

### ナビゲーション構造
- **下部タブ 3個**
  1. 支出記録 → 支出入力フォーム (通常/テンプレ)
  2. 月次サマリ → 当月/過去月の集計・編集・削除hub
  3. 管理ハブ → テンプレ・カテゴリ・収入・履歴設定へのアクセス

### 月次サマリ画面を中核に設計
- 未分類支出を即時カテゴリ化
- 支出の削除・再編集（日付変更可）
- 月別スクロール機能（カレンダーで過去月閲覧）
- カテゴリ別集計表示
- 臨時収入を別枠表示（集計対象外）

### データ集約方針
- **支出**: 月別でGROUP BY → Expressを使用可能
- **臨時収入**: 支出テーブルと分離 → 集計時に除外
- **カテゴリ**: 親子関係対応（parent_id NULLで主カテゴリ）
- **上限制約**: カテゴリ10個、テンプレ10個（DB/App層で実装可能）

### パフォーマンス目標
- 支出追加＆編集: **1秒以内**
- グラフ表示: **2秒以内**
- ウィジェット入力: **5秒以内**

---

## ✅ Phase 1 & 2 実装完了 (2026-02-07)

### 実装完了した機能
1. **全6画面の実装**
   - ✅ ExpenseEntryScreen (支出入力:通常/テンプレ)
   - ✅ MonthlySummaryScreen (月次サマリ:集計/編集/削除)
   - ✅ BudgetSettingScreen (予算設定)
   - ✅ IncomeManagementScreen (臨時収入管理)
   - ✅ CategoryManagementScreen (カテゴリ管理:階層表示)
   - ✅ TemplateManagementScreen (テンプレ管理)
   - ✅ MonthlyHistoryScreen (月次履歴:12ヶ月分)

2. **完全なClean Architecture実装**
   - ✅ Data層: 5 Entities + 5 DAOs + Room Database
   - ✅ Domain層: 23 UseCases + Repository Interfaces
   - ✅ Presentation層: 7 ViewModels + Compose UI
   - ✅ DI: Hilt完全統合

3. **ビルド状態**
   - ✅ BUILD SUCCESSFUL (警告0件)
   - ✅ Compose Compiler Version: 1.5.14 (安定版)
   - ✅ Kotlin 1.9.24 + AGP 8.7.3

### テスト実装完了 (62テストケース全PASS)

#### Domain層 Unit Test (9ファイル)
1. ✅ SaveBudgetUseCaseTest - 予算管理 (FR-1)
2. ✅ AddExpenseUseCaseTest - 支出追加 (FR-2)
3. ✅ UpdateExpenseUseCaseTest - 支出更新 (FR-2)
4. ✅ DeleteExpenseUseCaseTest - 支出削除 (FR-2)
5. ✅ AddCategoryUseCaseTest - カテゴリ管理 (FR-3)
6. ✅ GetMonthlySummaryUseCaseTest - 月次サマリー (FR-4)
7. ✅ SaveIncomeUseCaseTest - 臨時収入 (FR-6)
8. ✅ AddTemplateUseCaseTest - テンプレ管理 (FR-9)
9. ✅ CreateExpenseFromTemplateUseCaseTest - テンプレから支出作成 (FR-9)

#### Presentation層 ViewModel Test (2ファイル)
1. ✅ BudgetSettingViewModelTest - 予算設定画面 (9テストケース)
2. ✅ MonthlySummaryViewModelTest - 月次サマリー画面 (5テストケース)

#### テストカバレッジ
- ✅ FR-1: 月単位の予算管理 (完全カバー)
- ✅ FR-2: 支出記録 (追加/更新/削除カバー)
- ✅ FR-3: カテゴリ・サブカテゴリ管理 (完全カバー)
- ✅ FR-4: 支出の可視化 (サマリー/集計カバー)
- ✅ FR-6: 臨時収入の記録 (完全カバー)
- ✅ FR-9: テンプレ管理 (完全カバー)

---

## 🎯 次にやるべきこと

### 優先度1: 動作確認とデバッグ 🔥
1. **APKビルド & 実機テスト**
   ```bash
   .\gradlew.bat assembleDebug
   # APK: app/build/outputs/apk/debug/app-debug.apk
   ```
   - 全画面の動作確認
   - 実際のタップ数カウント (NFR-3: 3タップ以内)
   - パフォーマンス測定 (NFR-2: 1秒/2秒以内)

2. **実機で発見されるバグの修正**
   - UI/UXの微調整
   - エラーハンドリングの改善

### 優先度2: 追加テスト 🧪
1. **UI Test (Compose UI Test)**
   - ExpenseEntryScreenTest (タップ操作)
   - MonthlySummaryScreenTest (カテゴリ化/削除)
   - CategoryManagementScreenTest (階層表示)

2. **追加ViewModel Test**
   - ExpenseEntryViewModelTest
   - CategoryManagementViewModelTest
   - IncomeManagementViewModelTest

3. **統合テストドキュメント作成**
   - `test/system-test-cases.md` 
   - シナリオテスト (支出記録→サマリー確認)
   - パフォーマンステスト (実測値記録)
   - ユーザビリティテスト (タップ数計測)

### 優先度3: Phase 3 追加機能 ⭐
- FR-7: ウィジェット実装 (ホーム画面から金額入力)
- FR-8: Pixel Watch連携 (優先度低、スコープ外の可能性あり)

---

## 📝 技術メモ

### 解決した主な課題
1. **Compose Compiler互換性**
   - 問題: Kotlin 1.9.24とCompose Compilerバージョン不一致
   - 解決: `kotlinCompilerExtensionVersion = "1.5.14"` を明示的に設定

2. **Room Result<T>型のハンドリング**
   - IncomeManagementViewModelで`getOrNull() ?: emptyList()`パターン採用

3. **テストでのCoroutine処理**
   - MainDispatcherRuleクラス作成
   - testScheduler.advanceTimeBy()で3秒delayをスキップ

### 既知の軽微な警告 (機能に影響なし)
- KeyboardArrowLeft/Right → AutoMirrored版推奨 (Material3)
- Divider → HorizontalDivider推奨
- menuAnchor()のパラメータ非推奨

---

## 🚀 次回開発時の推奨手順

1. APKビルドして実機確認
2. バグ修正 & UI微調整
3. 統合テストドキュメント作成
4. UI Testの追加実装
5. Phase 3機能検討

**現在地**: Phase 1 & 2 完全実装完了、テスト62ケース全PASS、次は実機確認フェーズ
- 画面遷移: 3タップ以内で支出記録可能

---

## 📝 実装前の準備状況

### 実装可能な状態
✅ 要件定義完全
✅ アーキテクチャ設計完全
✅ UI仕様完全
✅ データモデル完全
✅ トレーサビリティ確立（FR → 画面 → UI → テーブル）

### 次のステップで必要な作業

#### 1. Androidプロジェクト初期化
- `build.gradle.kts` (App-level)
  - Jetpack Compose 依存関係
  - Room 依存関係
  - その他ライブラリ (Coroutines, Hilt等)
- プロジェクト構造
  ```
  app/
    src/main/
      kotlin/com/example/okodukai/
        presentation/     (Compose screens, ViewModels)
        domain/           (UseCases, Entities, Repositories)
        data/             (DAOs, Database, Implementations)
  ```

#### 2. Room Database 実装
- Entity定義: Budgets, Expenses, Incomes, Categories, Templates
- DAO作成: 各テーブルのCRUD操作
- Database クラス: Room.databaseBuilder()
- Migration スクリプト（将来の変更対応）

#### 3. Domain層の定義
- Use Cases
  - AddExpenseUseCase, EditExpenseUseCase, DeleteExpenseUseCase
  - GetMonthlySummaryUseCase, GetExpensesByMonthUseCase
  - ManageCategoryUseCase, ManageTemplateUseCase
  - ManageIncomeUseCase, GetBudgetUseCase
- Entities: Expense, Income, Category, Template, Budget
- Repository インターフェース

#### 4. Data層 Repository 実装
- ExpenseRepository, IncomeRepository, CategoryRepository, TemplateRepository, BudgetRepository
- Room DAOを呼び出す統一インターフェース

#### 5. Presentation層 実装
- 各画面ごとViewModel + Composable
  - ExpenseEntryScreen / ExpenseEntryViewModel
  - MonthlySummaryScreen / MonthlySummaryViewModel
  - ManagementHubScreen / (各管理画面ViewModel)
- Navigation設定 (Jetpack Navigation Compose)

#### 6. テスト実装
- **ユニットテスト**: UseCase, Repository, ViewModel
- **UIテスト**: 画面遷移, ボタン操作, メッセージ表示確認
- テストケースリスト作成（要件 → テストケース マッピング）

---

## ⚠️ 注意点と留意事項

### 1. ドキュメント間の同期が重要
- 実装中に**UI仕様**の微調整が必要になった場合
  - 必ず**要件**と**アーキテクチャ**も同時に見直す
  - 複数ドキュメント間の乖離を防ぐ

### 2. データベース制約の実装方法
- **10項目制限** (カテゴリ, テンプレ)
  - **カテゴリ**: 親カテゴリは最大10件
  - **サブカテゴリ**: 各カテゴリごとに最大10件
  - **実装場所**: Data層（DB層またはRepository層）で検証
  - UI層でも事前チェックを実施し、分かりやすいエラー表示
  - 制約値は定数化して仕様変更に柔軟対応
  - UI仕様のエラーメッセージ: 「カテゴリは最大10件です」「サブカテゴリは最大10件です」

### 3. 未分類支出の扱い
- DB: `is_uncategorized` フラグで管理
- **自動設定**: `category_id = NULL` のとき自動的に `is_uncategorized = 1`
- **適用タイミング**: ウィジェットから金額のみで入力したとき
- **更新フロー**: 月次サマリでカテゴリ付け → `is_uncategorized = 0` に更新
- 集計: `WHERE is_uncategorized = 0` で未分類を除外

### 4. 臨時収入の扱い
- 別テーブル（incomes）で管理
- 月次サマリで 集計の**下部に別枠で表示**
- 予算比較時には**支出のみ**を対象

### 5. 月別データ取得のパフォーマンス
- Date型は `YYYY-MM-DD` で保存
- SQL: `strftime('%Y-%m', date) = '2024-02'` で抽出
- または: `date BETWEEN '2024-02-01' AND '2024-02-29'` での範囲検索

### 6. テンプレとカテゴリの上限
- **テンプレ**: 最大10個（FR-4で要件化）
- **カテゴリ**: 最大10個（親カテゴリのみ、`parent_id = NULL`）
- **サブカテゴリ**: 各カテゴリごとに最大10個（各親カテゴリの配下）
  - 例: 「食費」（カテゴリ）→「食事」「飲料」「その他」（サブカテゴリ、各10件まで）
- **UI表現**: 階層表示（カテゴリをタップ→サブカテゴリ一覧展開）

### 7. 今後の拡張を見据えた設計
- Phase 2 (臨時収入) はincomes テーブルが既に存在
- Phase 3 (ウィジェット) はWidget固有のEntity不要（ExpenseRepository再利用）
- **Pixel Watch連携** (FR-8) は優先度低で現在の開発スコープ外
  - 将来的な拡張機能として要件に保持
  - 実装時はデバイス情報テーブル追加を検討

---

## 📊 進捗サマリー

| フェーズ | 項目 | 進捗 |
|--------|------|------|
| **設計** | 要件定義 | ✅ 完了 |
| | アーキテクチャ | ✅ 完了 |
| | UI仕様 | ✅ 完了 |
| | データモデル | ✅ 完了 |
| | 設計整合性確認 | ✅ 完了 (2026-02-07) |
| **準備** | 指示ファイル更新 | ✅ 完了 |
| **実装準備** | Androidプロジェクト初期化 | ⏳ 未開始 |
| | Room設計実装 | ⏳ 未開始 |
| | Domain/Data層 | ⏳ 未開始 |
| | Presentation層 | ⏳ 未開始 |
| | テスト設計 | ⏳ 未開始 |

---

## 🚀 次回開発開始時のチェックリスト

- [ ] temp.md を参照して前回の決定事項を確認
- [ ] requirement/requirements.md 最新内容を確認
- [ ] architecture/architecture.md でスクリーン/フロー確認
- [ ] architecture/ui-spec.md で画面仕様確認
- [ ] architecture/data-model.md でテーブル定義確認
- [ ] 実装対象スクリーン/機能の FR と UI を明記してから実装開始
- [ ] 実装中の変更があれば、すべてのドキュメントの同期を確認

---

**作成日**: 2024-02-07  
**更新日**: 2026-02-07  
**プロジェクト**: おこづかいアプリ (Android, Kotlin, Jetpack Compose)  
**ステータス**: 設計完了、実装開始準備中

---

## 🔄 最新の更新履歴 (2026-02-07)

### 設計の明確化と整合性確認
1. **Pixel Watchを優先度低として除外**
   - architecture.mdから入力チャネル・フロー・ユースケースを削除
   - ui-spec.mdから画面仕様セクションを削除
   - requirements.mdのFR-8に「現在の開発スコープには含まれません」と明記
   - Phase 3以降の将来的な拡張機能として保持

2. **カテゴリ管理UIの階層表示化**
   - ui-spec.mdの「カテゴリ管理」画面を階層表示に変更
   - カテゴリをタップ→サブカテゴリ一覧展開
   - 各カテゴリごとにサブカテゴリ最大10件
   - バリデーションメッセージを分離（カテゴリ用・サブカテゴリ用）

3. **データモデルの制約を詳細化**
   - カテゴリ（親）は最大10件
   - サブカテゴリは各カテゴリごとに最大10件
   - 10件制約はData層で検証（仕様変更に柔軟対応）
   - 未分類フラグ（is_uncategorized）の動作を明確化
     - category_id = NULLのとき自動的に1
     - ウィジェット入力時に適用
     - 月次サマリでカテゴリ付け後に0に更新

4. **ドキュメント整合性確認完了**
   - 要件定義 ✅
   - アーキテクチャ設計 ✅
   - UI仕様 ✅
   - データモデル ✅
   - トレーサビリティ確立（FR → 画面 → UI → テーブル）✅
