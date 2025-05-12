import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import config.loadConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import controller.signApk
import ui.apkSignerUI
import java.awt.GraphicsEnvironment
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.io.File

/**
 * 签名工具首页界面
 *
 * @author 杨耿雷
 * @since 2025/5/7 14:48
 */

// ----------------------------------------------
//  自定义主题（清新蓝绿配色方案）
// ----------------------------------------------
private val FreshColors = lightColors(
    primary = Color(0xFF00897B),
    primaryVariant = Color(0xFF00695C),
    secondary = Color(0xFF26A69A),
    background = Color(0xFFE0F2F1),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF004D40),
    onSurface = Color.Black
)

fun main() = application {
    // 启动应用程序
    println("Application Starting...")

    // 读取外部配置
    val config = remember { mutableStateOf(loadConfig()) }
    var apkFile by remember { mutableStateOf<File?>(null) }
    val logLines = remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()

    // 默认窗口大小
    val state = rememberWindowState(size = DpSize(1000.dp, 700.dp))

    Window(
        onCloseRequest = ::exitApplication,
        title = "自动签名工具",
        state = state,
        icon = painterResource("android_icon.png") // 放在 resources 目录下
    ) {

        // ⚠️ Jetpack Compose for Desktop 在不同 DPI 屏幕之间拖动窗口时，存在 UI 错位的已知 Bug
        // 相关的 Bug 讨论链接：
        // - https://youtrack.jetbrains.com/issue/CMP-6857
        // - https://github.com/JetBrains/compose-multiplatform/issues/3685
        //
        // 官方尚未修复此问题，因此不得已先采用监听窗口位置变化并调用 repaint() 的方式临时解决
        var lastScreenHash by remember { mutableStateOf<Int?>(null) }

        window.addComponentListener(object : ComponentAdapter() {

            override fun componentMoved(e: ComponentEvent?) {
                super.componentMoved(e)
                // 判断窗口当前所在屏幕是否发生了变化（根据 screen bounds 判断）
                val currentScreen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .screenDevices
                    .firstOrNull {
                        it.defaultConfiguration.bounds.contains(window.location)
                    }

                val newHash = currentScreen?.hashCode()
                if (newHash != lastScreenHash) {
                    lastScreenHash = newHash
                    window.repaint()
                }
            }
        })
        // 设置 UI 主题
        MaterialTheme(colors = FreshColors) {
            apkSignerUI(
                apkFile = apkFile,
                logLines = logLines.value,
                onSelectFile = { file ->
                    apkFile = file
                    logLines.value += "选中：${file.name}"
                },
                onSignClicked = {
                    scope.launch(Dispatchers.IO) {
                        signApk(apkFile, config.value, logLines)
                    }
                },
                onResetThePage = {
                    apkFile = null
                    logLines.value = emptyList()
                    config.value = loadConfig()
                },
                config = config.value,
                window = window
            )
        }
    }
}