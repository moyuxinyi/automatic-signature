package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import config.SignConfig
import controller.chooseApkFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Window
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.io.File

/**
 * 签名工具主界面的Ui结构
 *
 * @author 杨耿雷
 * @since 2025/5/7 15:12
 */
@Composable
fun apkSignerUI(
    apkFile: File?,
    logLines: List<String>,
    onSelectFile: (File) -> Unit,
    onSignClicked: () -> Unit,
    onResetThePage: () -> Unit,
    config: SignConfig,
    window: Window
) {

    val scope = rememberCoroutineScope()

    // 拖拽支持
    LaunchedEffect(window) {
        window.dropTarget = object : DropTarget() {
            override fun drop(event: DropTargetDropEvent) {
                event.acceptDrop(DnDConstants.ACTION_COPY)
                val data = event.transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>
                val file = data?.firstOrNull() as? File
                file?.let {
                    if (file.extension.equals("apk", true)) {
                        onSelectFile(file)
                    }
                }
                event.dropComplete(true)
            }
        }
    }

    Surface(color = MaterialTheme.colors.background, modifier = Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // 左侧：文件与操作
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = 6.dp,
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier.width(300.dp).fillMaxHeight()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("文件操作", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .border(1.dp, MaterialTheme.colors.primaryVariant, RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colors.background, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(apkFile?.name ?: "可拖入APK文件", color = MaterialTheme.colors.onSurface)
                    }
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                // 在协程内选择文件
                                val file = chooseApkFile()
                                file?.let {
                                    // 回到主线程处理文件选择
                                    onSelectFile(it)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("选择 APK")
                    }

                    Button(
                        onClick = onSignClicked,
                        enabled = apkFile != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("开始签名") }

                    Button(
                        onClick = onResetThePage,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("重置界面") }

                    Spacer(Modifier.height(8.dp))

                    signerConfigUI(config)

                    Spacer(Modifier.height(8.dp))

                    AnimatedVisibility(
                        visible = config.alias == "androiddebugkey",
                    ) {
                        Text(
                            text = "🔐 正式使用时请替换你自己的签名文件",
                            style = MaterialTheme.typography.subtitle2,
                            color = Color(0xFFF44336),
                        )
                    }
                }
            }

            // 日志输出
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = 4.dp,
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("日志：", style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.secondary)
                    Spacer(Modifier.height(4.dp))
                    logDisplay(logLines = logLines)
                }
            }
        }
    }
}