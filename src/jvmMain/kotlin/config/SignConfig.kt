package config

/**
 * 签名配置信息，来源: signature.properties文件
 *
 * @property zipalignPath zipalign 工具路径，用于 APK 对齐优化。
 * @property apksignerPath apksigner 工具路径，用于 APK 签名与验证。
 * @property keystorePath keystore 签名证书文件路径。
 * @property alias keystore 中用于签名的别名（alias）。
 * @property storePassword keystore 文件的访问密码。
 * @property keyPassword 签名别名对应的私钥密码。
 * @property alignedDir 对齐后 APK 的输出目录。
 * @property signedDir 签名后 APK 的输出目录。
 *
 * @author 杨耿雷
 * @since 2025/5/7 19:27
 */
data class SignConfig(
    val zipalignPath: String,
    val apksignerPath: String,
    val keystorePath: String,
    val alias: String,
    val storePassword: String,
    val keyPassword: String,
    val alignedDir: String,
    val signedDir: String
)