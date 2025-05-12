package config

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*

/**
 * 配置相关的功能
 *
 * @author 杨耿雷
 * @since 2025/5/7 18:47
 */

/**
 * 从配置文件加载 SignConfig
 */
fun getConfigDir(): File {
    // 可放在 config 子目录下
    return File("config").apply { mkdirs() }
}

/**
 * 获取构建目录
 */
fun getBuildDir(): File {
    return File("build_product").apply {
        mkdirs()
    }
}

/**
 * 加载配置
 */
fun loadConfig(): SignConfig {
    val configDir = getConfigDir()
    val buildDir = getBuildDir()

    // 配置文件
    val configFile = File(configDir, "signature.properties")
    // zipalign对齐工具文件
    val zipalignFile = File(configDir, "zipalign.exe")
    // apksigner签名脚本文件
    val apksignerFile = File(configDir, "apksigner.bat")
    // apksigner签名脚本jar依赖文件
    val apksignerJarFile = File(configDir, "apksigner.jar")
    // 签名文件
    val signatureFile = File(configDir, "signature.keystore")

    // 输入目录，构建产品物
    val alignedDir = buildDir.absolutePath.plus(File.separator).plus("aligned")
    val signedDir = buildDir.absolutePath.plus(File.separator).plus("signed")

    // 从 resources 中复制文件到 configDir
    copyFromResourceIfMissing(zipalignFile.name, zipalignFile)
    copyFromResourceIfMissing(apksignerFile.name, apksignerFile)
    copyFromResourceIfMissing(signatureFile.name, signatureFile)
    copyFromResourceIfMissing(apksignerJarFile.name, apksignerJarFile)

    if (!configFile.exists()) {
        val entries = listOf(
            "zipalignPath" to zipalignFile.absolutePath,
            "apksignerPath" to apksignerFile.absolutePath,
            "keystorePath" to signatureFile.absolutePath,
            "alias" to "androiddebugkey",
            "storePassword" to "android",
            "keyPassword" to "android",
            "alignedDir" to alignedDir,
            "signedDir" to signedDir
        )

        FileOutputStream(configFile).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                writer.write("# 自动签名工具配置\n")
                writer.write("# 以下配置中的所有属性值，均可随意替换\n")
                entries.forEach { (key, value) ->
                    writer.write("$key=${value.replace(File.separator, "\\\\")}\n")
                }
            }
        }
        println("✅ 已生成默认配置文件：${configFile.absolutePath}")
    }

    val props = Properties().apply {
        FileInputStream(configFile).use { load(it) }
    }

    return SignConfig(
        zipalignPath = props.getProperty("zipalignPath"),
        apksignerPath = props.getProperty("apksignerPath"),
        keystorePath = props.getProperty("keystorePath"),
        alias = props.getProperty("alias"),
        storePassword = props.getProperty("storePassword"),
        keyPassword = props.getProperty("keyPassword"),
        alignedDir = props.getProperty("alignedDir", alignedDir),
        signedDir = props.getProperty("signedDir", signedDir)
    )
}

/**
 * 复制资源文件到目标路径（仅当目标文件不存在时）
 */
fun copyFromResourceIfMissing(resourceName: String, targetFile: File) {
    if (!targetFile.exists()) {
        val inputStream = object { }.javaClass.getResourceAsStream("/$resourceName")
        if (inputStream != null) {
            targetFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            println("✅ 已从资源复制：$resourceName -> ${targetFile.absolutePath}")
        } else {
            println("⚠️ 资源不存在：$resourceName")
        }
    }
}