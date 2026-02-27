# おこづかいアプリ - プロジェクト概要

## プロジェクトの目的
Androidで使用可能なおこづかい管理アプリ。サラリーマンが月単位で自由費（おこづかい）を管理し、カテゴリ別支出を把握できるシンプルなアプリ。

**ターゲットユーザー**: サラリーマン（20-60代）
**主目的**: 月単位でのおこづかい予算管理と支出の可視化

## 技術スタック

### 言語・フレームワーク
- **言語**: Kotlin 1.9.24
- **UI**: Jetpack Compose (Compose BOM 2024.12.01, Compiler 1.5.14)
- **アーキテクチャ**: Clean Architecture (Presentation / Domain / Data層)
- **パターン**: MVVM (ViewModel + Compose UI)

### 主要ライブラリ
- **Room**: 2.6.1 (ローカルデータベース - SQLite)
- **Hilt**: 2.51 (依存性注入)
- **Navigation Compose**: 2.8.5
- **Lifecycle**: 2.8.7
- **Coroutines**: 1.9.0
- **KSP**: 1.9.24-1.0.20

### ビルドツール
- **Android Gradle Plugin**: 8.7.3
- **Gradle**: 8.9 (wrapper)
- **最小SDK**: 27 (Android 8.1)
- **ターゲットSDK**: 36 (Android 14)
- **コンパイルSDK**: 36

## プロジェクト構造

```
implementation/
  app/
    src/
      main/
        java/com/tinygc/okodukai/
          data/                 - Repository実装、Room DAOs、Entities
          di/                   - Hilt DIモジュール
          domain/               - Use Cases、Repository Interfaces
          presentation/         - Compose Screens、ViewModels
          MainActivity.kt
          OkodukaiApplication.kt
      androidTest/              - UI Tests (Compose UI Test)
      test/                     - Unit Tests
    build.gradle.kts            - アプリレベル設定
  build.gradle.kts              - プロジェクトレベル設定
  gradle/
    libs.versions.toml          - バージョンカタログ
  settings.gradle.kts

requirement/
  requirements.md              - 機能要件・非機能要件

architecture/
  architecture.md              - アーキテクチャ設計、画面構成
  data-model.md                - データモデル（5テーブル）
  ui-spec.md                   - UI仕様書（8画面）

test/                          - システムテストドキュメント（予定）

temp.md                        - 申し送り事項
README.md                      - プロジェクト概要
```

## データモデル（5テーブル）

1. **budgets** - 月ごとの予算
2. **expenses** - 支出（未分類フラグあり）
3. **incomes** - 臨時収入
4. **categories** - カテゴリ（親子関係対応、最大10個）
5. **templates** - テンプレ（最大10個）

## 主要機能（FR）

### Phase 1 & 2 実装完了（2026-02-07）
- FR-1: 月単位の予算管理 ✅
- FR-2: 支出記録（追加・編集・削除） ✅
- FR-3: カテゴリ・サブカテゴリ管理 ✅
- FR-4: 支出の可視化（カテゴリ別集計） ✅
- FR-5: 月ごとの履歴一覧 ✅
- FR-6: 臨時収入の記録 ✅
- FR-9: テンプレ管理 ✅

### Phase 3（計画中）
- FR-7: ウィジェットによる超クイック入力
- FR-8: Pixel Watch連携（優先度低、開発スコープ外）

## 実装済み画面（7画面）

1. **ExpenseEntryScreen** - 支出入力（通常/テンプレタブ）
2. **MonthlySummaryScreen** - 月次サマリー（集計・編集・削除）
3. **BudgetSettingScreen** - 予算設定
4. **IncomeManagementScreen** - 臨時収入管理
5. **CategoryManagementScreen** - カテゴリ管理（階層表示）
6. **TemplateManagementScreen** - テンプレ管理
7. **MonthlyHistoryScreen** - 月次履歴（12ヶ月分）

## テスト実装状況

### 62テストケース全PASS
- **Domain層 Unit Test**: 9ファイル（Use Cases）
- **Presentation層 ViewModel Test**: 2ファイル（14テストケース）
- **カバレッジ**: FR-1, FR-2, FR-3, FR-4, FR-6, FR-9

## 非機能要件（NFR）

- **NFR-1**: ローカルデータ永続化（Room + SQLite）
- **NFR-2**: パフォーマンス
  - 支出記録の追加/編集が1秒以内
  - 月ごとのグラフ表示が2秒以内
- **NFR-3**: ユーザビリティ
  - 直感的なUIで3タップ以内に支出を記録
  - ウィジェットから金額だけで5秒以内に登録

## ビルド状態

✅ BUILD SUCCESSFUL（警告0件）
✅ テスト62ケース全PASS
✅ Phase 1 & 2 実装完了
