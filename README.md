# tweenb

tweenbは、Jetpack Composeを使用したKotlinデスクトップアプリケーションです。

Windows用のTween/OpenTweenに似た使用感を目指して作成しています。

## 機能

- タブベースのインターフェース
- ウィンドウサイズと位置の記憶
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


## プロジェクト構成

```
tweenb/
├── app/                    # アプリケーションモジュール
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/     # Kotlinソースコード
│   │   │   └── resources/  # リソースファイル
│   │   └── test/           # テストコード
│   └── build.gradle.kts    # アプリモジュールのビルド設定
├── build.gradle.kts        # プロジェクトのビルド設定
└── settings.gradle.kts     # Gradleの設定
```

## ライセンス

Apache-2.0 license
