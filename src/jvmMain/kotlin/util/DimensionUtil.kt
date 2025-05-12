package util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.awt.GraphicsEnvironment
import java.awt.Toolkit

/**
 * 封装用于处理不同单位之间的转换，支持从像素（px）、dp、毫米（mm）、英寸（inches）等单位之间的转换。
 *
 * @author 杨耿雷
 * @since 2025/5/9 13:42
 */

/**
 * 获取设备屏幕的缩放比例（dpi）
 */
val screenScale: Float
    get() = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .defaultScreenDevice.defaultConfiguration.defaultTransform.scaleX.toFloat()

/**
 * 将像素（px）转换为dp（密度无关像素）
 */
fun pxToDp(px: Float): Dp {
    return (px / screenScale).dp
}

/**
 * 将dp（密度无关像素）转换为像素（px）
 */
fun dpToPx(dp: Dp): Float {
    return dp.value * screenScale
}

/**
 * 将dp（密度无关像素）转换为毫米（mm）
 */
fun dpToMm(dp: Dp): Float {
    return dpToPx(dp) / dpiToMm()
}

/**
 * 将毫米（mm）转换为dp（密度无关像素）
 */
fun mmToDp(mm: Float): Dp {
    return pxToDp(mm * dpiToMm())
}

/**
 * 将dp（密度无关像素）转换为英寸（inches）
 */
fun dpToInches(dp: Dp): Float {
    return dpToPx(dp) / dpiToInches()
}

/**
 * 将英寸（inches）转换为dp（密度无关像素）
 */
fun inchesToDp(inches: Float): Dp {
    return pxToDp(inches * dpiToInches())
}

/**
 * 获取DPI（每英寸点数），用于单位转换
 */
private fun dpiToMm(): Float {
    val dpi = Toolkit.getDefaultToolkit().screenResolution
    return dpi / 25.4f  // 1英寸=25.4毫米
}

/**
 * 获取DPI（每英寸点数），用于单位转换
 */
private fun dpiToInches(): Float {
    return Toolkit.getDefaultToolkit().screenResolution.toFloat()
}