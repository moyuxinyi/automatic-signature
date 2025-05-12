package ui

import androidx.compose.runtime.Composable
import config.SignConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import controller.revealInExplorer
import java.io.File
import androidx.compose.ui.draw.clip

/**
 * 将签名配置ui显示的这一块的内容提出来
 *
 * @author 杨耿雷
 * @since 2025/5/8 14:42
 */
@Composable
fun signerConfigUI(config: SignConfig) {

    val rippleShape = RoundedCornerShape(8.dp)

    Card(
        shape = rippleShape,
        backgroundColor = MaterialTheme.colors.background,
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        val keystoreFileName = config.keystorePath.substringAfterLast(File.separator)
        val signedDirName = config.signedDir.substringAfterLast(File.separator)

        Column(Modifier.padding(6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(rippleShape)
                    .clickable(
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        val signatureConfig =
                            File(File(config.apksignerPath).parentFile.absolutePath, "signature.properties")
                        revealInExplorer(signatureConfig)
                    }.padding(vertical = 8.dp)
            ) {
                Text(
                    text = "📝 签名配置",
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(rippleShape)
                    .clickable(
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        revealInExplorer(File(config.keystorePath))
                    }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "📄 签名文件: $keystoreFileName",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "🏷️ 别名: ${config.alias}",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(rippleShape)
                    .clickable(
                        indication = rememberRipple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        revealInExplorer(File(config.signedDir))
                    }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "📂 输出目录: $signedDirName",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                )
            }
        }
    }
}