# 🛍️ おこづかい - Android家計簿アプリ

Androidで使える、シンプルでパワフルな家計簿アプリ。支出管理、予算設定、テンプレート機能で、おこづかいの管理がもっと簡単に。

![GitHub](https://img.shields.io/badge/GitHub-tinygc-blue?logo=github)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-purple?logo=kotlin)
![Android](https://img.shields.io/badge/Android-14%2B-green?logo=android)

---

## ✨ 機能一覧

### 🎯 Phase 1 & 2 実装完了（2026-02-07）

| 機能 | 状態 | 説明 |
|------|------|------|
| **予算管理** | ✅ | 月ごとの予算設定、残額表示 |
| **支出記録** | ✅ | 日付・金額・カテゴリで支出を記録、編集・削除対応 |
| **カテゴリ管理** | ✅ | 親カテゴリ・サブカテゴリの自由な設定（最大10個まで） |
| **月間サマリー** | ✅ | 支出の集計、カテゴリ別の内訳可視化 |
| **一時収入記録** | ✅ | 月間収支の補正用 |
| **テンプレート機能** | ✅ | よく使う支出をテンプレート化、ワンタップで記録 |
| **月別履歴** | ✅ | 過去の月の支出・予算を閲覧 |

### 🚀 Phase 3（計画中）
- スマートウォッチ対応（Wear OS）
- ホームスクリーン用ウィジェット

---

## 📱 実装済みスクリーン

1. **支出入力画面（ExpenseEntry）** - 日々の支出を素早く記録
2. **月間サマリー画面（MonthlySummary）** - 支出の内訳を可視化
3. **予算設定画面（BudgetSetting）** - 月の予算を管理
4. **収入管理画面（IncomeManagement）** - 臨時収入を記録
5. **カテゴリ管理画面（CategoryManagement）** - カテゴリを自由に構成
6. **テンプレート管理画面（TemplateManagement）** - よく使う支出をテンプレート化
7. **月別履歴画面（MonthlyHistory）** - 過去の支出を確認

---

## 🏗️ アーキテクチャ

Clean Architecture に従った3層構成：

```
┌─────────────────────────────────────┐
│   Presentation Layer                │
│   (7 Screens, 7 ViewModels)         │
├─────────────────────────────────────┤
│   Domain Layer                      │
│   (23 UseCases, 5 Repositories)     │
├─────────────────────────────────────┤
│   Data Layer                        │
│   (Room DB, 5 DAOs, 5 Entities)     │
└─────────────────────────────────────┘
```

### 技術スタック

- **言語**: Kotlin 1.9.24
- **UI**: Jetpack Compose
- **DB**: Room + Kotlin Coroutines
- **DI**: Hilt
- **テスト**: JUnit 4 + kotlinx-coroutines-test

---

## 🧪 テスト

✅ **62 テストケース実装済み**（Build Success）

### テストカバレッジ

| レイヤー | テスト数 | 対象機能 |
|---------|--------|---------|
| Domain（UseCase） | 40 | 予算・支出・カテゴリ・サマリー・収入・テンプレート |
| Presentation（ViewModel） | 14 | 予算設定・月間サマリー |
| Example Tests | 8 | Android Instrumented + Unit Test例 |

### テスト実行

```bash
# ユニットテスト実行
./gradlew test

# 完全なビルド＆テスト
./gradlew build
```

---

## 📋 要件定義

- 📄 [requirement/requirements.md](requirement/requirements.md)
  - FR-1: 予算設定・管理
  - FR-2: 支出記録・CRUD
  - FR-3: カテゴリ管理
  - FR-4: 月間集計・可視化
  - FR-6: 一時収入記録
  - FR-9: テンプレート機能

---

## 🎨 設計資料

- 📐 [architecture/architecture.md](architecture/architecture.md) - 全体設計
- 📊 [architecture/data-model.md](architecture/data-model.md) - DB設計
- 🖼️ [architecture/ui-spec.md](architecture/ui-spec.md) - UI仕様

---

## 🛠️ セットアップ

### 前提条件

- Android Studio Hedgehog以降
- Java 11以上
- Android SDK 35以上

### インストール

```bash
# リポジトリをクローン
git clone https://github.com/tinygc/okodukai.git
cd okodukai

# Gradle同期
./gradlew clean build

# エミュレータで実行
./gradlew installDebug
```

### GitHub Actionsで正式署名リリースを作る

Play Console提出用の署名付きAPK/AABは、[.github/workflows/release-build.yml](.github/workflows/release-build.yml) で自動作成できます。

1. GitHub Secretsを登録
  - `OKODUKAI_KEYSTORE_BASE64`: keystoreファイルをBase64化した値
  - `OKODUKAI_STORE_PASSWORD`: keystoreのパスワード
  - `OKODUKAI_KEY_ALIAS`: キーエイリアス
  - `OKODUKAI_KEY_PASSWORD`: キーのパスワード

2. keystoreをBase64化（ローカル実行例）

```bash
base64 -w 0 okodukai-release.jks
```

3. Actions実行
  - GitHubのActionsタブから `Release Build` を手動実行
  - または `v0.1.1` のようなタグをpushして自動実行

4. バージョンルール
  - `versionCode`: GitHub Actionsの`run_number`で自動インクリメント
  - `versionName`: タグpush時はタグ名（先頭`v`除去）を使用
  - 手動実行時は入力`version_name`（未指定なら`0.1.1`）を使用

5. 出力物
  - Actions Artifact: 署名済み APK / AAB
  - GitHub Release: タグに紐づくAPK/AAB添付

ローカルで署名付きリリースを作る場合は `implementation/local.properties` に以下を設定してください。

```properties
OKODUKAI_STORE_FILE=../okodukai-release.jks
OKODUKAI_STORE_PASSWORD=your_store_password
OKODUKAI_KEY_ALIAS=your_key_alias
OKODUKAI_KEY_PASSWORD=your_key_password
```

---

## 📊 プロジェクト進捗

```
Phase 1 & 2: ████████████████████ 100%
├─ スクリーン実装: ✅ 7/7
├─ UseCase実装: ✅ 23/23
├─ テスト実装: ✅ 62/62
└─ 警告・エラー: ✅ 0/0

Phase 3: ░░░░░░░░░░ 0% (計画中)
└─ Wear OS対応・ウィジェット
```

---

## 🎯 次のステップ

### Priority 1: デバイステスト（実装検証）
- [ ] APKビルド＆デバイスインストール
- [ ] 実機での動作確認
- [ ] パフォーマンス計測

### Priority 2: テスト拡充
- [ ] UI自動テスト（Compose Test）
- [ ] ViewModel追加テスト（5スクリーン分）
- [ ] システムテストドキュメント

### Priority 3: Phase 3機能
- [ ] Wear OS対応
- [ ] ホームスクリーンウィジェット

---

## 📝 開発ノート

### 実装時の工夫

- **Clean Architecture** で層を明確に分離
- **FakeRepository パターン** でテスト駆動開発（TDD）を実施
- **Kotlin Coroutines** で非同期処理を簡潔に
- **Room + Flow** でリアルタイムデータ更新

### 既知の事項

- ビルド警告: AutoMirroredアイコン、HorizontalDivider（Compose Compiler対応待ち）
- ViewModel テスト: `advanceTimeBy()` で 3 秒の delay を回避

---

## 👨‍💻 開発者

**tinygc**
- GitHub: [@tinygc](https://github.com/tinygc)
- Email: tinygc404@gmail.com

---

## 📄 ライセンス

このプロジェクトはMITライセンスの下で公開されています。

---

## 🔗 リンク

- [GitHub Repository](https://github.com/tinygc/okodukai)
- [要件定義書](requirement/requirements.md)
- [設計資料](architecture/)
- [テスト結果](temp.md)

---

最後に更新: **2026年2月7日** (Phase 1 & 2 完了)
