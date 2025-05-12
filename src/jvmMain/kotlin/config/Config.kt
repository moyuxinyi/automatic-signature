package config

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*

/**
 * 配置相关的功能
 * 主要用于加载签名工具所需的配置文件与资源
 *
 * @author 杨耿雷
 * @since 2025/5/7 18:47
 */

/**
 * 获取配置目录
 * 自动创建 config 目录（若不存在）
 */
fun getConfigDir(): File = File("config").apply { mkdirs() }

/**
 * 获取构建输出目录
 * 自动创建 build_product 目录（若不存在）
 */
fun getBuildDir(): File = File("build_product").apply { mkdirs() }

/**
 * 如果配置文件 signature.properties 存在，则读取配置
 * 否则生成默认配置，并从资源复制必要工具（zipalign、apksigner、keystore）
 */
fun loadConfig(): SignConfig {
    val configDir = getConfigDir()
    // 配置文件路径
    val configFile = File(configDir, "signature.properties")
    // 输出目录（aligned、signed 的绝对路径）
    val buildDirs = getOutputDirs()

    // apksigner签名脚本jar依赖文件
    val apksignerJarFile = File(configDir, "apksigner.jar")
    copyFromResourceIfMissing(apksignerJarFile.name, apksignerJarFile)

    // 读取配置文件（若存在），否则生成默认配置
    val signConfig = readSignConfig(configFile, buildDirs) ?: run {
        // zipalign 对齐工具
        val zipalign = File(configDir, "zipalign.exe").also {
            copyFromResourceIfMissing("zipalign.exe", it)
        }
        // apksigner 批处理脚本
        val apksigner = File(configDir, "apksigner.bat").also {
            copyFromResourceIfMissing("apksigner.bat", it)
        }
        // 签名文件路径
        val keystore = File(configDir, "signature.jks").also {
            copyFromResourceIfMissing("signature.keystore", it)
        }

        // 组装默认配置对象（包含默认路径与调试签名信息）
        val defaultConfig = SignConfig(
            zipalignPath = zipalign.absolutePath,
            apksignerPath = apksigner.absolutePath,
            keystorePath = keystore.absolutePath,
            alias = "androiddebugkey",
            storePassword = "android",
            keyPassword = "android",
            alignedDir = buildDirs.first,
            signedDir = buildDirs.second
        )

        // 写入默认配置
        FileOutputStream(configFile).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                writer.write("# 自动签名工具配置\n")
                writer.write("# 以下配置中的所有属性值，均可随意替换\n")
                // 将配置项逐条写入（路径做转义处理）
                defaultConfig.toProperties().forEach { (key, value) ->
                    writer.write("$key=${value.replace(File.separator, "\\\\")}\n")
                }
            }
        }
        println("✅ 已生成默认配置文件：${configFile.absolutePath}")
        defaultConfig
    }
    return signConfig
}

/**
 * 构建输出目录路径
 * @return Pair<alignedDirPath, signedDirPath>
 */
private fun getOutputDirs(): Pair<String, String> {
    val buildDir = getBuildDir().absolutePath
    return buildDir + File.separator + "aligned" to
            buildDir + File.separator + "signed"
}

/**
 * 根据路径返回 File 对象（支持 null 判空）
 */
fun getFileByPath(filepath: String?): File? =
    if (filepath.isNullOrEmpty()) null else File(filepath)

/**
 * 读取签名配置文件（若存在）
 *
 * @param fallbackDirs 用于默认输出目录补充
 */
fun readSignConfig(configFile: File, fallbackDirs: Pair<String, String>): SignConfig? {
    if (!configFile.exists() || !configFile.isFile) {
        return null
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
        alignedDir = props.getProperty("alignedDir", fallbackDirs.first),
        signedDir = props.getProperty("signedDir", fallbackDirs.second)
    )
}

/**
 * 复制资源文件到目标路径（若目标不存在）
 *
 * @param resourceName 位于 resources 根目录的文件名
 * @param targetFile 要写入的目标文件
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

/**
 * 将 SignConfig 对象转为键值对形式（用于写入 properties 文件）
 */
private fun SignConfig.toProperties(): Map<String, String> = mapOf(
    "zipalignPath" to zipalignPath,
    "apksignerPath" to apksignerPath,
    "keystorePath" to keystorePath,
    "alias" to alias,
    "storePassword" to storePassword,
    "keyPassword" to keyPassword,
    "alignedDir" to alignedDir,
    "signedDir" to signedDir
)