# アーキテクチャ設計

## 方針
- Clean Architectureを採用し、レイヤ間の依存方向を固定する
- UIとドメインを分離し、テスト可能性を優先する
- ウィジェット・Pixel Watchは入力チャネルとして扱い、アプリ本体のユースケースに接続する

---

## レイヤ構成

### Presentation層
- 役割: 画面表示、ユーザー入力受付、状態管理
- 主な責務: 画面Stateの保持、UIイベントの発火、入力バリデーション
- 依存: Domain層のみ

### Domain層
- 役割: ビジネスルールとユースケースの実装
- 主な責務: 予算集計、支出記録、テンプレ処理、未分類整理
- 依存: Data層の抽象（Repositoryインターフェース）

### Data層
- 役割: データの永続化、外部IO
- 主な責務: DB読み書き、Repository実装、マッピング
- 依存: Domain層のモデル（最小限）

---

## 依存関係
- Presentation -> Domain
- Domain -> Repository Interface
- Data -> Repository Implementation -> DB

---

## 主要画面構成

### 1. 支出入力
- 初期表示のデフォルト画面
- 通常入力タブ: カテゴリ選択 -> 金額入力
- テンプレタブ: テンプレボタンから選択 -> 即時登録
- 日付変更（デフォルトは当日）
- クイック入力ボタン8件はユーザー設定値（初期値: 1, 5, 10, 50, 100, 500, 1000, 5000）を表示

### 2. 月次サマリ
- 当月の予算、支出、残額を表示
- 臨時収入は別枠で合計表示
- カテゴリ別の支出グラフを表示
- 支出一覧の編集/削除と未分類のカテゴリ付け
- 月選択で過去の月へ遡れる
- カテゴリ別支出はTop3を表示し、残りは「その他」に集約
- 支出一覧は最新5件を表示し、全件表示へ遷移
- 月切替はスワイプ/矢印の両方に対応
- 空月は専用メッセージを表示

### 3. 管理
- 管理メニューの集約画面
- テンプレ管理 / カテゴリ管理 / 臨時収入管理 / 月別履歴 / 予算設定 / クイック入力金額設定

### 4. テンプレ管理
- テンプレ作成・編集・削除
- カテゴリ/サブカテゴリ/金額を設定

### 5. カテゴリ管理
- カテゴリ/サブカテゴリの作成・編集・削除

### 6. 月別履歴
- 月ごとの予算・支出・残額を表示

### 7. 臨時収入管理
- 臨時収入の追加・編集・削除
- 月別の合計表示

### 8. 予算設定
- 毎月の固定予算額を設定・変更
- 設定した固定予算額を毎月の予算として自動反映

### 9. 貯金目標管理
- 貯金目標の作成・編集・削除
- 達成モード（個別達成/合計達成）の切替
- 繰越残高と目標進捗の確認

---

## 入力チャネル

### 端末ホーム画面ウィジェット
- 金額のみ入力
- 支出は未分類として保存

---

## バックアップマイグレーション設計

### バックアップフォーマット方針
- バックアップJSONは `backupSchemaVersion` を必須とする
- ルートに `backupPolicy` を持ち、データ集合ごとに `INCLUDED` / `EXCLUDED` を明示する
- `EXCLUDED` は「復元不要」ではなく「復元時に再構築する前提データ」として扱う

### Importパイプライン
1. JSON構文チェック
2. `backupSchemaVersion` 判定
3. `BackupMigrationStep` を古い順に適用（例: v1->v2, v2->v3）
4. 正規化済みDTOを検証（必須項目、ID重複、参照整合）
5. トランザクション内で全置換Import
6. `EXCLUDED` データの再構築（デフォルト投入、再計算、キャッシュ再生成）

### 互換性ルール
- backward compatibility: 旧バックアップを新アプリで読めること
- forward compatibility: 未知フィールドは無視すること
- breaking change時は `backupSchemaVersion` を更新し、対応Migrationを実装すること

### レイヤ責務
- Presentation: 進捗表示、失敗時メッセージ
- Domain: Import/Exportユースケース（Repository呼び出し）
- Data: JSON I/O、Drive AppData I/O、Migration適用、検証、DB全置換保存

---

## 画面遷移（概要）
- 下部タブ: 支出入力 / 月次サマリ / 管理
- 管理 -> テンプレ管理
- 管理 -> カテゴリ管理
- 管理 -> 臨時収入管理
- 管理 -> 月別履歴
- 管理 -> 予算設定
- 管理 -> 貯金目標管理
- 管理 -> クイック入力金額設定
- 月次サマリ -> カテゴリ別一覧
- 月次サマリ -> 支出一覧
- 支出一覧 -> 支出編集

---

## 画面遷移（詳細フロー）

### 通常入力フロー
1. 下部タブで「支出入力」を選択
2. 通常タブでカテゴリを選択
3. 金額を入力
4. 保存で月次サマリに戻る

### テンプレ入力フロー
1. 下部タブで「支出入力」を選択
2. テンプレタブを切り替え
3. テンプレボタンを選択
4. 即時保存し月次サマリに戻る

### ウィジェット入力フロー
1. 端末ホーム画面ウィジェットの金額欄に入力
2. 登録ボタンで保存（未分類）
3. 月次サマリの支出一覧でカテゴリ付け

### 臨時収入フロー
1. 下部タブで「管理」を選択
2. 臨時収入管理へ
2. 追加/編集/削除
3. 月別合計を確認

---

## モデル概要（ドメイン）

### Budget
- id, month, amount

### Expense
- id, date, amount, categoryId, subCategoryId, memo, isUncategorized

### Income
- id, date, amount, memo

### SavingGoal
- id, name, targetAmount, isActive, displayOrder

### Category
- id, name, parentId (サブカテゴリ用)

### Template
- id, name, categoryId, subCategoryId, amount

### UserPreferences
- defaultCategoryId
- goalAchievementMode
- quickInputAmounts (8件、各1〜99999整数)

---

## 例: ユースケース一覧
- CreateExpense
- UpdateExpense
- CreateIncome
- UpdateIncome
- GetSavingsProgress
- SaveSavingGoal
- CreateTemplate
- UpdateTemplate
- GetMonthlySummary
- GetCategorySummary

---

## ユースケース優先度

### フェーズ1（MVP・最優先）
- CreateExpense
- UpdateExpense
- GetMonthlySummary
- GetCategorySummary
- CreateTemplate
- UpdateTemplate

### フェーズ2（入力効率化）
- CreateIncome
- UpdateIncome
- GetSavingsProgress
- SaveSavingGoal

### フェーズ3（周辺機能）
- ウィジェット機能の拡張

---

## 非機能的な注意
- UIの入力導線は最短化し、3タップ以内で支出記録を完了できるよう設計
- DBアクセスは非同期で実行し、UIスレッドをブロックしない
- 支出の追加/編集は1秒以内、グラフ表示は2秒以内を目標
- ウィジェット入力は5秒以内に完了する導線にする
- 強調は色に依存せず、サイズ/余白/太字/線幅で設計する
