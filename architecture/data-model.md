# データモデル詳細

## 前提
- 永続化はRoom（SQLite）を使用
- 金額は整数（JPY）
- 日付はUTCではなくローカル日付（YYYY-MM-DD）で管理

---

## テーブル一覧

### 1. budgets
| カラム | 型 | 制約 | 説明 |
|---|---|---|---|
| id | TEXT | PK | UUID |
| month | TEXT | UNIQUE, NOT NULL | YYYY-MM |
| amount | INTEGER | NOT NULL | 予算金額 |
| created_at | TEXT | NOT NULL | 作成日時 |
| updated_at | TEXT | NOT NULL | 更新日時 |

インデックス:
- UNIQUE(month)

---

### 2. expenses
| カラム | 型 | 制約 | 説明 |
|---|---|---|---|
| id | TEXT | PK | UUID |
| date | TEXT | NOT NULL | YYYY-MM-DD |
| amount | INTEGER | NOT NULL | 支出金額 |
| category_id | TEXT | NULL | categories.id |
| sub_category_id | TEXT | NULL | categories.id |
| memo | TEXT | NULL | メモ |
| is_uncategorized | INTEGER | NOT NULL | 0/1 |
| created_at | TEXT | NOT NULL | 作成日時 |
| updated_at | TEXT | NOT NULL | 更新日時 |

インデックス:
- idx_expenses_date (date)
- idx_expenses_category (category_id)
- idx_expenses_sub_category (sub_category_id)

---

### 3. incomes
| カラム | 型 | 制約 | 説明 |
|---|---|---|---|
| id | TEXT | PK | UUID |
| date | TEXT | NOT NULL | YYYY-MM-DD |
| amount | INTEGER | NOT NULL | 臨時収入金額 |
| memo | TEXT | NULL | メモ |
| created_at | TEXT | NOT NULL | 作成日時 |
| updated_at | TEXT | NOT NULL | 更新日時 |

インデックス:
- idx_incomes_date (date)

---

### 4. categories
| カラム | 型 | 制約 | 説明 |
|---|---|---|---|
| id | TEXT | PK | UUID |
| name | TEXT | NOT NULL | 名称 |
| parent_id | TEXT | NULL | 親カテゴリのid |
| created_at | TEXT | NOT NULL | 作成日時 |
| updated_at | TEXT | NOT NULL | 更新日時 |

インデックス:
- idx_categories_parent (parent_id)

---

### 5. templates
| カラム | 型 | 制約 | 説明 |
|---|---|---|---|
| id | TEXT | PK | UUID |
| name | TEXT | NOT NULL | テンプレ名 |
| category_id | TEXT | NOT NULL | categories.id |
| sub_category_id | TEXT | NULL | categories.id |
| amount | INTEGER | NOT NULL | 固定金額 |
| created_at | TEXT | NOT NULL | 作成日時 |
| updated_at | TEXT | NOT NULL | 更新日時 |

インデックス:
- idx_templates_category (category_id)

---

### 6. category_orders
| カラム | 型 | 制約 | 説明 |
|---|---|---|---|
| id | TEXT | PK | UUID |
| category_id | TEXT | NOT NULL, UNIQUE | categories.id |
| parent_id | TEXT | NULL | 並び順のスコープ（NULL=親カテゴリ、値あり=サブカテゴリ） |
| display_order | INTEGER | NOT NULL | 表示順序（0以上の整数） |
| created_at | TEXT | NOT NULL | 作成日時 |
| updated_at | TEXT | NOT NULL | 更新日時 |

インデックス:
- UNIQUE(category_id)
- idx_category_orders_parent (parent_id)
- idx_category_orders_display (parent_id, display_order)

---

## リレーション
- categories.parent_id -> categories.id (サブカテゴリ)
- expenses.category_id -> categories.id
- expenses.sub_category_id -> categories.id
- templates.category_id -> categories.id
- templates.sub_category_id -> categories.id
- category_orders.category_id -> categories.id (UNIQUE)
- category_orders.parent_id -> categories.id (親カテゴリと同じ値、またはNULL)

---

## 制約と補足

### カテゴリの階層構造
- categoriesは親を持たないもの（parent_id = NULL）がカテゴリ
- parent_idを持つものがサブカテゴリ
- カテゴリ（parent_id = NULL）は最大10件
- サブカテゴリは各カテゴリごとに最大10件
- 例: 「食費」（カテゴリ）→「食事」「飲料」「その他」（サブカテゴリ）

### カテゴリの並び順管理
- カテゴリの並び順はcategory_ordersテーブルで管理
- parent_idで並び順のスコープを管理（NULL=親カテゴリ間、値あり=サブカテゴリ間）
- display_orderで表示順を制御（小さい値ほど上に表示）
- カテゴリ新規作成時は既存の最大display_order + 1で追加
- category_ordersにレコードがない場合は、categories.created_atの昇順で表示
- 並び替え時はparent_idが同じカテゴリのみ対象とする

### カテゴリ数上限制約の実装
- 10件制約はData層（DB層またはRepository層）で検証する
- UI層でも事前チェックを実施し、ユーザーに分かりやすいエラーを表示
- 仕様変更に柔軟に対応できるよう、制約値は定数化する

### 未分類支出の管理
- expensesのis_uncategorizedはcategory_idがNULLのとき自動的に1
- アプリケーション層での保存時に自動設定
- ウィジェットからの入力時はcategory_id = NULL & is_uncategorized = 1で保存
- 月次サマリでカテゴリ付けした後はis_uncategorized = 0に更新

### その他のデータ制約
- templatesはカテゴリ必須、サブカテゴリ任意
- expensesはカテゴリ未設定の保存を許可する（未分類フラグで管理）
- テンプレも最大10件まで
- 月次集計はdateのYYYY-MMでフィルタする
