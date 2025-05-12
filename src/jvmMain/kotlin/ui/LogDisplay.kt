package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily

/**
 * 日志区域
 *
 * @author 杨耿雷
 * @since 2025/5/8 11:39
 */
@Composable
fun logDisplay(logLines: List<String>) {
    val text = logLines.joinToString("\n")

    // 创建一个滚动状态
    val scrollState = rememberScrollState()

    // 使用 LaunchedEffect 监听 logLines 的变化并滚动到底部
    LaunchedEffect(logLines) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(Color(0xFFF3F3F3)) // 柔和背景色
            .verticalScroll(scrollState) // 添加滚动效果
    ) {
        BasicTextField(
            value = text,
            onValueChange = { },
            readOnly = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 24.sp, // 更宽的行距
                color = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )
    }
}