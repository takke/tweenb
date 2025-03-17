# tweenb

tweenbは、Jetpack Composeを使用したKotlinデスクトップアプリケーションです。

Windows用のTween/OpenTweenに似た使用感を目指して作成しています。

## 機能

- タブベースのインターフェース
- ウィンドウサイズと位置の記憶
- Blueskyアカウントの管理と認証
- TODO: その他の機能を追加

## インストール方法

### 前提条件

- JDK 11以上
- Gradle 7.0以上

### ビルド方法

```bash
./gradlew :app:build
```

### 実行方法

```bash
./gradlew :app:run
```

## 設定ファイル

tweenbは、ユーザー設定をプロパティファイルとして保存します。

### 設定ファイルの場所

設定ファイルは、ユーザーのホームディレクトリに保存されます：

```
${user.home}/.tweenb.properties
```

### 設定項目

| キー | 説明 |
|-----|-----|
| window.x | ウィンドウのX座標位置 |
| window.y | ウィンドウのY座標位置 |
| window.width | ウィンドウの幅 |
| window.height | ウィンドウの高さ |
| accounts | Blueskyアカウント情報（JSON形式） |


## プロジェクト構成

```
tweenb/
├── app/                                # アプリケーションモジュール
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/                 # Kotlinソースコード
│   │   │   │   └── jp/takke/tweenb/app/
│   │   │   │       ├── domain/         # ドメイン層
│   │   │   │       │   ├── Account.kt  # アカウントモデル
│   │   │   │       │   ├── BlueskyAuthService.kt # 認証サービス
│   │   │   │       │   └── BlueskyClient.kt # APIクライアント
│   │   │   │       ├── repository/     # データ層
│   │   │   │       │   └── AppPropertyRepository.kt # 設定リポジトリ
│   │   │   │       ├── viewmodel/      # プレゼンテーション層
│   │   │   │       │   └── AppViewModel.kt # ビューモデル
│   │   │   │       └── AppConstants.kt # アプリ定数
│   │   │   └── resources/              # リソースファイル
│   │   └── test/                       # テストコード
│   └── build.gradle.kts                # アプリモジュールのビルド設定
├── build.gradle.kts                    # プロジェクトのビルド設定
├── gradle/
│   └── libs.versions.toml              # 依存関係のバージョン管理
└── settings.gradle.kts                 # Gradleの設定
```

## アーキテクチャ

アプリケーションはMVVMアーキテクチャに基づいて設計されています：

- **ドメイン層** - ビジネスロジックとモデル
  - `Account` - Blueskyアカウント情報
  - `BlueskyAuthService` - 認証処理
  - `BlueskyClient` - API通信

- **データ層** - データアクセスと永続化
  - `AppPropertyRepository` - 設定とアカウント情報の管理

- **プレゼンテーション層** - UI関連
  - `AppViewModel` - UIの状態管理
  - Compose UI - 画面表示

## 使用ライブラリ

- Jetpack Compose - UI構築
- kotlinx.coroutines - 非同期処理
- kotlinx.serialization - JSONシリアライゼーション
- kbsky - Bluesky API クライアント

## ライセンス

Apache-2.0 license
