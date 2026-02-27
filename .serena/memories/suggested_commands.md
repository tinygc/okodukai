# おこづかいアプリ - 推奨コマンド集

## プロジェクトディレクトリ
```
cd c:\Users\tinyg\Okodukai\implementation
```

## ビルド関連

### クリーンビルド
```powershell
.\gradlew.bat clean build
```

### デバッグAPKビルド
```powershell
.\gradlew.bat assembleDebug
# 出力: app\build\outputs\apk\debug\app-debug.apk
```

### リリースAPKビルド
```powershell
.\gradlew.bat assembleRelease
# 出力: app\build\outputs\apk\release\app-release.apk
```

### 依存関係確認
```powershell
.\gradlew.bat dependencies
```

## テスト関連

### 全ユニットテスト実行
```powershell
.\gradlew.bat test
# レポート: app\build\reports\tests\testDebugUnitTest\index.html
```

### 特定クラスのテスト実行
```powershell
.\gradlew.bat test --tests "com.tinygc.okodukai.domain.usecase.budget.SaveBudgetUseCaseTest"
```

### 全UIテスト実行（実機またはエミュレータ起動必須）
```powershell
.\gradlew.bat connectedAndroidTest
# レポート: app\build\reports\androidTests\connected\index.html
```

### テストレポート閲覧（Windows）
```powershell
start app\build\reports\tests\testDebugUnitTest\index.html
```

## コード品質

### Kotlinコンパイル
```powershell
.\gradlew.bat compileDebugKotlin
```

### KSP（Room/Hilt生成コード）確認
```powershell
.\gradlew.bat kspDebugKotlin
# 生成コード: app\build\generated\ksp\debug\kotlin
```

### 生成コード確認（Hilt）
Hilt生成コードの場所:
```
app\build\generated\hilt\component_sources\debug\...
```

### 生成コード確認（Room DAO実装）
Room生成コードの場所:
```
app\build\generated\ksp\debug\kotlin\com\tinygc\okodukai\data\dao
```

## 実機/エミュレータ

### 接続デバイス確認
```powershell
adb devices
```

### デバッグインストール
```powershell
.\gradlew.bat installDebug
```

### アプリ起動
```powershell
adb shell am start -n com.tinygc.okodukai/.MainActivity
```

### ログ確認
```powershell
adb logcat | Select-String "OkodukaiApp"
```

### データベース確認（実機でRootedの場合）
```powershell
adb shell
cd /data/data/com.tinygc.okodukai/databases
sqlite3 okodukai.db
.tables
.schema expenses
```

## Git関連（Windows）

### 現在のブランチ確認
```powershell
git branch
```

### 変更確認
```powershell
git status
```

### コミット
```powershell
git add .
git commit -m "コミットメッセージ"
```

### プッシュ
```powershell
git push origin main
```

### ログ確認
```powershell
git log --oneline -10
```

## ファイル操作（Windows PowerShell）

### ディレクトリ一覧
```powershell
Get-ChildItem -Path . -Directory
```

### ファイル検索
```powershell
Get-ChildItem -Path . -Recurse -Filter "*.kt" | Select-String "ExpenseRepository"
```

### ファイル内容表示
```powershell
Get-Content .\app\src\main\java\com\tinygc\okodukai\MainActivity.kt
```

### ファイル編集（VS Code起動）
```powershell
code .
```

## プロジェクト管理

### タスク一覧
```powershell
.\gradlew.bat tasks
```

### クリーンアップ
```powershell
.\gradlew.bat clean
```

### キャッシュクリア
```powershell
.\gradlew.bat --stop
Remove-Item -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Force
```

## トラブルシューティング

### Gradle Daemonを停止
```powershell
.\gradlew.bat --stop
```

### ビルドキャッシュクリア
```powershell
.\gradlew.bat clean
.\gradlew.bat cleanBuildCache
```

### KSP生成コードを再生成
```powershell
.\gradlew.bat clean kspDebugKotlin
```

### 完全リビルド
```powershell
.\gradlew.bat clean
.\gradlew.bat build --no-build-cache
```

## 開発時のチェックリスト

### コード変更後の確認手順
1. ビルドエラーがないか確認
   ```powershell
   .\gradlew.bat compileDebugKotlin
   ```

2. ユニットテスト実行
   ```powershell
   .\gradlew.bat test
   ```

3. APKビルド
   ```powershell
   .\gradlew.bat assembleDebug
   ```

4. 実機インストール
   ```powershell
   .\gradlew.bat installDebug
   ```

### 機能実装完了時
1. 全テスト実行
   ```powershell
   .\gradlew.bat test
   ```

2. テストレポート確認
   ```powershell
   start app\build\reports\tests\testDebugUnitTest\index.html
   ```

3. ドキュメント更新（README.md, temp.md）

4. コミット・プッシュ
   ```powershell
   git add .
   git commit -m "実装完了: [機能名]"
   git push origin main
   ```
