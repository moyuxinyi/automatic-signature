package controller

import java.io.File

/**
 * 文件选择器
 *
 * @author 杨耿雷
 * @since 2025/5/7 16:12
 */
fun chooseApkFile(): File? {
    return try {
        val chooser = javax.swing.JFileChooser(File(System.getProperty("user.home"), "Desktop"))
        chooser.dialogTitle = "选择 APK 文件"
        chooser.fileSelectionMode = javax.swing.JFileChooser.FILES_ONLY
        chooser.isAcceptAllFileFilterUsed = false
        chooser.fileFilter = object : javax.swing.filechooser.FileFilter() {
            override fun accept(f: File) = f.isDirectory || f.name.endsWith(".apk", true)
            override fun getDescription() = "APK 文件 (*.apk)"
        }
        val result = chooser.showOpenDialog(null)
        if (result == javax.swing.JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 在资源管理器中显示签名成功后的apk文件
 */
fun revealInExplorer(file: File) {
    if (!file.exists()) return
    try {
        when {
            System.getProperty("os.name").startsWith("Windows") -> {
                Runtime.getRuntime().exec("explorer /select,\"${file.absolutePath}\"")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}