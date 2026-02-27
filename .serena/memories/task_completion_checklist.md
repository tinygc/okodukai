# おこづかいアプリ - タスク完了時のチェックリスト

## 実装完了時の手順

### 1. コンパイル確認
```powershell
.\gradlew.bat compileDebugKotlin
```
- ビルドエラーがないことを確認
- 警告がある場合は可能な限り対応

### 2. ユニットテスト実行
```powershell
.\gradlew.bat test
```
- 全テストがPASSすることを確認
- 新規実装の場合、対応するテストケースを追加

### 3. テストレポート確認
```powershell
start app\build\reports\tests\testDebugUnitTest\index.html
```
- テストカバレッジを確認
- 失敗したテストケースの詳細を確認

### 4. APKビルド
```powershell
.\gradlew.bat assembleDebug
```
- APKが正常に生成されることを確認
- 出力先: `app\build\outputs\apk\debug\app-debug.apk`

### 5. ドキュメント更新

#### 更新すべきドキュメント
- **README.md**: 実装済み機能の状態を更新
- **temp.md**: 申し送り事項を更新
- **該当するUI仕様**: 実装時の微調整を反映
- **アーキテクチャドキュメント**: 構造変更があった場合

#### ドキュメント整合性確認
- [ ] 要件定義（requirement/requirements.md）との整合性
- [ ] アーキテクチャ設計（architecture/architecture.md）との整合性
- [ ] UI仕様（architecture/ui-spec.md）との整合性
- [ ] データモデル（architecture/data-model.md）との整合性

### 6. コード品質チェック

#### Clean Architectureの遵守
- [ ] Presentation -> Domain -> Dataの依存方向が守られている
- [ ] Data層のRepositoryがDomain層のインターフェースを実装している
- [ ] ViewModel がUse Caseを介してRepositoryにアクセスしている
- [ ] Entity（Room）とDomain Modelが適切に分離されている

#### 命名規則
- [ ] ファイル名がコーディング規約に従っている
- [ ] クラス名・関数名が適切である
- [ ] 定数が定数化されている（マジックナンバー排除）

#### エラーハンドリング
- [ ] Result型またはtry-catchでエラーハンドリングされている
- [ ] ViewModelでエラーメッセージが適切に処理されている
- [ ] UI仕様で定義されたエラーメッセージが使用されている

#### テストコード
- [ ] 対応するユニットテストが実装されている
- [ ] Given-When-Then構造で書かれている
- [ ] 主要な成功・失敗ケースがカバーされている

### 7. Git コミット

#### コミット前の確認
```powershell
git status
git diff
```

#### コミットメッセージ例
```
実装完了: [機能名] ([FR-x])

- [詳細1]
- [詳細2]
- テスト追加: [テストクラス名]
```

#### コミット・プッシュ
```powershell
git add .
git commit -m "実装完了: [機能名] ([FR-x])"
git push origin main
```

## 新機能実装時の追加手順

### 1. 要件確認
- [ ] 該当するFR（Functional Requirement）を確認
- [ ] 該当するNFR（Non-Functional Requirement）を確認
- [ ] UI仕様を確認（操作フロー、バリデーション、メッセージ）
- [ ] データモデルを確認（テーブル、カラム、制約）

### 2. アーキテクチャ設計確認
- [ ] 画面遷移フローを確認
- [ ] 必要なUse Caseを特定
- [ ] 必要なRepositoryメソッドを特定
- [ ] 必要なDAOメソッドを特定

### 3. 実装順序（推奨）
1. **Data層**: Entity, DAO, Repository実装
2. **Domain層**: Use Case, Repository Interface
3. **Presentation層**: ViewModel, UI State, Screen
4. **DI**: Hilt Module追加（必要な場合）
5. **Navigation**: 画面遷移追加（必要な場合）

### 4. TDD（テスト駆動開発）推奨
1. テストケースを先に書く
2. テストが失敗することを確認
3. 最小限の実装を追加
4. テストがPASSすることを確認
5. リファクタリング

## バグ修正時の手順

### 1. 問題の特定
- [ ] エラーログを確認
- [ ] 再現手順を明確化
- [ ] 該当するコードを特定

### 2. 修正
- [ ] 原因を特定
- [ ] 最小限の変更で修正
- [ ] 既存機能への影響を確認

### 3. テスト追加
- [ ] バグを再現するテストケースを追加
- [ ] 修正後にテストがPASSすることを確認

### 4. 回帰テスト
```powershell
.\gradlew.bat test
```
- 他の機能に影響がないことを確認

## パフォーマンステスト時の手順

### 1. 計測対象の明確化
- [ ] NFR-2の要件を確認
  - 支出記録の追加/編集が1秒以内
  - 月ごとのグラフ表示が2秒以内
  - ウィジェット入力が5秒以内

### 2. 実機での計測
- [ ] 実機またはエミュレータで動作確認
- [ ] ログで処理時間を計測
- [ ] ユーザー体感での確認

### 3. パフォーマンス改善（必要な場合）
- [ ] データベースクエリの最適化
- [ ] インデックスの追加
- [ ] 不要な処理の削減
- [ ] Flowの最適化

## リリース前の最終チェック

### 1. 全機能の動作確認
- [ ] 全画面の表示確認
- [ ] 全操作フローの確認
- [ ] エラーケースの確認

### 2. 全テストPASS
```powershell
.\gradlew.bat test
.\gradlew.bat connectedAndroidTest
```

### 3. ドキュメント最終確認
- [ ] README.mdの更新
- [ ] 各種ドキュメントの整合性確認
- [ ] temp.mdの申し送り事項更新

### 4. ビルド確認
```powershell
.\gradlew.bat clean
.\gradlew.bat assembleRelease
```

### 5. Git タグ作成
```powershell
git tag -a v1.0.0 -m "Phase 1 & 2 実装完了"
git push origin v1.0.0
```

## トラブルシューティング

### ビルドエラー時
1. クリーンビルド実行
   ```powershell
   .\gradlew.bat clean build
   ```

2. KSP生成コード再生成
   ```powershell
   .\gradlew.bat clean kspDebugKotlin
   ```

3. Gradle Daemon停止
   ```powershell
   .\gradlew.bat --stop
   ```

4. キャッシュクリア
   ```powershell
   Remove-Item -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Force
   ```

### テスト失敗時
1. エラーログ確認
2. テストレポート確認 (`app\build\reports\tests\...`)
3. 該当するテストケースを単独実行
4. 必要に応じてデバッグ

### 実機動作不具合時
1. ログ確認
   ```powershell
   adb logcat | Select-String "OkodukaiApp"
   ```

2. データベース状態確認
3. 再インストール
   ```powershell
   adb uninstall com.tinygc.okodukai
   .\gradlew.bat installDebug
   ```
