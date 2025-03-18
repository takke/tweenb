package jp.takke.tweenb.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import jp.takke.tweenb.app.repository.BlueskyClient
import jp.takke.tweenb.app.repository.PostRepository
import jp.takke.tweenb.app.util.LoggerWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 投稿関連の状態と機能を管理するViewModel
 */
class PublishViewModel(
  private val blueskyClient: BlueskyClient
) : ViewModel() {

  // ロガー
  private val logger = LoggerWrapper("PublishViewModel")

  /**
   * UIの状態
   */
  data class UiState(
    // 入力中のテキスト
    val currentInputText: String = "",
    // 投稿確認中のテキスト
    val pendingPostText: String = "",
    // 投稿中かどうか
    val isPosting: Boolean = false,
  )

  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  // 投稿確認ダイアログの表示状態
  var showPostConfirmDialog by mutableStateOf(false)
    private set

  /**
   * 入力テキストを更新する
   */
  fun updateInputText(text: String) {
    _uiState.update {
      it.copy(currentInputText = text)
    }
  }

  /**
   * 投稿前の確認ダイアログを表示する
   */
  fun showPostConfirmation(text: String) {
    if (!blueskyClient.isInitialized()) {
      logger.w("Blueskyクライアントが初期化されていません")
      throw IllegalStateException("Blueskyクライアントが初期化されていません")
    }

    _uiState.update {
      it.copy(pendingPostText = text)
    }
    showPostConfirmDialog = true
  }

  /**
   * 投稿確認ダイアログを閉じる
   */
  fun dismissPostConfirmDialog() {
    showPostConfirmDialog = false
    _uiState.update {
      it.copy(pendingPostText = "")
    }
  }

  /**
   * 投稿確認ダイアログをキャンセルする
   * キャンセル時は入力欄を元に戻す
   */
  fun cancelPostConfirmDialog() {
    showPostConfirmDialog = false
    _uiState.update {
      it.copy(pendingPostText = "")
    }
  }

  /**
   * 投稿完了後の処理
   */
  fun completePost() {
    // 投稿完了後、入力欄をクリア
    _uiState.update {
      it.copy(currentInputText = "")
    }
    dismissPostConfirmDialog()
  }

  /**
   * 投稿を作成する
   * @return 投稿が成功したかどうか
   */
  suspend fun createPost(text: String): Boolean {
    if (!blueskyClient.isInitialized()) {
      logger.w("Blueskyクライアントが初期化されていません")
      return false
    }

    try {
      // 投稿開始
      logger.i("投稿開始: $text")

      _uiState.update {
        it.copy(isPosting = true)
      }

      // 投稿処理
      val postRepository = PostRepository.getInstance(blueskyClient)
      val success = postRepository.createPost(text)

      if (success) {
        logger.i("投稿成功")
      } else {
        logger.e("投稿失敗")
      }

      _uiState.update {
        it.copy(isPosting = false)
      }

      return success
    } catch (e: Exception) {
      logger.e("投稿エラー: ${e.message}", e)

      _uiState.update {
        it.copy(isPosting = false)
      }

      return false
    }
  }
} 