# tweenb

tweenbは、Jetpack Composeを使用したKotlinデスクトップアプリケーションです。

Windows用のTween/OpenTweenに似た使用感を目指して作成しています。

## 機能

- タブベースのインターフェース
- ウィンドウサイズと位置の記憶
- Blueskyアカウントの管理と認証
- カスタマイズ可能なカラムレイアウト（幅の調整と保存）
- ユーザーアイコン表示

## インストール方法

### リリースからのインストール

GitHubのリリースページから以下のいずれかをダウンロードしてください：

- **MSIインストーラー**: インストーラーを実行してアプリケーションをインストールします。
- **ZIPパッケージ**: ZIPファイルを任意の場所に展開して使用します。インストール不要で実行できます。

### 前提条件（開発者向け）

- JDK 17以上
- Gradle 8.13以上

### ビルド方法

```bash
./gradlew :app:build
```

### 実行方法

```bash
./gradlew :app:run
```

### Windows用パッケージの作成

#### MSIインストーラーの作成

```bash
./gradlew :app:packageMsi
```

生成されたMSIファイルは `app/build/compose/binaries/main/msi/` ディレクトリに保存されます。

#### ZIPパッケージの作成

```bash
./gradlew :app:createZipDistribution
```

生成されたZIPファイルは `app/build/compose/binaries/main/zip/` ディレクトリに保存されます。

## CI

このプロジェクトはGitHub Actionsを使用してCIを実現しています。

### 自動ビルド

メインブランチへのプッシュやプルリクエストが作成されると、自動的にWindows用のビルドが実行されます。ビルド結果はGitHub Actionsのアーティファクトとして保存されます。

### 自動リリース

`v*` 形式のタグ（例：`v1.0.0`）をプッシュすると、自動的にリリースが作成され、Windows用のMSIインストーラーとZIPパッケージがアップロードされます。

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
| columns.layout | カラム情報（JSON形式） |

## カスタマイズ機能

### カラムレイアウト

タイムラインのカラムレイアウトはカスタマイズ可能です：

- ヘッダー部分の区切り線をドラッグすることで各カラムの幅を調整できます
- 調整したカラム幅は自動的に保存され、次回起動時に復元されます
- カラムの種類（アイコン、名前、投稿内容、日時）ごとに幅を個別に設定可能

### ユーザーインターフェース

- スクロールバーによる長いタイムラインの快適なナビゲーション
- ユーザーアイコンの表示（Coil3ライブラリを使用）
- ドラッグ可能な区切り線上でのカーソル変更によるユーザビリティ向上

## プロジェクト構成

```
tweenb/
├── app/                                              # アプリケーションモジュール
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/                               # Kotlinソースコード
│   │   │   │   └── jp/takke/tweenb/app/
│   │   │   │       ├── compose/                      # Compose UI コンポーネント
│   │   │   │       ├── domain/                       # ドメイン層
│   │   │   │       ├── repository/                   # データ層
│   │   │   │       ├── service/                      # サービス層
│   │   │   │       ├── util/                         # ユーティリティ層
│   │   │   │       ├── viewmodel/                    # プレゼンテーション層
│   │   │   └── resources/                            # リソースファイル
│   │   └── test/                                     # テストコード
│   └── build.gradle.kts                              # アプリモジュールのビルド設定
├── build.gradle.kts                                  # プロジェクトのビルド設定
├── gradle/
│   └── libs.versions.toml                            # 依存関係のバージョン管理
├── .github/
│   └── workflows/                                    # GitHub Actions ワークフロー
└── settings.gradle.kts                               # Gradle設定
```

## アーキテクチャ

アプリケーションはMVVMアーキテクチャに基づいて設計されています：

- **ドメイン層** - ビジネスロジックとモデル
  - `Account` - Blueskyアカウント情報
  - `BlueskyAuthService` - 認証処理
  - `BlueskyClient` - API通信
  - `ColumnInfo` - カラム情報モデル

- **データ層** - データアクセスと永続化
  - `AppPropertyRepository` - ウィンドウ設定とカラム情報の管理
  - `AccountRepository` - アカウント情報の管理
  - `TimelineRepository` - タイムライン情報の管理

- **プレゼンテーション層** - UI関連
  - `AppViewModel` - UIの状態管理
  - Compose UI - 画面表示
    - `PostItem` - 投稿アイテム表示
    - `PostListContent` - 投稿リスト表示

## 使用ライブラリ

- JetBrains Compose - UI構築
- kotlinx.coroutines - 非同期処理
- kotlinx.serialization - JSONシリアライゼーション
- Lifecycle - ライフサイクル管理
- kbsky - Bluesky API クライアント
- Cryptography - 暗号化ライブラリ
- Coil3 - 画像読み込みと表示

## ライセンス

Apache-2.0 license
