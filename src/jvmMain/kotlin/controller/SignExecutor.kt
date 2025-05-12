package controller


import androidx.compose.runtime.MutableState
import config.SignConfig
import java.io.File

/**
 * apk签名逻辑
 *
 * @param apkFile apk文件
 * @param config 签名配置
 * @param log 日志
 *
 * @author 杨耿雷
 * @since 2025/5/8 13:32
 */
fun signApk(apkFile: File?, config: SignConfig, logState: MutableState<List<String>>) {
    fun log(msg: String) {
        logState.value = logState.value + msg
    }

    if (apkFile == null) {
        log("❌ 错误：未选择 APK")
        return
    }

    val missingFiles = listOf(
        "zipalign 工具" to config.zipalignPath,
        "apksigner 工具" to config.apksignerPath,
        "keystore 签名文件" to config.keystorePath
    ).filter { !File(it.second).exists() }

    if (missingFiles.isNotEmpty()) {
        missingFiles.forEach {
            log("❌ 错误：未找到 ${it.first}，请检查路径是否正确：${it.second}")
        }
        return
    }

    try {
        val alignedOut = File(config.alignedDir).apply { mkdirs() }
        val signedOut = File(config.signedDir).apply { mkdirs() }
        val base = apkFile.nameWithoutExtension
        val alignedFile = File(alignedOut, "${base}_aligned.apk")
        val signedFile = File(signedOut, "${base}_signed.apk")

        log("➡️ 正在对齐：${apkFile.name} -> ${alignedFile.absolutePath}")
        val alignProcess = ProcessBuilder(
            config.zipalignPath, "-f", "-v", "4",
            apkFile.absolutePath, alignedFile.absolutePath
        ).inheritIO().start()
        if (alignProcess.waitFor() != 0) {
            log("❌ 对齐失败")
            return
        }
        log("✅ 对齐完成")

        log("➡️ 正在签名：${alignedFile.name} -> ${signedFile.absolutePath}")
        val signProcess = ProcessBuilder(
            config.apksignerPath, "sign",
            "--ks", config.keystorePath,
            "--ks-key-alias", config.alias,
            "--ks-pass", "pass:${config.storePassword}",
            "--key-pass", "pass:${config.keyPassword}",
            "--out", signedFile.absolutePath,
            alignedFile.absolutePath
        ).inheritIO().start()
        if (signProcess.waitFor() != 0) {
            log("❌ 签名失败")
            return
        }
        log("✅ 签名完成")

        log("🔍 验证签名中：${signedFile.name}")
        val verifyResult = ProcessBuilder(
            config.apksignerPath, "verify", "--verbose", signedFile.absolutePath
        ).inheritIO().start().waitFor()
        if (verifyResult != 0) {
            log("⚠️ 签名验证失败")
        } else {
            log("✅ 签名验证通过")

            log("📂 正在打开输出目录...")
            revealInExplorer(signedFile)
            log("✅ 打开完成")
        }

    } catch (ex: Exception) {
        log("❌ 出现异常：${ex.message}")
    }
}