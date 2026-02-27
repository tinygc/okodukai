package com.tinygc.okodukai.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Now in Android スタイルの角丸定義
 * 大きな角丸でモダンな印象を与える
 */
val AppShapes = Shapes(
    // Extra Small - 小さなチップやタグ
    extraSmall = RoundedCornerShape(4.dp),
    // Small - 小さなボタンやアイコンボタン
    small = RoundedCornerShape(8.dp),
    // Medium - 通常のボタンやカード
    medium = RoundedCornerShape(16.dp),
    // Large - 大きなカードやダイアログ
    large = RoundedCornerShape(20.dp),
    // Extra Large - トップレベルコンテナ
    extraLarge = RoundedCornerShape(28.dp)
)
